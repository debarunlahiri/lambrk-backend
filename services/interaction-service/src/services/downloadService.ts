import { DownloadModel, CreateDownloadData, UpdateDownloadData, Download } from '../models/Download';
import { ContentType } from '../models/Like';
import { NotFoundError, ForbiddenError } from '@lambrk/shared';

export class DownloadService {
  private downloadModel: DownloadModel;

  constructor() {
    this.downloadModel = new DownloadModel();
  }

  async createDownload(data: CreateDownloadData): Promise<Download> {
    return this.downloadModel.create(data);
  }

  async getDownload(id: string): Promise<Download> {
    const download = await this.downloadModel.findById(id);
    if (!download) {
      throw new NotFoundError('Download not found');
    }
    return download;
  }

  async getUserDownloads(userId: string, limit: number = 20, offset: number = 0): Promise<Download[]> {
    return this.downloadModel.findByUserId(userId, limit, offset);
  }

  async updateDownload(id: string, userId: string, data: UpdateDownloadData): Promise<Download> {
    const download = await this.downloadModel.findById(id);
    if (!download) {
      throw new NotFoundError('Download not found');
    }

    if (download.userId !== userId) {
      throw new ForbiddenError('You can only update your own downloads');
    }

    return this.downloadModel.update(id, userId, data);
  }

  async deleteDownload(id: string, userId: string): Promise<void> {
    const download = await this.downloadModel.findById(id);
    if (!download) {
      throw new NotFoundError('Download not found');
    }

    if (download.userId !== userId) {
      throw new ForbiddenError('You can only delete your own downloads');
    }

    const deleted = await this.downloadModel.delete(id, userId);
    if (!deleted) {
      throw new NotFoundError('Download not found');
    }
  }
}
