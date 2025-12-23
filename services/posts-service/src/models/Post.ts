import { getPool } from '@lambrk/shared';
import { PostQueries } from '../queries/postQueries';

export interface Post {
  id: string;
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  views: number;
  status: 'draft' | 'published';
  createdAt: Date;
  updatedAt: Date;
}

export interface CreatePostData {
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  status?: 'draft' | 'published';
}

export interface UpdatePostData {
  title?: string;
  content?: string;
  imageUrl?: string;
  status?: 'draft' | 'published';
}

export class PostModel {
  private queries: PostQueries | null = null;

  private getQueries(): PostQueries {
    if (!this.queries) {
      this.queries = new PostQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreatePostData): Promise<Post> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Post | null> {
    return this.getQueries().findById(id);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Post[]> {
    return this.getQueries().findByUserId(userId, limit, offset);
  }

  async findAll(limit: number = 20, offset: number = 0, status?: string): Promise<Post[]> {
    return this.getQueries().findAll(limit, offset, status);
  }

  async update(id: string, userId: string, data: UpdatePostData): Promise<Post> {
    return this.getQueries().update(id, userId, data);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    return this.getQueries().delete(id, userId);
  }

  async incrementViews(id: string): Promise<void> {
    return this.getQueries().incrementViews(id);
  }
}
