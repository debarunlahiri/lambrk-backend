import { Pool } from 'pg';
import { CreatePostData, UpdatePostData, Post } from '../models/Post';

export class PostQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreatePostData): Promise<Post> {
    const query = `
      INSERT INTO posts (title, content, image_url, user_id, status)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id, title, content, image_url, user_id, views, status, created_at, updated_at
    `;

    const values = [
      data.title,
      data.content,
      data.imageUrl || null,
      data.userId,
      data.status || 'draft',
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToPost(result.rows[0]);
  }

  async findById(id: string): Promise<Post | null> {
    const query = `
      SELECT id, title, content, image_url, user_id, views, status, created_at, updated_at
      FROM posts
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToPost(result.rows[0]);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Post[]> {
    const query = `
      SELECT id, title, content, image_url, user_id, views, status, created_at, updated_at
      FROM posts
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToPost(row));
  }

  async findAll(limit: number = 20, offset: number = 0, status?: string): Promise<Post[]> {
    let query = `
      SELECT id, title, content, image_url, user_id, views, status, created_at, updated_at
      FROM posts
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
    return result.rows.map((row: any) => this.mapRowToPost(row));
  }

  async update(id: string, userId: string, data: UpdatePostData): Promise<Post> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramCount = 1;

    if (data.title !== undefined) {
      updates.push(`title = $${paramCount++}`);
      values.push(data.title);
    }
    if (data.content !== undefined) {
      updates.push(`content = $${paramCount++}`);
      values.push(data.content);
    }
    if (data.imageUrl !== undefined) {
      updates.push(`image_url = $${paramCount++}`);
      values.push(data.imageUrl);
    }
    if (data.status !== undefined) {
      updates.push(`status = $${paramCount++}`);
      values.push(data.status);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id, userId);

    const query = `
      UPDATE posts
      SET ${updates.join(', ')}
      WHERE id = $${paramCount} AND user_id = $${paramCount + 1}
      RETURNING id, title, content, image_url, user_id, views, status, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Post not found or unauthorized');
    }
    return this.mapRowToPost(result.rows[0]);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    const query = `
      DELETE FROM posts
      WHERE id = $1 AND user_id = $2
    `;

    const result = await this.pool.query(query, [id, userId]);
    return (result.rowCount ?? 0) > 0;
  }

  async incrementViews(id: string): Promise<void> {
    const query = `
      UPDATE posts
      SET views = views + 1
      WHERE id = $1
    `;

    await this.pool.query(query, [id]);
  }

  private mapRowToPost(row: any): Post {
    return {
      id: row.id,
      title: row.title,
      content: row.content,
      imageUrl: row.image_url,
      userId: row.user_id,
      views: row.views,
      status: row.status,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}
