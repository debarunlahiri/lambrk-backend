import { ShareModel, CreateShareData, Share, ContentType } from '../models/Share';
import { NotFoundError } from '@lambrk/shared';

export class ShareService {
  private shareModel: ShareModel;

  constructor() {
    this.shareModel = new ShareModel();
  }

  async createShare(data: CreateShareData): Promise<Share> {
    return this.shareModel.create(data);
  }

  async getShare(id: string): Promise<Share> {
    const share = await this.shareModel.findById(id);
    if (!share) {
      throw new NotFoundError('Share not found');
    }
    return share;
  }

  async getContentShares(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    return this.shareModel.getContentShares(contentType, contentId, limit, offset);
  }

  async getUserShares(userId: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    return this.shareModel.getUserShares(userId, limit, offset);
  }

  async getShareCount(contentType: ContentType, contentId: string): Promise<number> {
    return this.shareModel.getShareCount(contentType, contentId);
  }

  async getSharesByPlatform(platform: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    return this.shareModel.getSharesByPlatform(platform, limit, offset);
  }
}

