import { Pool } from 'pg';
import { CreateDownloadData, UpdateDownloadData, Download } from '../models/Download';

export class DownloadQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateDownloadData): Promise<Download> {
    const query = `
      INSERT INTO downloads (user_id, content_type, content_id, download_url, file_size, status)
      VALUES ($1, $2, $3, $4, $5, $6)
      RETURNING id, user_id, content_type, content_id, download_url, file_size, status, created_at, updated_at
    `;

    const values = [
      data.userId,
      data.contentType,
      data.contentId,
      data.downloadUrl,
      data.fileSize || null,
      data.status || 'pending',
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToDownload(result.rows[0]);
  }

  async findById(id: string): Promise<Download | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, download_url, file_size, status, created_at, updated_at
      FROM downloads
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToDownload(result.rows[0]);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Download[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, download_url, file_size, status, created_at, updated_at
      FROM downloads
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToDownload(row));
  }

  async update(id: string, userId: string, data: UpdateDownloadData): Promise<Download> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramCount = 1;

    if (data.status !== undefined) {
      updates.push(`status = $${paramCount++}`);
      values.push(data.status);
    }
    if (data.downloadUrl !== undefined) {
      updates.push(`download_url = $${paramCount++}`);
      values.push(data.downloadUrl);
    }
    if (data.fileSize !== undefined) {
      updates.push(`file_size = $${paramCount++}`);
      values.push(data.fileSize);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id, userId);

    const query = `
      UPDATE downloads
      SET ${updates.join(', ')}
      WHERE id = $${paramCount} AND user_id = $${paramCount + 1}
      RETURNING id, user_id, content_type, content_id, download_url, file_size, status, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Download not found or unauthorized');
    }
    return this.mapRowToDownload(result.rows[0]);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    const query = `
      DELETE FROM downloads
      WHERE id = $1 AND user_id = $2
    `;

    const result = await this.pool.query(query, [id, userId]);
    return (result.rowCount ?? 0) > 0;
  }

  private mapRowToDownload(row: any): Download {
    return {
      id: row.id,
      userId: row.user_id,
      contentType: row.content_type,
      contentId: row.content_id,
      downloadUrl: row.download_url,
      fileSize: row.file_size,
      status: row.status,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}
