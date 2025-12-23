import { Pool } from 'pg';
import { CreateLikeData, Like, LikeStats, ContentType, LikeType } from '../models/Like';

export class LikeQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async upsert(data: CreateLikeData): Promise<Like> {
    const query = `
      INSERT INTO likes (user_id, content_type, content_id, like_type)
      VALUES ($1, $2, $3, $4)
      ON CONFLICT (user_id, content_type, content_id)
      DO UPDATE SET like_type = EXCLUDED.like_type, updated_at = NOW()
      RETURNING id, user_id, content_type, content_id, like_type, created_at, updated_at
    `;

    const values = [data.userId, data.contentType, data.contentId, data.likeType];
    const result = await this.pool.query(query, values);
    return this.mapRowToLike(result.rows[0]);
  }

  async remove(userId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    const query = `
      DELETE FROM likes
      WHERE user_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [userId, contentType, contentId]);
    return (result.rowCount ?? 0) > 0;
  }

  async findByUserAndContent(userId: string, contentType: ContentType, contentId: string): Promise<Like | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, like_type, created_at, updated_at
      FROM likes
      WHERE user_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [userId, contentType, contentId]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToLike(result.rows[0]);
  }

  async getStats(contentType: ContentType, contentId: string, userId?: string): Promise<LikeStats> {
    let query = `
      SELECT 
        COUNT(*) FILTER (WHERE like_type = 'like') AS likes,
        COUNT(*) FILTER (WHERE like_type = 'dislike') AS dislikes
    `;

    const values: any[] = [contentType, contentId];

    if (userId) {
      query += `, (SELECT like_type FROM likes WHERE user_id = $3 AND content_type = $1 AND content_id = $2) AS user_like_type`;
      values.push(userId);
    }

    query += `
      FROM likes
      WHERE content_type = $1 AND content_id = $2
    `;

    const result = await this.pool.query(query, values);
    const row = result.rows[0];

    return {
      likes: parseInt(row.likes) || 0,
      dislikes: parseInt(row.dislikes) || 0,
      userLikeType: row.user_like_type || null,
    };
  }

  async getUserLikedContent(userId: string, contentType: ContentType, likeType: LikeType, limit: number = 20, offset: number = 0): Promise<string[]> {
    const query = `
      SELECT content_id
      FROM likes
      WHERE user_id = $1 AND content_type = $2 AND like_type = $3
      ORDER BY created_at DESC
      LIMIT $4 OFFSET $5
    `;

    const result = await this.pool.query(query, [userId, contentType, likeType, limit, offset]);
    return result.rows.map((row: any) => row.content_id);
  }

  private mapRowToLike(row: any): Like {
    return {
      id: row.id,
      userId: row.user_id,
      contentType: row.content_type,
      contentId: row.content_id,
      likeType: row.like_type,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}
