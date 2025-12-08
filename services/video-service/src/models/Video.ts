import { getPool } from '@lambrk/shared';
import { VideoQueries } from '../queries/videoQueries';

export interface Video {
  id: string;
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  views: number;
  likes: number;
  status: 'draft' | 'published' | 'processing';
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateVideoData {
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  status?: 'draft' | 'published' | 'processing';
}

export interface UpdateVideoData {
  title?: string;
  description?: string;
  thumbnailUrl?: string;
  status?: 'draft' | 'published' | 'processing';
}

export class VideoModel {
  private queries: VideoQueries | null = null;

  private getQueries(): VideoQueries {
    if (!this.queries) {
      this.queries = new VideoQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateVideoData): Promise<Video> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Video | null> {
    return this.getQueries().findById(id);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Video[]> {
    return this.getQueries().findByUserId(userId, limit, offset);
  }

  async findAll(limit: number = 20, offset: number = 0, status?: string): Promise<Video[]> {
    return this.getQueries().findAll(limit, offset, status);
  }

  async update(id: string, userId: string, data: UpdateVideoData): Promise<Video> {
    return this.getQueries().update(id, userId, data);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    return this.getQueries().delete(id, userId);
  }

  async incrementViews(id: string): Promise<void> {
    return this.getQueries().incrementViews(id);
  }

  async incrementLikes(id: string): Promise<void> {
    return this.getQueries().incrementLikes(id);
  }
}

