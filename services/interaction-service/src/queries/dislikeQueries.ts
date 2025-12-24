import { Pool } from 'pg';
import { CreateDislikeData, Dislike, ContentType } from '../models/Dislike';

export class DislikeQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateDislikeData): Promise<Dislike> {
    const query = `
      INSERT INTO dislikes (user_id, content_type, content_id)
      VALUES ($1, $2, $3)
      ON CONFLICT (user_id, content_type, content_id)
      DO UPDATE SET updated_at = NOW()
      RETURNING id, user_id, content_type, content_id, created_at, updated_at
    `;

    const values = [data.userId, data.contentType, data.contentId];
    const result = await this.pool.query(query, values);
    return this.mapRowToDislike(result.rows[0]);
  }

  async remove(userId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    const query = `
      DELETE FROM dislikes
      WHERE user_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [userId, contentType, contentId]);
    return (result.rowCount ?? 0) > 0;
  }

  async findByUserAndContent(userId: string, contentType: ContentType, contentId: string): Promise<Dislike | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, created_at, updated_at
      FROM dislikes
      WHERE user_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [userId, contentType, contentId]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToDislike(result.rows[0]);
  }

  async getUserDislikedContent(userId: string, contentType: ContentType, limit: number = 20, offset: number = 0): Promise<string[]> {
    const query = `
      SELECT content_id
      FROM dislikes
      WHERE user_id = $1 AND content_type = $2
      ORDER BY created_at DESC
      LIMIT $3 OFFSET $4
    `;

    const result = await this.pool.query(query, [userId, contentType, limit, offset]);
    return result.rows.map((row: any) => row.content_id);
  }

  private mapRowToDislike(row: any): Dislike {
    return {
      id: row.id,
      userId: row.user_id,
      contentType: row.content_type,
      contentId: row.content_id,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}

