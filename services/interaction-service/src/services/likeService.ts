import { LikeModel, CreateLikeData, ContentType, LikeStats } from '../models/Like';
import { DislikeModel } from '../models/Dislike';

export class LikeService {
  private likeModel: LikeModel;
  private dislikeModel: DislikeModel;

  constructor() {
    this.likeModel = new LikeModel();
    this.dislikeModel = new DislikeModel();
  }

  async toggleLike(userId: string, contentType: ContentType, contentId: string): Promise<{ action: 'added' | 'removed'; stats: LikeStats }> {
    const existingLike = await this.likeModel.findByUserAndContent(userId, contentType, contentId);
    const existingDislike = await this.dislikeModel.findByUserAndContent(userId, contentType, contentId);

    if (existingLike) {
      await this.likeModel.remove(userId, contentType, contentId);
      const stats = await this.likeModel.getStats(contentType, contentId, userId);
      return { action: 'removed', stats };
    } else {
      if (existingDislike) {
        await this.dislikeModel.remove(userId, contentType, contentId);
      }
      await this.likeModel.create({ userId, contentType, contentId });
      const stats = await this.likeModel.getStats(contentType, contentId, userId);
      return { action: 'added', stats };
    }
  }

  async getLikeStats(contentType: ContentType, contentId: string, userId?: string): Promise<LikeStats> {
    return this.likeModel.getStats(contentType, contentId, userId);
  }

  async getUserLikedContent(userId: string, contentType: ContentType, limit: number = 20, offset: number = 0): Promise<string[]> {
    return this.likeModel.getUserLikedContent(userId, contentType, limit, offset);
  }
}
