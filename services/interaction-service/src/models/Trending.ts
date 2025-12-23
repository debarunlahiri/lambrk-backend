import { getPool } from '@lambrk/shared';
import { TrendingQueries } from '../queries/trendingQueries';

export interface TrendingVideo {
  id: string;
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  views: number;
  likes: number;
  dislikes: number;
  status: string;
  isTrending: boolean;
  trendingScore: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface TrendingBitz {
  id: string;
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  views: number;
  status: string;
  trendingScore: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface TrendingPost {
  id: string;
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  views: number;
  status: string;
  trendingScore: number;
  createdAt: Date;
  updatedAt: Date;
}

export class TrendingModel {
  private queries: TrendingQueries | null = null;

  private getQueries(): TrendingQueries {
    if (!this.queries) {
      this.queries = new TrendingQueries(getPool());
    }
    return this.queries;
  }

  async getTrendingVideos(limit: number = 20, offset: number = 0): Promise<TrendingVideo[]> {
    return this.getQueries().getTrendingVideos(limit, offset);
  }

  async getTrendingBitz(limit: number = 20, offset: number = 0): Promise<TrendingBitz[]> {
    return this.getQueries().getTrendingBitz(limit, offset);
  }

  async getTrendingPosts(limit: number = 20, offset: number = 0): Promise<TrendingPost[]> {
    return this.getQueries().getTrendingPosts(limit, offset);
  }

  async refreshTrendingViews(): Promise<void> {
    return this.getQueries().refreshTrendingViews();
  }
}
