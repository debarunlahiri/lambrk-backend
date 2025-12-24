import { Pool } from 'pg';
import { CreateLikeData, Like, LikeStats, ContentType } from '../models/Like';

export class LikeQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateLikeData): Promise<Like> {
    const query = `
      INSERT INTO likes (user_id, content_type, content_id)
      VALUES ($1, $2, $3)
      ON CONFLICT (user_id, content_type, content_id)
      DO UPDATE SET updated_at = NOW()
      RETURNING id, user_id, content_type, content_id, created_at, updated_at
    `;

    const values = [data.userId, data.contentType, data.contentId];
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
      SELECT id, user_id, content_type, content_id, created_at, updated_at
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
    const query = `
      SELECT 
        (SELECT COUNT(*) FROM likes WHERE content_type = $1 AND content_id = $2) AS likes,
        (SELECT COUNT(*) FROM dislikes WHERE content_type = $1 AND content_id = $2) AS dislikes,
        $3::uuid AS user_id
    `;

    const result = await this.pool.query(query, [contentType, contentId, userId || null]);
    const row = result.rows[0];

    let userLiked = false;
    let userDisliked = false;

    if (userId) {
      const userLikeCheck = await this.pool.query(
        'SELECT 1 FROM likes WHERE user_id = $1 AND content_type = $2 AND content_id = $3',
        [userId, contentType, contentId]
      );
      userLiked = userLikeCheck.rows.length > 0;

      const userDislikeCheck = await this.pool.query(
        'SELECT 1 FROM dislikes WHERE user_id = $1 AND content_type = $2 AND content_id = $3',
        [userId, contentType, contentId]
      );
      userDisliked = userDislikeCheck.rows.length > 0;
    }

    return {
      likes: parseInt(row.likes) || 0,
      dislikes: parseInt(row.dislikes) || 0,
      userLiked,
      userDisliked,
    };
  }

  async getUserLikedContent(userId: string, contentType: ContentType, limit: number = 20, offset: number = 0): Promise<string[]> {
    const query = `
      SELECT content_id
      FROM likes
      WHERE user_id = $1 AND content_type = $2
      ORDER BY created_at DESC
      LIMIT $3 OFFSET $4
    `;

    const result = await this.pool.query(query, [userId, contentType, limit, offset]);
    return result.rows.map((row: any) => row.content_id);
  }

  private mapRowToLike(row: any): Like {
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
