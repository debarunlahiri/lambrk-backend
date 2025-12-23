import { TrendingModel, TrendingVideo, TrendingBitz, TrendingPost } from '../models/Trending';

export class TrendingService {
  private trendingModel: TrendingModel;

  constructor() {
    this.trendingModel = new TrendingModel();
  }

  async getTrendingVideos(limit: number = 20, offset: number = 0): Promise<TrendingVideo[]> {
    return this.trendingModel.getTrendingVideos(limit, offset);
  }

  async getTrendingBitz(limit: number = 20, offset: number = 0): Promise<TrendingBitz[]> {
    return this.trendingModel.getTrendingBitz(limit, offset);
  }

  async getTrendingPosts(limit: number = 20, offset: number = 0): Promise<TrendingPost[]> {
    return this.trendingModel.getTrendingPosts(limit, offset);
  }

  async refreshTrending(): Promise<void> {
    await this.trendingModel.refreshTrendingViews();
  }
}
