import { getPool } from '@lambrk/shared';
import { DownloadQueries } from '../queries/downloadQueries';
import { ContentType } from './Like';

export interface Download {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  downloadUrl: string;
  fileSize?: number;
  status: 'pending' | 'completed' | 'failed';
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateDownloadData {
  userId: string;
  contentType: ContentType;
  contentId: string;
  downloadUrl: string;
  fileSize?: number;
  status?: 'pending' | 'completed' | 'failed';
}

export interface UpdateDownloadData {
  status?: 'pending' | 'completed' | 'failed';
  downloadUrl?: string;
  fileSize?: number;
}

export class DownloadModel {
  private queries: DownloadQueries | null = null;

  private getQueries(): DownloadQueries {
    if (!this.queries) {
      this.queries = new DownloadQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateDownloadData): Promise<Download> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Download | null> {
    return this.getQueries().findById(id);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Download[]> {
    return this.getQueries().findByUserId(userId, limit, offset);
  }

  async update(id: string, userId: string, data: UpdateDownloadData): Promise<Download> {
    return this.getQueries().update(id, userId, data);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    return this.getQueries().delete(id, userId);
  }
}
