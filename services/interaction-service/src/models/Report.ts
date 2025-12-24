import { getPool } from '@lambrk/shared';
import { ReportQueries } from '../queries/reportQueries';

export type ContentType = 'video' | 'bitz' | 'post';

export type ReportStatus = 'pending' | 'reviewed' | 'resolved' | 'dismissed';

export interface Report {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  reason: string;
  description?: string;
  status: ReportStatus;
  reviewedBy?: string;
  reviewedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateReportData {
  userId: string;
  contentType: ContentType;
  contentId: string;
  reason: string;
  description?: string;
}

export interface UpdateReportData {
  status: ReportStatus;
  reviewedBy?: string;
}

export class ReportModel {
  private queries: ReportQueries | null = null;

  private getQueries(): ReportQueries {
    if (!this.queries) {
      this.queries = new ReportQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateReportData): Promise<Report> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Report | null> {
    return this.getQueries().findById(id);
  }

  async findByUserAndContent(userId: string, contentType: ContentType, contentId: string): Promise<Report | null> {
    return this.getQueries().findByUserAndContent(userId, contentType, contentId);
  }

  async update(id: string, data: UpdateReportData): Promise<Report> {
    return this.getQueries().update(id, data);
  }

  async getContentReports(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Report[]> {
    return this.getQueries().getContentReports(contentType, contentId, limit, offset);
  }

  async getUserReports(userId: string, limit: number = 20, offset: number = 0): Promise<Report[]> {
    return this.getQueries().getUserReports(userId, limit, offset);
  }

  async getReportsByStatus(status: ReportStatus, limit: number = 20, offset: number = 0): Promise<Report[]> {
    return this.getQueries().getReportsByStatus(status, limit, offset);
  }

  async getReportCount(contentType: ContentType, contentId: string): Promise<number> {
    return this.getQueries().getReportCount(contentType, contentId);
  }
}

