import { getPool } from '@lambrk/shared';
import { BitzQueries } from '../queries/bitzQueries';

export interface Bitz {
  id: string;
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  views: number;
  status: 'draft' | 'published' | 'processing';
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateBitzData {
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  status?: 'draft' | 'published' | 'processing';
}

export interface UpdateBitzData {
  title?: string;
  description?: string;
  thumbnailUrl?: string;
  status?: 'draft' | 'published' | 'processing';
}

export class BitzModel {
  private queries: BitzQueries | null = null;

  private getQueries(): BitzQueries {
    if (!this.queries) {
      this.queries = new BitzQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateBitzData): Promise<Bitz> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Bitz | null> {
    return this.getQueries().findById(id);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Bitz[]> {
    return this.getQueries().findByUserId(userId, limit, offset);
  }

  async findAll(limit: number = 20, offset: number = 0, status?: string): Promise<Bitz[]> {
    return this.getQueries().findAll(limit, offset, status);
  }

  async update(id: string, userId: string, data: UpdateBitzData): Promise<Bitz> {
    return this.getQueries().update(id, userId, data);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    return this.getQueries().delete(id, userId);
  }

  async incrementViews(id: string): Promise<void> {
    return this.getQueries().incrementViews(id);
  }
}
