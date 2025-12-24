import { getPool } from '@lambrk/shared';
import { ShareQueries } from '../queries/shareQueries';

export type ContentType = 'video' | 'bitz' | 'post';

export interface Share {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  platform?: string;
  shareUrl?: string;
  createdAt: Date;
}

export interface CreateShareData {
  userId: string;
  contentType: ContentType;
  contentId: string;
  platform?: string;
  shareUrl?: string;
}

export class ShareModel {
  private queries: ShareQueries | null = null;

  private getQueries(): ShareQueries {
    if (!this.queries) {
      this.queries = new ShareQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateShareData): Promise<Share> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Share | null> {
    return this.getQueries().findById(id);
  }

  async getContentShares(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    return this.getQueries().getContentShares(contentType, contentId, limit, offset);
  }

  async getUserShares(userId: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    return this.getQueries().getUserShares(userId, limit, offset);
  }

  async getShareCount(contentType: ContentType, contentId: string): Promise<number> {
    return this.getQueries().getShareCount(contentType, contentId);
  }

  async getSharesByPlatform(platform: string, limit: number = 20, offset: number = 0): Promise<Share[]> {
    return this.getQueries().getSharesByPlatform(platform, limit, offset);
  }
}

