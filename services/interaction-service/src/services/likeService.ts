import { LikeModel, CreateLikeData, ContentType, LikeType, LikeStats } from '../models/Like';
import { NotFoundError } from '@lambrk/shared';

export class LikeService {
  private likeModel: LikeModel;

  constructor() {
    this.likeModel = new LikeModel();
  }

  async toggleLike(userId: string, contentType: ContentType, contentId: string, likeType: LikeType): Promise<{ action: 'added' | 'updated' | 'removed'; stats: LikeStats }> {
    const existing = await this.likeModel.findByUserAndContent(userId, contentType, contentId);

    if (existing) {
      if (existing.likeType === likeType) {
        // Remove the like/dislike
        await this.likeModel.remove(userId, contentType, contentId);
        const stats = await this.likeModel.getStats(contentType, contentId, userId);
        return { action: 'removed', stats };
      } else {
        // Update to the opposite
        await this.likeModel.upsert({ userId, contentType, contentId, likeType });
        const stats = await this.likeModel.getStats(contentType, contentId, userId);
        return { action: 'updated', stats };
      }
    } else {
      // Add new like/dislike
      await this.likeModel.upsert({ userId, contentType, contentId, likeType });
      const stats = await this.likeModel.getStats(contentType, contentId, userId);
      return { action: 'added', stats };
    }
  }

  async getLikeStats(contentType: ContentType, contentId: string, userId?: string): Promise<LikeStats> {
    return this.likeModel.getStats(contentType, contentId, userId);
  }

  async getUserLikedContent(userId: string, contentType: ContentType, likeType: LikeType, limit: number = 20, offset: number = 0): Promise<string[]> {
    return this.likeModel.getUserLikedContent(userId, contentType, likeType, limit, offset);
  }
}
