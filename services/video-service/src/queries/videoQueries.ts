import { Pool } from 'pg';
import { CreateVideoData, UpdateVideoData, Video } from '../models/Video';

export class VideoQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateVideoData): Promise<Video> {
    const query = `
      INSERT INTO videos (
        title, description, url, thumbnail_url, duration, user_id, status, processing_status,
        file_size, format, codec, resolution_width, resolution_height, bitrate, frame_rate,
        category, tags, privacy, is_live, live_stream_url, published_at, scheduled_publish_at,
        language, location
      )
      VALUES (
        $1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20, $21, $22, $23, $24
      )
      RETURNING id, title, description, url, thumbnail_url, duration, user_id, views, likes, dislikes, status, processing_status,
        file_size, format, codec, resolution_width, resolution_height, bitrate, frame_rate,
        category, tags, privacy, is_live, live_stream_url, published_at, scheduled_publish_at,
        language, location, created_at, updated_at
    `;

    const values = [
      data.title,
      data.description || null,
      data.url,
      data.thumbnailUrl || null,
      data.duration || null,
      data.userId,
      data.status || 'draft',
      data.processingStatus || 'pending',
      data.fileSize || null,
      data.format || null,
      data.codec || null,
      data.resolutionWidth || null,
      data.resolutionHeight || null,
      data.bitrate || null,
      data.frameRate || null,
      data.category || null,
      data.tags || null,
      data.privacy || 'public',
      data.isLive || false,
      data.liveStreamUrl || null,
      data.publishedAt || null,
      data.scheduledPublishAt || null,
      data.language || null,
      data.location || null,
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToVideo(result.rows[0]);
  }

  async findById(id: string): Promise<Video | null> {
    const query = `
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, likes, dislikes, status, processing_status,
        file_size, format, codec, resolution_width, resolution_height, bitrate, frame_rate,
        category, tags, privacy, is_live, live_stream_url, published_at, scheduled_publish_at,
        language, location, created_at, updated_at
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
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, likes, dislikes, status, processing_status,
        file_size, format, codec, resolution_width, resolution_height, bitrate, frame_rate,
        category, tags, privacy, is_live, live_stream_url, published_at, scheduled_publish_at,
        language, location, created_at, updated_at
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
      SELECT id, title, description, url, thumbnail_url, duration, user_id, views, likes, dislikes, status, processing_status,
        file_size, format, codec, resolution_width, resolution_height, bitrate, frame_rate,
        category, tags, privacy, is_live, live_stream_url, published_at, scheduled_publish_at,
        language, location, created_at, updated_at
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
    if (data.processingStatus !== undefined) {
      updates.push(`processing_status = $${paramCount++}`);
      values.push(data.processingStatus);
    }
    if (data.fileSize !== undefined) {
      updates.push(`file_size = $${paramCount++}`);
      values.push(data.fileSize);
    }
    if (data.format !== undefined) {
      updates.push(`format = $${paramCount++}`);
      values.push(data.format);
    }
    if (data.codec !== undefined) {
      updates.push(`codec = $${paramCount++}`);
      values.push(data.codec);
    }
    if (data.resolutionWidth !== undefined) {
      updates.push(`resolution_width = $${paramCount++}`);
      values.push(data.resolutionWidth);
    }
    if (data.resolutionHeight !== undefined) {
      updates.push(`resolution_height = $${paramCount++}`);
      values.push(data.resolutionHeight);
    }
    if (data.bitrate !== undefined) {
      updates.push(`bitrate = $${paramCount++}`);
      values.push(data.bitrate);
    }
    if (data.frameRate !== undefined) {
      updates.push(`frame_rate = $${paramCount++}`);
      values.push(data.frameRate);
    }
    if (data.category !== undefined) {
      updates.push(`category = $${paramCount++}`);
      values.push(data.category);
    }
    if (data.tags !== undefined) {
      updates.push(`tags = $${paramCount++}`);
      values.push(data.tags);
    }
    if (data.privacy !== undefined) {
      updates.push(`privacy = $${paramCount++}`);
      values.push(data.privacy);
    }
    if (data.isLive !== undefined) {
      updates.push(`is_live = $${paramCount++}`);
      values.push(data.isLive);
    }
    if (data.liveStreamUrl !== undefined) {
      updates.push(`live_stream_url = $${paramCount++}`);
      values.push(data.liveStreamUrl);
    }
    if (data.publishedAt !== undefined) {
      updates.push(`published_at = $${paramCount++}`);
      values.push(data.publishedAt);
    }
    if (data.scheduledPublishAt !== undefined) {
      updates.push(`scheduled_publish_at = $${paramCount++}`);
      values.push(data.scheduledPublishAt);
    }
    if (data.language !== undefined) {
      updates.push(`language = $${paramCount++}`);
      values.push(data.language);
    }
    if (data.location !== undefined) {
      updates.push(`location = $${paramCount++}`);
      values.push(data.location);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id, userId);

    const query = `
      UPDATE videos
      SET ${updates.join(', ')}
      WHERE id = $${paramCount} AND user_id = $${paramCount + 1}
      RETURNING id, title, description, url, thumbnail_url, duration, user_id, views, likes, dislikes, status, processing_status,
        file_size, format, codec, resolution_width, resolution_height, bitrate, frame_rate,
        category, tags, privacy, is_live, live_stream_url, published_at, scheduled_publish_at,
        language, location, created_at, updated_at
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
      dislikes: row.dislikes || 0,
      status: row.status,
      processingStatus: row.processing_status,
      fileSize: row.file_size,
      format: row.format,
      codec: row.codec,
      resolutionWidth: row.resolution_width,
      resolutionHeight: row.resolution_height,
      bitrate: row.bitrate,
      frameRate: row.frame_rate ? parseFloat(row.frame_rate) : undefined,
      category: row.category,
      tags: row.tags,
      privacy: row.privacy || 'public',
      isLive: row.is_live || false,
      liveStreamUrl: row.live_stream_url,
      publishedAt: row.published_at,
      scheduledPublishAt: row.scheduled_publish_at,
      language: row.language,
      location: row.location,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}

