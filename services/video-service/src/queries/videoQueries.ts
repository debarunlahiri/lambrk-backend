import { Pool } from 'pg';
import { CreateVideoData, UpdateVideoData, Video } from '../models/Video';

export class VideoQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateVideoData): Promise<Video> {
    const query = `
      INSERT INTO videos (title, description, url, thumbnail_url, duration, user_id, status)
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id, title, description, url, thumbnail_url, duration, user_id, views, likes, status, created_at, updated_at
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
    return this.mapRowToVideo(result.rows[0]);
  }

  async findById(id: string): Promise<Video | null> {
    const query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, likes, status, created_at, updated_at
      FROM videos
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToVideo(result.rows[0]);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Video[]> {
    const query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, likes, status, created_at, updated_at
      FROM videos
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToVideo(row));
  }

  async findAll(limit: number = 20, offset: number = 0, status?: string): Promise<Video[]> {
    let query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, likes, status, created_at, updated_at
      FROM videos
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
    return result.rows.map((row: any) => this.mapRowToVideo(row));
  }

  async update(id: string, userId: string, data: UpdateVideoData): Promise<Video> {
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
      UPDATE videos
      SET ${updates.join(', ')}
      WHERE id = $${paramCount} AND user_id = $${paramCount + 1}
      RETURNING id, title, description, url, thumbnail_url, duration, user_id, views, likes, status, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Video not found or unauthorized');
    }
    return this.mapRowToVideo(result.rows[0]);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    const query = `
      DELETE FROM videos
      WHERE id = $1 AND user_id = $2
    `;

    const result = await this.pool.query(query, [id, userId]);
    return (result.rowCount ?? 0) > 0;
  }

  async incrementViews(id: string): Promise<void> {
    const query = `
      UPDATE videos
      SET views = views + 1
      WHERE id = $1
    `;

    await this.pool.query(query, [id]);
  }

  async incrementLikes(id: string): Promise<void> {
    const query = `
      UPDATE videos
      SET likes = likes + 1
      WHERE id = $1
    `;

    await this.pool.query(query, [id]);
  }

  private mapRowToVideo(row: any): Video {
    return {
      id: row.id,
      title: row.title,
      description: row.description,
      url: row.url,
      thumbnailUrl: row.thumbnail_url,
      duration: row.duration,
      userId: row.user_id,
      views: row.views,
      likes: row.likes,
      status: row.status,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}

