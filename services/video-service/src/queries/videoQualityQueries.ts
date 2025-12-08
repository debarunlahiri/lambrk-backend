import { Pool } from 'pg';
import { CreateVideoQualityData, UpdateVideoQualityData, VideoQuality, VideoQualityType } from '../models/VideoQuality';

export class VideoQualityQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateVideoQualityData): Promise<VideoQuality> {
    // If this is set as default, unset other defaults for this video first
    if (data.isDefault) {
      await this.pool.query(
        'UPDATE video_qualities SET is_default = false WHERE video_id = $1',
        [data.videoId]
      );
    }

    const query = `
      INSERT INTO video_qualities (
        video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
        codec, container, duration, is_default, status
      )
      VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
      RETURNING id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
                codec, container, duration, is_default, status, created_at, updated_at
    `;

    const values = [
      data.videoId,
      data.quality,
      data.url,
      data.fileSize || null,
      data.bitrate || null,
      data.resolutionWidth || null,
      data.resolutionHeight || null,
      data.codec || null,
      data.container || null,
      data.duration || null,
      data.isDefault || false,
      data.status || 'processing',
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToVideoQuality(result.rows[0]);
  }

  async findById(id: string): Promise<VideoQuality | null> {
    const query = `
      SELECT id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
             codec, container, duration, is_default, status, created_at, updated_at
      FROM video_qualities
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToVideoQuality(result.rows[0]);
  }

  async findByVideoId(videoId: string): Promise<VideoQuality[]> {
    const query = `
      SELECT id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
             codec, container, duration, is_default, status, created_at, updated_at
      FROM video_qualities
      WHERE video_id = $1
      ORDER BY 
        CASE quality
          WHEN 'original' THEN 0
          WHEN '2160p' THEN 1
          WHEN '1440p' THEN 2
          WHEN '1080p' THEN 3
          WHEN '720p' THEN 4
          WHEN '480p' THEN 5
          WHEN '360p' THEN 6
          WHEN '240p' THEN 7
          WHEN '144p' THEN 8
          ELSE 9
        END
    `;

    const result = await this.pool.query(query, [videoId]);
    return result.rows.map((row: any) => this.mapRowToVideoQuality(row));
  }

  async findDefaultByVideoId(videoId: string): Promise<VideoQuality | null> {
    const query = `
      SELECT id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
             codec, container, duration, is_default, status, created_at, updated_at
      FROM video_qualities
      WHERE video_id = $1 AND is_default = true
      LIMIT 1
    `;

    const result = await this.pool.query(query, [videoId]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToVideoQuality(result.rows[0]);
  }

  async findByVideoIdAndQuality(videoId: string, quality: VideoQualityType): Promise<VideoQuality | null> {
    const query = `
      SELECT id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
             codec, container, duration, is_default, status, created_at, updated_at
      FROM video_qualities
      WHERE video_id = $1 AND quality = $2
      LIMIT 1
    `;

    const result = await this.pool.query(query, [videoId, quality]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToVideoQuality(result.rows[0]);
  }

  async update(id: string, data: UpdateVideoQualityData): Promise<VideoQuality> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramCount = 1;

    if (data.quality !== undefined) {
      updates.push(`quality = $${paramCount++}`);
      values.push(data.quality);
    }
    if (data.url !== undefined) {
      updates.push(`url = $${paramCount++}`);
      values.push(data.url);
    }
    if (data.fileSize !== undefined) {
      updates.push(`file_size = $${paramCount++}`);
      values.push(data.fileSize);
    }
    if (data.bitrate !== undefined) {
      updates.push(`bitrate = $${paramCount++}`);
      values.push(data.bitrate);
    }
    if (data.resolutionWidth !== undefined) {
      updates.push(`resolution_width = $${paramCount++}`);
      values.push(data.resolutionWidth);
    }
    if (data.resolutionHeight !== undefined) {
      updates.push(`resolution_height = $${paramCount++}`);
      values.push(data.resolutionHeight);
    }
    if (data.codec !== undefined) {
      updates.push(`codec = $${paramCount++}`);
      values.push(data.codec);
    }
    if (data.container !== undefined) {
      updates.push(`container = $${paramCount++}`);
      values.push(data.container);
    }
    if (data.duration !== undefined) {
      updates.push(`duration = $${paramCount++}`);
      values.push(data.duration);
    }
    if (data.status !== undefined) {
      updates.push(`status = $${paramCount++}`);
      values.push(data.status);
    }
    if (data.isDefault !== undefined) {
      if (data.isDefault) {
        // Get video_id first to unset other defaults
        const videoResult = await this.pool.query('SELECT video_id FROM video_qualities WHERE id = $1', [id]);
        if (videoResult.rows.length > 0) {
          await this.pool.query(
            'UPDATE video_qualities SET is_default = false WHERE video_id = $1 AND id != $2',
            [videoResult.rows[0].video_id, id]
          );
        }
      }
      updates.push(`is_default = $${paramCount++}`);
      values.push(data.isDefault);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id);

    const query = `
      UPDATE video_qualities
      SET ${updates.join(', ')}
      WHERE id = $${paramCount}
      RETURNING id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
                codec, container, duration, is_default, status, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Video quality not found');
    }
    return this.mapRowToVideoQuality(result.rows[0]);
  }

  async delete(id: string): Promise<boolean> {
    const query = `DELETE FROM video_qualities WHERE id = $1`;
    const result = await this.pool.query(query, [id]);
    return (result.rowCount ?? 0) > 0;
  }

  async setAsDefault(id: string, videoId: string): Promise<VideoQuality> {
    // Unset other defaults for this video
    await this.pool.query(
      'UPDATE video_qualities SET is_default = false WHERE video_id = $1',
      [videoId]
    );

    // Set this one as default
    const query = `
      UPDATE video_qualities
      SET is_default = true, updated_at = NOW()
      WHERE id = $1
      RETURNING id, video_id, quality, url, file_size, bitrate, resolution_width, resolution_height,
                codec, container, duration, is_default, status, created_at, updated_at
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      throw new Error('Video quality not found');
    }
    return this.mapRowToVideoQuality(result.rows[0]);
  }

  private mapRowToVideoQuality(row: any): VideoQuality {
    return {
      id: row.id,
      videoId: row.video_id,
      quality: row.quality,
      url: row.url,
      fileSize: row.file_size,
      bitrate: row.bitrate,
      resolutionWidth: row.resolution_width,
      resolutionHeight: row.resolution_height,
      codec: row.codec,
      container: row.container,
      duration: row.duration,
      isDefault: row.is_default,
      status: row.status,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}

