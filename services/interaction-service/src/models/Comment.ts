import { getPool } from '@lambrk/shared';
import { CommentQueries } from '../queries/commentQueries';
import { ContentType } from './Like';

export interface Comment {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  parentCommentId?: string;
  commentText: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateCommentData {
  userId: string;
  contentType: ContentType;
  contentId: string;
  parentCommentId?: string;
  commentText: string;
}

export interface UpdateCommentData {
  commentText: string;
}

export class CommentModel {
  private queries: CommentQueries | null = null;

  private getQueries(): CommentQueries {
    if (!this.queries) {
      this.queries = new CommentQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateCommentData): Promise<Comment> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Comment | null> {
    return this.getQueries().findById(id);
  }

  async findByContent(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Comment[]> {
    return this.getQueries().findByContent(contentType, contentId, limit, offset);
  }

  async findReplies(parentCommentId: string, limit: number = 20, offset: number = 0): Promise<Comment[]> {
    return this.getQueries().findReplies(parentCommentId, limit, offset);
  }

  async update(id: string, userId: string, data: UpdateCommentData): Promise<Comment> {
    return this.getQueries().update(id, userId, data);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    return this.getQueries().delete(id, userId);
  }

  async getCount(contentType: ContentType, contentId: string): Promise<number> {
    return this.getQueries().getCount(contentType, contentId);
  }
}
