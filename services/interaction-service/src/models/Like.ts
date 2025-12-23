import { getPool } from '@lambrk/shared';
import { LikeQueries } from '../queries/likeQueries';

export type ContentType = 'video' | 'bitz' | 'post';
export type LikeType = 'like' | 'dislike';

export interface Like {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  likeType: LikeType;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateLikeData {
  userId: string;
  contentType: ContentType;
  contentId: string;
  likeType: LikeType;
}

export interface LikeStats {
  likes: number;
  dislikes: number;
  userLikeType?: LikeType | null;
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

  async getUserLikedContent(userId: string, contentType: ContentType, likeType: LikeType, limit: number = 20, offset: number = 0): Promise<string[]> {
    return this.getQueries().getUserLikedContent(userId, contentType, likeType, limit, offset);
  }
}
