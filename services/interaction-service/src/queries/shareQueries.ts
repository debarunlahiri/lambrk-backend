import { Pool } from 'pg';
import { CreateShareData, Share, ContentType } from '../models/Share';

export class ShareQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateShareData): Promise<Share> {
    const query = `
      INSERT INTO shares (user_id, content_type, content_id, platform, share_url)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id, user_id, content_type, content_id, platform, share_url, created_at
    `;

    const values = [data.userId, data.contentType, data.contentId, data.platform || null, data.shareUrl || null];
    const result = await this.pool.query(query, values);
    return this.mapRowToShare(result.rows[0]);
  }

  async findById(id: string): Promise<Share | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, platform, share_url, created_at
      FROM shares
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToShare(result.rows[0]);
  }

  async getContentShares(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, platform, share_url, created_at
      FROM shares
      WHERE content_type = $1 AND content_id = $2
      ORDER BY created_at DESC
      LIMIT $3 OFFSET $4
    `;

    const result = await this.pool.query(query, [contentType, contentId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToShare(row));
  }

  async getUserShares(userId: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, platform, share_url, created_at
      FROM shares
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToShare(row));
  }

  async getShareCount(contentType: ContentType, contentId: string): Promise<number> {
    const query = `
      SELECT COUNT(*) as count
      FROM shares
      WHERE content_type = $1 AND content_id = $2
    `;

    const result = await this.pool.query(query, [contentType, contentId]);
    return parseInt(result.rows[0].count) || 0;
  }

  async getSharesByPlatform(platform: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, platform, share_url, created_at
      FROM shares
      WHERE platform = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [platform, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToShare(row));
  }

  private mapRowToShare(row: any): Share {
    return {
      id: row.id,
      userId: row.user_id,
      contentType: row.content_type,
      contentId: row.content_id,
      platform: row.platform,
      shareUrl: row.share_url,
      createdAt: row.created_at,
    };
  }
}

