import { Pool } from 'pg';
import {
  RecommendedVideo,
  RecommendedPost,
  RecommendedBitz,
  TrendingContent,
  UserContext,
} from '../models/Recommendation';
import { ContentType } from '../models/Like';

export class RecommendationQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async getRecommendedVideos(
    userId: string,
    currentVideoId?: string,
    limit: number = 20
  ): Promise<RecommendedVideo[]> {
    const query = `
      SELECT 
        v.id,
        v.title,
        v.description,
        v.url,
        v.thumbnail_url,
        v.duration,
        v.user_id,
        v.views,
        COALESCE(l.like_count, 0) as likes,
        COALESCE(d.dislike_count, 0) as dislikes,
        v.category,
        v.tags,
        v.published_at,
        v.created_at
      FROM videos v
      LEFT JOIN (
        SELECT content_id, COUNT(*) as like_count
        FROM likes
        WHERE content_type = 'video'
        GROUP BY content_id
      ) l ON v.id = l.content_id
      LEFT JOIN (
        SELECT content_id, COUNT(*) as dislike_count
        FROM dislikes
        WHERE content_type = 'video'
        GROUP BY content_id
      ) d ON v.id = d.content_id
      WHERE v.status = 'published'
        AND v.privacy = 'public'
        ${currentVideoId ? `AND v.id != $1` : ''}
      ORDER BY v.created_at DESC
    `;

    const params = currentVideoId ? [currentVideoId] : [];
    const result = await this.pool.query(query, params);
    
    return result.rows.map((row: any) => ({
      id: row.id,
      title: row.title,
      description: row.description,
      url: row.url,
      thumbnailUrl: row.thumbnail_url,
      duration: row.duration,
      userId: row.user_id,
      views: row.views || 0,
      likes: parseInt(row.likes) || 0,
      dislikes: parseInt(row.dislikes) || 0,
      category: row.category,
      tags: row.tags || [],
      publishedAt: row.published_at,
      createdAt: row.created_at,
      score: 0,
    }));
  }

  async getRecommendedPosts(
    userId: string,
    currentPostId?: string,
    limit: number = 15
  ): Promise<RecommendedPost[]> {
    const query = `
      SELECT 
        p.id,
        p.title,
        p.content,
        p.image_url,
        p.user_id,
        p.views,
        COALESCE(l.like_count, 0) as likes,
        COALESCE(d.dislike_count, 0) as dislikes,
        p.created_at
      FROM posts p
      LEFT JOIN (
        SELECT content_id, COUNT(*) as like_count
        FROM likes
        WHERE content_type = 'post'
        GROUP BY content_id
      ) l ON p.id = l.content_id
      LEFT JOIN (
        SELECT content_id, COUNT(*) as dislike_count
        FROM dislikes
        WHERE content_type = 'post'
        GROUP BY content_id
      ) d ON p.id = d.content_id
      WHERE p.status = 'published'
        ${currentPostId ? `AND p.id != $1` : ''}
      ORDER BY p.created_at DESC
    `;

    const params = currentPostId ? [currentPostId] : [];
    const result = await this.pool.query(query, params);
    
    return result.rows.map((row: any) => ({
      id: row.id,
      title: row.title,
      content: row.content,
      imageUrl: row.image_url,
      userId: row.user_id,
      views: row.views || 0,
      likes: parseInt(row.likes) || 0,
      dislikes: parseInt(row.dislikes) || 0,
      createdAt: row.created_at,
      score: 0,
    }));
  }

  async getRecommendedBitz(
    userId: string,
    currentBitzId?: string,
    limit: number = 10
  ): Promise<RecommendedBitz[]> {
    const query = `
      SELECT 
        b.id,
        b.title,
        b.description,
        b.url,
        b.thumbnail_url,
        b.duration,
        b.user_id,
        b.views,
        COALESCE(l.like_count, 0) as likes,
        COALESCE(d.dislike_count, 0) as dislikes,
        b.created_at
      FROM bitz b
      LEFT JOIN (
        SELECT content_id, COUNT(*) as like_count
        FROM likes
        WHERE content_type = 'bitz'
        GROUP BY content_id
      ) l ON b.id = l.content_id
      LEFT JOIN (
        SELECT content_id, COUNT(*) as dislike_count
        FROM dislikes
        WHERE content_type = 'bitz'
        GROUP BY content_id
      ) d ON b.id = d.content_id
      WHERE b.status = 'published'
        ${currentBitzId ? `AND b.id != $1` : ''}
      ORDER BY b.created_at DESC
    `;

    const params = currentBitzId ? [currentBitzId] : [];
    const result = await this.pool.query(query, params);
    
    return result.rows.map((row: any) => ({
      id: row.id,
      title: row.title,
      description: row.description,
      url: row.url,
      thumbnailUrl: row.thumbnail_url,
      duration: row.duration,
      userId: row.user_id,
      views: row.views || 0,
      likes: parseInt(row.likes) || 0,
      dislikes: parseInt(row.dislikes) || 0,
      createdAt: row.created_at,
      score: 0,
    }));
  }

  async getTrendingContent(
    contentType: ContentType,
    timeWindow: '24h' | '7d' | '30d' = '7d',
    limit: number = 10
  ): Promise<TrendingContent[]> {
    const timeIntervals: { [key: string]: string } = {
      '24h': "INTERVAL '24 hours'",
      '7d': "INTERVAL '7 days'",
      '30d': "INTERVAL '30 days'",
    };

    const tableName = contentType === 'video' ? 'videos' : contentType === 'bitz' ? 'bitz' : 'posts';
    const statusColumn = contentType === 'post' ? 'status' : 'status';
    const statusValue = 'published';

    const query = `
      SELECT 
        c.id,
        c.title,
        c.views,
        COALESCE(l.like_count, 0) as likes,
        COALESCE(d.dislike_count, 0) as dislikes,
        c.created_at
      FROM ${tableName} c
      LEFT JOIN (
        SELECT content_id, COUNT(*) as like_count
        FROM likes
        WHERE content_type = $1
        GROUP BY content_id
      ) l ON c.id = l.content_id
      LEFT JOIN (
        SELECT content_id, COUNT(*) as dislike_count
        FROM dislikes
        WHERE content_type = $1
        GROUP BY content_id
      ) d ON c.id = d.content_id
      WHERE c.${statusColumn} = $2
        AND c.created_at > NOW() - ${timeIntervals[timeWindow]}
      ORDER BY c.views DESC, c.created_at DESC
      LIMIT $3
    `;

    const result = await this.pool.query(query, [contentType, statusValue, limit]);
    
    return result.rows.map((row: any) => ({
      id: row.id,
      contentType,
      title: row.title,
      score: 0,
      views: row.views || 0,
      likes: parseInt(row.likes) || 0,
      dislikes: parseInt(row.dislikes) || 0,
      createdAt: row.created_at,
    }));
  }

  async getUserContext(userId: string): Promise<UserContext> {
    const subscriptionsQuery = `
      SELECT channel_id
      FROM subscriptions
      WHERE subscriber_id = $1
    `;

    const likedVideosQuery = `
      SELECT content_id
      FROM likes
      WHERE user_id = $1 AND content_type = 'video'
      ORDER BY created_at DESC
      LIMIT 100
    `;

    const likedPostsQuery = `
      SELECT content_id
      FROM likes
      WHERE user_id = $1 AND content_type = 'post'
      ORDER BY created_at DESC
      LIMIT 100
    `;

    const likedBitzQuery = `
      SELECT content_id
      FROM likes
      WHERE user_id = $1 AND content_type = 'bitz'
      ORDER BY created_at DESC
      LIMIT 100
    `;

    const watchedVideosQuery = `
      SELECT content_id
      FROM view_history
      WHERE user_id = $1 AND content_type = 'video'
      ORDER BY watched_at DESC
      LIMIT 20
    `;

    const watchedPostsQuery = `
      SELECT content_id
      FROM view_history
      WHERE user_id = $1 AND content_type = 'post'
      ORDER BY watched_at DESC
      LIMIT 20
    `;

    const watchedBitzQuery = `
      SELECT content_id
      FROM view_history
      WHERE user_id = $1 AND content_type = 'bitz'
      ORDER BY watched_at DESC
      LIMIT 20
    `;

    const commentedVideosQuery = `
      SELECT DISTINCT content_id
      FROM comments
      WHERE user_id = $1 AND content_type = 'video'
      ORDER BY created_at DESC
      LIMIT 50
    `;

    const commentedPostsQuery = `
      SELECT DISTINCT content_id
      FROM comments
      WHERE user_id = $1 AND content_type = 'post'
      ORDER BY created_at DESC
      LIMIT 50
    `;

    const commentedBitzQuery = `
      SELECT DISTINCT content_id
      FROM comments
      WHERE user_id = $1 AND content_type = 'bitz'
      ORDER BY created_at DESC
      LIMIT 50
    `;

    const [
      subscriptions,
      likedVideos,
      likedPosts,
      likedBitz,
      watchedVideos,
      watchedPosts,
      watchedBitz,
      commentedVideos,
      commentedPosts,
      commentedBitz,
    ] = await Promise.all([
      this.pool.query(subscriptionsQuery, [userId]),
      this.pool.query(likedVideosQuery, [userId]),
      this.pool.query(likedPostsQuery, [userId]),
      this.pool.query(likedBitzQuery, [userId]),
      this.pool.query(watchedVideosQuery, [userId]),
      this.pool.query(watchedPostsQuery, [userId]),
      this.pool.query(watchedBitzQuery, [userId]),
      this.pool.query(commentedVideosQuery, [userId]),
      this.pool.query(commentedPostsQuery, [userId]),
      this.pool.query(commentedBitzQuery, [userId]),
    ]);

    return {
      userId,
      subscribedChannels: subscriptions.rows.map((row: any) => row.channel_id),
      likedContent: {
        video: likedVideos.rows.map((row: any) => row.content_id),
        post: likedPosts.rows.map((row: any) => row.content_id),
        bitz: likedBitz.rows.map((row: any) => row.content_id),
      },
      watchedContent: {
        video: watchedVideos.rows.map((row: any) => row.content_id),
        post: watchedPosts.rows.map((row: any) => row.content_id),
        bitz: watchedBitz.rows.map((row: any) => row.content_id),
      },
      commentedContent: {
        video: commentedVideos.rows.map((row: any) => row.content_id),
        post: commentedPosts.rows.map((row: any) => row.content_id),
        bitz: commentedBitz.rows.map((row: any) => row.content_id),
      },
    };
  }

  async getCommentCounts(contentType: ContentType, contentIds: string[]): Promise<Map<string, number>> {
    if (contentIds.length === 0) {
      return new Map();
    }

    const query = `
      SELECT content_id, COUNT(*) as count
      FROM comments
      WHERE content_type = $1 AND content_id = ANY($2::uuid[])
      GROUP BY content_id
    `;

    const result = await this.pool.query(query, [contentType, contentIds]);
    const counts = new Map<string, number>();
    
    result.rows.forEach((row: any) => {
      counts.set(row.content_id, parseInt(row.count) || 0);
    });

    return counts;
  }

  async getShareCounts(contentType: ContentType, contentIds: string[]): Promise<Map<string, number>> {
    if (contentIds.length === 0) {
      return new Map();
    }

    const query = `
      SELECT content_id, COUNT(*) as count
      FROM shares
      WHERE content_type = $1 AND content_id = ANY($2::uuid[])
      GROUP BY content_id
    `;

    const result = await this.pool.query(query, [contentType, contentIds]);
    const counts = new Map<string, number>();
    
    result.rows.forEach((row: any) => {
      counts.set(row.content_id, parseInt(row.count) || 0);
    });

    return counts;
  }

  async getVideoById(videoId: string): Promise<{ id: string; userId: string } | null> {
    const query = `
      SELECT id, user_id
      FROM videos
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [videoId]);
    if (result.rows.length === 0) {
      return null;
    }

    return {
      id: result.rows[0].id,
      userId: result.rows[0].user_id,
    };
  }
}

