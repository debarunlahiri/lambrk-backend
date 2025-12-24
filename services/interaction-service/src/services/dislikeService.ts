import { DislikeModel, CreateDislikeData, ContentType } from '../models/Dislike';
import { LikeModel } from '../models/Like';
import { LikeStats } from '../models/Like';

export class DislikeService {
  private dislikeModel: DislikeModel;
  private likeModel: LikeModel;

  constructor() {
    this.dislikeModel = new DislikeModel();
    this.likeModel = new LikeModel();
  }

  async toggleDislike(userId: string, contentType: ContentType, contentId: string): Promise<{ action: 'added' | 'removed'; stats: LikeStats }> {
    const existingDislike = await this.dislikeModel.findByUserAndContent(userId, contentType, contentId);
    const existingLike = await this.likeModel.findByUserAndContent(userId, contentType, contentId);

    if (existingDislike) {
      await this.dislikeModel.remove(userId, contentType, contentId);
      const stats = await this.likeModel.getStats(contentType, contentId, userId);
      return { action: 'removed', stats };
    } else {
      if (existingLike) {
        await this.likeModel.remove(userId, contentType, contentId);
      }
      await this.dislikeModel.create({ userId, contentType, contentId });
      const stats = await this.likeModel.getStats(contentType, contentId, userId);
      return { action: 'added', stats };
    }
  }

  async getUserDislikedContent(userId: string, contentType: ContentType, limit: number = 20, offset: number = 0): Promise<string[]> {
    return this.dislikeModel.getUserDislikedContent(userId, contentType, limit, offset);
  }
}

