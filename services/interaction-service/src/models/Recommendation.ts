import { getPool } from '@lambrk/shared';
import { RecommendationQueries } from '../queries/recommendationQueries';
import { ContentType } from './Like';

export interface RecommendedVideo {
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
  category?: string;
  tags?: string[];
  publishedAt?: Date;
  createdAt: Date;
  score: number;
}

export interface RecommendedPost {
  id: string;
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  views: number;
  likes: number;
  dislikes: number;
  createdAt: Date;
  score: number;
}

export interface RecommendedBitz {
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
  createdAt: Date;
  score: number;
}

export interface TrendingContent {
  id: string;
  contentType: ContentType;
  title: string;
  score: number;
  views: number;
  likes: number;
  dislikes: number;
  createdAt: Date;
}

export interface UserContext {
  userId: string;
  subscribedChannels: string[];
  likedContent: { [key in ContentType]?: string[] };
  watchedContent: { [key in ContentType]?: string[] };
  commentedContent: { [key in ContentType]?: string[] };
}

export interface RecommendationWeights {
  engagement: number;
  recency: number;
  relevance: number;
  quality: number;
}

export interface ContentEngagementData {
  views: number;
  likes: number;
  dislikes: number;
  comments: number;
  shares: number;
  publishedAt?: Date;
  createdAt: Date;
}

export class RecommendationModel {
  private queries: RecommendationQueries | null = null;

  private getQueries(): RecommendationQueries {
    if (!this.queries) {
      this.queries = new RecommendationQueries(getPool());
    }
    return this.queries;
  }

  async getRecommendedVideos(
    userId: string,
    currentVideoId?: string,
    limit: number = 20
  ): Promise<RecommendedVideo[]> {
    return this.getQueries().getRecommendedVideos(userId, currentVideoId, limit);
  }

  async getRecommendedPosts(
    userId: string,
    currentPostId?: string,
    limit: number = 15
  ): Promise<RecommendedPost[]> {
    return this.getQueries().getRecommendedPosts(userId, currentPostId, limit);
  }

  async getRecommendedBitz(
    userId: string,
    currentBitzId?: string,
    limit: number = 10
  ): Promise<RecommendedBitz[]> {
    return this.getQueries().getRecommendedBitz(userId, currentBitzId, limit);
  }

  async getTrendingContent(
    contentType: ContentType,
    timeWindow: '24h' | '7d' | '30d' = '7d',
    limit: number = 10
  ): Promise<TrendingContent[]> {
    return this.getQueries().getTrendingContent(contentType, timeWindow, limit);
  }

  async getUserContext(userId: string): Promise<UserContext> {
    return this.getQueries().getUserContext(userId);
  }
}

