import { getPool } from '@lambrk/shared';
import { DislikeQueries } from '../queries/dislikeQueries';
import { ContentType } from './Like';

export interface Dislike {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateDislikeData {
  userId: string;
  contentType: ContentType;
  contentId: string;
}

export class DislikeModel {
  private queries: DislikeQueries | null = null;

  private getQueries(): DislikeQueries {
    if (!this.queries) {
      this.queries = new DislikeQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateDislikeData): Promise<Dislike> {
    return this.getQueries().create(data);
  }

  async remove(userId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    return this.getQueries().remove(userId, contentType, contentId);
  }

  async findByUserAndContent(userId: string, contentType: ContentType, contentId: string): Promise<Dislike | null> {
    return this.getQueries().findByUserAndContent(userId, contentType, contentId);
  }

  async getUserDislikedContent(userId: string, contentType: ContentType, limit: number = 20, offset: number = 0): Promise<string[]> {
    return this.getQueries().getUserDislikedContent(userId, contentType, limit, offset);
  }
}

