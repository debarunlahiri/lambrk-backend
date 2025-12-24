import { Pool } from 'pg';
import { CreateReportData, UpdateReportData, Report, ContentType, ReportStatus } from '../models/Report';

export class ReportQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateReportData): Promise<Report> {
    const query = `
      INSERT INTO reports (user_id, content_type, content_id, reason, description)
      VALUES ($1, $2, $3, $4, $5)
      ON CONFLICT (user_id, content_type, content_id)
      DO UPDATE SET reason = EXCLUDED.reason, description = EXCLUDED.description, updated_at = NOW()
      RETURNING id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
    `;

    const values = [data.userId, data.contentType, data.contentId, data.reason, data.description || null];
    const result = await this.pool.query(query, values);
    return this.mapRowToReport(result.rows[0]);
  }

  async findById(id: string): Promise<Report | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
      FROM reports
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToReport(result.rows[0]);
  }

  async findByUserAndContent(userId: string, contentType: ContentType, contentId: string): Promise<Report | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
      FROM reports
      WHERE user_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [userId, contentType, contentId]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToReport(result.rows[0]);
  }

  async update(id: string, data: UpdateReportData): Promise<Report> {
    const query = `
      UPDATE reports
      SET status = $1,
          reviewed_by = $2,
          reviewed_at = CASE WHEN $1 != 'pending' THEN NOW() ELSE reviewed_at END,
          updated_at = NOW()
      WHERE id = $3
      RETURNING id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
    `;

    const result = await this.pool.query(query, [data.status, data.reviewedBy || null, id]);
    return this.mapRowToReport(result.rows[0]);
  }

  async getContentReports(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Report[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
      FROM reports
      WHERE content_type = $1 AND content_id = $2
      ORDER BY created_at DESC
      LIMIT $3 OFFSET $4
    `;

    const result = await this.pool.query(query, [contentType, contentId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToReport(row));
  }

  async getUserReports(userId: string, limit: number = 20, offset: number = 0): Promise<Report[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
      FROM reports
      WHERE user_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToReport(row));
  }

  async getReportsByStatus(status: ReportStatus, limit: number = 20, offset: number = 0): Promise<Report[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, reason, description, status, reviewed_by, reviewed_at, created_at, updated_at
      FROM reports
      WHERE status = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [status, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToReport(row));
  }

  async getReportCount(contentType: ContentType, contentId: string): Promise<number> {
    const query = `
      SELECT COUNT(*) as count
      FROM reports
      WHERE content_type = $1 AND content_id = $2
    `;

    const result = await this.pool.query(query, [contentType, contentId]);
    return parseInt(result.rows[0].count) || 0;
  }

  private mapRowToReport(row: any): Report {
    return {
      id: row.id,
      userId: row.user_id,
      contentType: row.content_type,
      contentId: row.content_id,
      reason: row.reason,
      description: row.description,
      status: row.status,
      reviewedBy: row.reviewed_by,
      reviewedAt: row.reviewed_at,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}

