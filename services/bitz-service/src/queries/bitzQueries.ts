import { Pool } from 'pg';
import { CreateBitzData, UpdateBitzData, Bitz } from '../models/Bitz';

export class BitzQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateBitzData): Promise<Bitz> {
    const query = `
      INSERT INTO bitz (title, description, url, thumbnail_url, duration, user_id, status)
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id, title, description, url, thumbnail_url, duration, user_id, views, status, created_at, updated_at
    `;

    const values = [
      data.title,
      data.description || null,
      data.url,
      data.thumbnailUrl || null,
      data.duration || null,
      data.userId,
      data.status || 'draft',
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToBitz(result.rows[0]);
  }

  async findById(id: string): Promise<Bitz | null> {
    const query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, status, created_at, updated_at
      FROM bitz
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToBitz(result.rows[0]);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Bitz[]> {
    const query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, status, created_at, updated_at
      FROM bitz
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToBitz(row));
  }

  async findAll(limit: number = 20, offset: number = 0, status?: string): Promise<Bitz[]> {
    let query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, status, created_at, updated_at
      FROM bitz
    `;

    const values: any[] = [];
    if (status) {
      query += ` WHERE status = $1`;
      values.push(status);
      query += ` ORDER BY created_at DESC LIMIT $2 OFFSET $3`;
      values.push(limit, offset);
    } else {
      query += ` ORDER BY created_at DESC LIMIT $1 OFFSET $2`;
      values.push(limit, offset);
    }

    const result = await this.pool.query(query, values);
    return result.rows.map((row: any) => this.mapRowToBitz(row));
  }

  async update(id: string, userId: string, data: UpdateBitzData): Promise<Bitz> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramCount = 1;

    if (data.title !== undefined) {
      updates.push(`title = $${paramCount++}`);
      values.push(data.title);
    }
    if (data.description !== undefined) {
      updates.push(`description = $${paramCount++}`);
      values.push(data.description);
    }
    if (data.thumbnailUrl !== undefined) {
      updates.push(`thumbnail_url = $${paramCount++}`);
      values.push(data.thumbnailUrl);
    }
    if (data.status !== undefined) {
      updates.push(`status = $${paramCount++}`);
      values.push(data.status);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id, userId);

    const query = `
      UPDATE bitz
      SET ${updates.join(', ')}
      WHERE id = $${paramCount} AND user_id = $${paramCount + 1}
      RETURNING id, title, description, url, thumbnail_url, duration, user_id, views, status, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Bitz not found or unauthorized');
    }
    return this.mapRowToBitz(result.rows[0]);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    const query = `
      DELETE FROM bitz
      WHERE id = $1 AND user_id = $2
    `;

    const result = await this.pool.query(query, [id, userId]);
    return (result.rowCount ?? 0) > 0;
  }

  async incrementViews(id: string): Promise<void> {
    const query = `
      UPDATE bitz
      SET views = views + 1
      WHERE id = $1
    `;

    await this.pool.query(query, [id]);
  }

  private mapRowToBitz(row: any): Bitz {
    return {
      id: row.id,
      title: row.title,
      description: row.description,
      url: row.url,
      thumbnailUrl: row.thumbnail_url,
      duration: row.duration,
      userId: row.user_id,
      views: row.views,
      status: row.status,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}
