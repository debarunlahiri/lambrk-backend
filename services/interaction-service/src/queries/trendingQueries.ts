import { Pool } from 'pg';
import { TrendingVideo, TrendingBitz, TrendingPost } from '../models/Trending';

export class TrendingQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async getTrendingVideos(limit: number = 20, offset: number = 0): Promise<TrendingVideo[]> {
    const query = `
      SELECT * FROM trending_videos
      ORDER BY trending_score DESC
      LIMIT $1 OFFSET $2
    `;

    const result = await this.pool.query(query, [limit, offset]);
    return result.rows.map((row: any) => this.mapRowToTrendingVideo(row));
  }

  async getTrendingBitz(limit: number = 20, offset: number = 0): Promise<TrendingBitz[]> {
    const query = `
      SELECT * FROM trending_bitz
      ORDER BY trending_score DESC
      LIMIT $1 OFFSET $2
    `;

    const result = await this.pool.query(query, [limit, offset]);
    return result.rows.map((row: any) => this.mapRowToTrendingBitz(row));
  }

  async getTrendingPosts(limit: number = 20, offset: number = 0): Promise<TrendingPost[]> {
    const query = `
      SELECT * FROM trending_posts
      ORDER BY trending_score DESC
      LIMIT $1 OFFSET $2
    `;

    const result = await this.pool.query(query, [limit, offset]);
    return result.rows.map((row: any) => this.mapRowToTrendingPost(row));
  }

  async refreshTrendingViews(): Promise<void> {
    const query = `SELECT refresh_trending_views()`;
    await this.pool.query(query);
  }

  private mapRowToTrendingVideo(row: any): TrendingVideo {
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
      dislikes: row.dislikes,
      status: row.status,
      isTrending: row.is_trending,
      trendingScore: parseFloat(row.trending_score),
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  private mapRowToTrendingBitz(row: any): TrendingBitz {
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
      trendingScore: parseFloat(row.trending_score),
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  private mapRowToTrendingPost(row: any): TrendingPost {
    return {
      id: row.id,
      title: row.title,
      content: row.content,
      imageUrl: row.image_url,
      userId: row.user_id,
      views: row.views,
      status: row.status,
      trendingScore: parseFloat(row.trending_score),
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}
