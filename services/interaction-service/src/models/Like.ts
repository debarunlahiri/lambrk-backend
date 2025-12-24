import { getPool } from '@lambrk/shared';
import { LikeQueries } from '../queries/likeQueries';

export type ContentType = 'video' | 'bitz' | 'post';

export interface Like {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateLikeData {
  userId: string;
  contentType: ContentType;
  contentId: string;
}

export interface LikeStats {
  likes: number;
  dislikes: number;
  userLiked?: boolean;
  userDisliked?: boolean;
}

export class LikeModel {
  private queries: LikeQueries | null = null;

  private getQueries(): LikeQueries {
    if (!this.queries) {
      this.queries = new LikeQueries(getPool());
    }
    return this.queries;
  }

  async upsert(data: CreateLikeData): Promise<Like> {
    return this.getQueries().upsert(data);
  }

  async remove(userId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    return this.getQueries().remove(userId, contentType, contentId);
  }

  async findByUserAndContent(userId: string, contentType: ContentType, contentId: string): Promise<Like | null> {
    return this.getQueries().findByUserAndContent(userId, contentType, contentId);
  }

  async getStats(contentType: ContentType, contentId: string, userId?: string): Promise<LikeStats> {
    return this.getQueries().getStats(contentType, contentId, userId);
  }

  async getUserLikedContent(userId: string, contentType: ContentType, limit: number = 20, offset: number = 0): Promise<string[]> {
    return this.getQueries().getUserLikedContent(userId, contentType, limit, offset);
  }
}
