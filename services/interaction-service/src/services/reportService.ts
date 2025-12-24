import { ReportModel, CreateReportData, UpdateReportData, Report, ContentType, ReportStatus } from '../models/Report';
import { NotFoundError } from '@lambrk/shared';

export class ReportService {
  private reportModel: ReportModel;

  constructor() {
    this.reportModel = new ReportModel();
  }

  async createReport(data: CreateReportData): Promise<Report> {
    return this.reportModel.create(data);
  }

  async getReport(id: string): Promise<Report> {
    const report = await this.reportModel.findById(id);
    if (!report) {
      throw new NotFoundError('Report not found');
    }
    return report;
  }

  async updateReport(id: string, data: UpdateReportData): Promise<Report> {
    const report = await this.reportModel.findById(id);
    if (!report) {
      throw new NotFoundError('Report not found');
    }
    return this.reportModel.update(id, data);
  }

  async getContentReports(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Report[]> {
    return this.reportModel.getContentReports(contentType, contentId, limit, offset);
  }

  async getUserReports(userId: string, limit: number = 20, offset: number = 0): Promise<Report[]> {
    return this.reportModel.getUserReports(userId, limit, offset);
  }

  async getReportsByStatus(status: ReportStatus, limit: number = 20, offset: number = 0): Promise<Report[]> {
    return this.reportModel.getReportsByStatus(status, limit, offset);
  }

  async getReportCount(contentType: ContentType, contentId: string): Promise<number> {
    return this.reportModel.getReportCount(contentType, contentId);
  }

  async checkUserReported(userId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    const report = await this.reportModel.findByUserAndContent(userId, contentType, contentId);
    return report !== null;
  }
}

