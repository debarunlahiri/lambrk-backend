import {
  RecommendedVideo,
  RecommendedPost,
  RecommendedBitz,
  TrendingContent,
  UserContext,
  RecommendationWeights,
  ContentEngagementData,
} from '../models/Recommendation';
import { RecommendationModel } from '../models/Recommendation';
import { RecommendationQueries } from '../queries/recommendationQueries';
import { getPool } from '@lambrk/shared';
import { ContentType } from '../models/Like';

export class RecommendationService {
  private recommendationModel: RecommendationModel;
  private queries: RecommendationQueries;

  private readonly DEFAULT_WEIGHTS: RecommendationWeights = {
    engagement: 0.40,
    recency: 0.25,
    relevance: 0.20,
    quality: 0.15,
  };

  constructor() {
    this.recommendationModel = new RecommendationModel();
    this.queries = new RecommendationQueries(getPool());
  }

  async getRecommendedVideos(
    userId: string,
    currentVideoId?: string,
    limit: number = 20
  ): Promise<RecommendedVideo[]> {
    const videos = await this.recommendationModel.getRecommendedVideos(userId, currentVideoId, 1000);
    const userContext = await this.recommendationModel.getUserContext(userId);
    
    let currentVideoUserId: string | undefined;
    if (currentVideoId) {
      const currentVideoResult = await this.queries.getVideoById(currentVideoId);
      currentVideoUserId = currentVideoResult?.userId;
    }

    const commentCounts = await this.queries.getCommentCounts('video', videos.map(v => v.id));
    const shareCounts = await this.queries.getShareCounts('video', videos.map(v => v.id));

    const scoredVideos = videos.map(video => {
      const engagementData: ContentEngagementData = {
        views: video.views,
        likes: video.likes,
        dislikes: video.dislikes,
        comments: commentCounts.get(video.id) || 0,
        shares: shareCounts.get(video.id) || 0,
        publishedAt: video.publishedAt,
        createdAt: video.createdAt,
      };

      const engagementScore = this.calculateVideoEngagementScore(engagementData);
      const recencyScore = this.calculateRecencyScore(engagementData.publishedAt || engagementData.createdAt);
      const relevanceScore = this.calculateVideoRelevanceScore(video, userContext, currentVideoUserId);
      const qualityScore = this.calculateQualityScore(video.likes, video.dislikes);

      const finalScore =
        engagementScore * this.DEFAULT_WEIGHTS.engagement +
        recencyScore * this.DEFAULT_WEIGHTS.recency +
        relevanceScore * this.DEFAULT_WEIGHTS.relevance +
        qualityScore * this.DEFAULT_WEIGHTS.quality;

      return {
        ...video,
        score: finalScore,
      };
    });

    const filteredVideos = this.filterVideoRecommendations(scoredVideos, userContext, currentVideoId);
    filteredVideos.sort((a, b) => b.score - a.score);

    return filteredVideos.slice(0, limit);
  }

  async getRecommendedPosts(
    userId: string,
    currentPostId?: string,
    limit: number = 15
  ): Promise<RecommendedPost[]> {
    const posts = await this.recommendationModel.getRecommendedPosts(userId, currentPostId, 1000);
    const userContext = await this.recommendationModel.getUserContext(userId);
    
    const commentCounts = await this.queries.getCommentCounts('post', posts.map(p => p.id));
    const shareCounts = await this.queries.getShareCounts('post', posts.map(p => p.id));

    const scoredPosts = posts.map(post => {
      const engagementData: ContentEngagementData = {
        views: post.views,
        likes: post.likes,
        dislikes: post.dislikes,
        comments: commentCounts.get(post.id) || 0,
        shares: shareCounts.get(post.id) || 0,
        publishedAt: post.createdAt,
        createdAt: post.createdAt,
      };

      const engagementScore = this.calculatePostEngagementScore(engagementData);
      const recencyScore = this.calculateRecencyScore(engagementData.createdAt);
      const relevanceScore = this.calculatePostRelevanceScore(post, userContext);
      const qualityScore = this.calculatePostQualityScore(post.likes, post.dislikes, engagementData.comments, engagementData.shares);

      const finalScore =
        engagementScore * this.DEFAULT_WEIGHTS.engagement +
        recencyScore * this.DEFAULT_WEIGHTS.recency +
        relevanceScore * this.DEFAULT_WEIGHTS.relevance +
        qualityScore * this.DEFAULT_WEIGHTS.quality;

      return {
        ...post,
        score: finalScore,
      };
    });

    const filteredPosts = this.filterPostRecommendations(scoredPosts, userContext, currentPostId);
    filteredPosts.sort((a, b) => b.score - a.score);

    return filteredPosts.slice(0, limit);
  }

  async getRecommendedBitz(
    userId: string,
    currentBitzId?: string,
    limit: number = 10
  ): Promise<RecommendedBitz[]> {
    const bitz = await this.recommendationModel.getRecommendedBitz(userId, currentBitzId, 1000);
    const userContext = await this.recommendationModel.getUserContext(userId);
    
    const commentCounts = await this.queries.getCommentCounts('bitz', bitz.map(b => b.id));
    const shareCounts = await this.queries.getShareCounts('bitz', bitz.map(b => b.id));

    const scoredBitz = bitz.map(bit => {
      const engagementData: ContentEngagementData = {
        views: bit.views,
        likes: bit.likes,
        dislikes: bit.dislikes,
        comments: commentCounts.get(bit.id) || 0,
        shares: shareCounts.get(bit.id) || 0,
        publishedAt: bit.createdAt,
        createdAt: bit.createdAt,
      };

      const engagementScore = this.calculateBitzEngagementScore(engagementData);
      const recencyScore = this.calculateRecencyScore(engagementData.createdAt);
      const relevanceScore = this.calculateBitzRelevanceScore(bit, userContext);
      const qualityScore = this.calculateBitzQualityScore(bit.likes, bit.dislikes, engagementData.comments);

      const finalScore =
        engagementScore * this.DEFAULT_WEIGHTS.engagement +
        recencyScore * this.DEFAULT_WEIGHTS.recency +
        relevanceScore * this.DEFAULT_WEIGHTS.relevance +
        qualityScore * this.DEFAULT_WEIGHTS.quality;

      return {
        ...bit,
        score: finalScore,
      };
    });

    const filteredBitz = this.filterBitzRecommendations(scoredBitz, userContext, currentBitzId);
    filteredBitz.sort((a, b) => b.score - a.score);

    return filteredBitz.slice(0, limit);
  }

  async getTrendingContent(
    contentType: ContentType,
    timeWindow: '24h' | '7d' | '30d' = '7d',
    limit: number = 10
  ): Promise<TrendingContent[]> {
    const content = await this.recommendationModel.getTrendingContent(contentType, timeWindow, 1000);
    const commentCounts = await this.queries.getCommentCounts(contentType, content.map(c => c.id));
    const shareCounts = await this.queries.getShareCounts(contentType, content.map(c => c.id));

    const now = new Date();
    const scoredContent = content.map(item => {
      const hoursSincePublication = this.getHoursSinceDate(item.createdAt, now);
      const comments = commentCounts.get(item.id) || 0;
      const shares = shareCounts.get(item.id) || 0;

      const velocityScore = (item.views / (hoursSincePublication + 1)) * 0.5;
      const engagementRateScore = ((item.likes / (item.views + 1)) * 1000) * 0.3;

      let recencyBoost = 1.0;
      if (timeWindow === '24h') {
        recencyBoost = hoursSincePublication < 24 ? 1.5 : 0.5;
      }

      const finalScore = (velocityScore + engagementRateScore) * recencyBoost;

      return {
        ...item,
        score: finalScore,
      };
    });

    scoredContent.sort((a, b) => b.score - a.score);
    return scoredContent.slice(0, limit);
  }

  private calculateVideoEngagementScore(data: ContentEngagementData): number {
    const normalizedViews = Math.min(data.views / 1000000, 1.0);
    return normalizedViews * 0.40;
  }

  private calculatePostEngagementScore(data: ContentEngagementData): number {
    const engagementRatio = (data.likes - data.dislikes) / (data.likes + data.dislikes + 1);
    const engagementComponent =
      (data.likes * 0.3 + data.comments * 0.2 + data.shares * 0.1 + engagementRatio * 1000 * 0.4) / 10000;
    return engagementComponent * 0.40;
  }

  private calculateBitzEngagementScore(data: ContentEngagementData): number {
    const normalizedViews = Math.min(data.views / 1000000, 1.0);
    const normalizedLikes = Math.min(data.likes / 100000, 1.0);
    const normalizedComments = Math.min(data.comments / 10000, 1.0);
    const engagementRatio = (data.likes - data.dislikes) / (data.likes + data.dislikes + 1);

    const engagementComponent =
      (normalizedViews * 0.3 +
        normalizedLikes * 0.3 +
        normalizedComments * 0.2 +
        engagementRatio * 1000 * 0.2) /
      10;
    return engagementComponent * 0.40;
  }

  private calculateRecencyScore(publishedAt: Date): number {
    const hoursSincePublication = this.getHoursSinceDate(publishedAt, new Date());

    if (hoursSincePublication < 1) {
      return 1.0;
    } else if (hoursSincePublication < 24) {
      return 0.9 - hoursSincePublication / 240;
    } else if (hoursSincePublication < 168) {
      return 0.7 - (hoursSincePublication - 24) / 1440;
    } else if (hoursSincePublication < 720) {
      return 0.5 - (hoursSincePublication - 168) / 11040;
    } else {
      return 0.1;
    }
  }

  private calculateVideoRelevanceScore(
    video: RecommendedVideo,
    userContext: UserContext,
    currentVideoUserId?: string
  ): number {
    let relevance = 0;

    if (userContext.subscribedChannels.includes(video.userId)) {
      relevance += 0.15;
    }

    if (userContext.watchedContent.video?.includes(video.id)) {
      relevance -= 0.05;
    }

    if (currentVideoUserId && video.userId === currentVideoUserId) {
      relevance += 0.05;
    }

    return Math.max(0, Math.min(1, relevance)) * 0.20;
  }

  private calculatePostRelevanceScore(post: RecommendedPost, userContext: UserContext): number {
    let relevance = 0;

    if (userContext.subscribedChannels.includes(post.userId)) {
      relevance += 0.15;
    }

    if (userContext.likedContent.post?.includes(post.id)) {
      relevance -= 0.10;
    }

    if (userContext.commentedContent.post?.includes(post.id)) {
      relevance -= 0.05;
    }

    return Math.max(0, Math.min(1, relevance)) * 0.20;
  }

  private calculateBitzRelevanceScore(bitz: RecommendedBitz, userContext: UserContext): number {
    let relevance = 0;

    if (userContext.subscribedChannels.includes(bitz.userId)) {
      relevance += 0.15;
    }

    if (userContext.likedContent.bitz?.includes(bitz.id)) {
      relevance -= 0.10;
    }

    if (userContext.watchedContent.bitz?.includes(bitz.id)) {
      relevance -= 0.05;
    }

    return Math.max(0, Math.min(1, relevance)) * 0.20;
  }

  private calculateQualityScore(likes: number, dislikes: number): number {
    const likeRatio = likes / (likes + dislikes + 1);
    return likeRatio * 0.15;
  }

  private calculatePostQualityScore(
    likes: number,
    dislikes: number,
    comments: number,
    shares: number
  ): number {
    const likeRatio = (likes / (likes + dislikes + 1)) * 0.5;
    const commentsComponent = comments > 100 ? 0.3 : comments / 333;
    const sharesComponent = shares > 50 ? 0.2 : shares / 250;

    return (likeRatio + commentsComponent + sharesComponent) * 0.15;
  }

  private calculateBitzQualityScore(likes: number, dislikes: number, comments: number): number {
    const likeRatio = (likes / (likes + dislikes + 1)) * 0.6;
    const commentsComponent = (comments / 1000) * 0.4;

    return (likeRatio + commentsComponent) * 0.15;
  }

  private filterVideoRecommendations(
    videos: RecommendedVideo[],
    userContext: UserContext,
    currentVideoId?: string
  ): RecommendedVideo[] {
    const watchedSet = new Set(userContext.watchedContent.video || []);
    const recentWatched = (userContext.watchedContent.video || []).slice(0, 20);

    return videos.filter(video => {
      if (currentVideoId && video.id === currentVideoId) {
        return false;
      }
      if (recentWatched.includes(video.id)) {
        return false;
      }
      return true;
    });
  }

  private filterPostRecommendations(
    posts: RecommendedPost[],
    userContext: UserContext,
    currentPostId?: string
  ): RecommendedPost[] {
    const likedSet = new Set(userContext.likedContent.post || []);

    return posts.filter(post => {
      if (currentPostId && post.id === currentPostId) {
        return false;
      }
      if (likedSet.has(post.id)) {
        return false;
      }
      return true;
    });
  }

  private filterBitzRecommendations(
    bitz: RecommendedBitz[],
    userContext: UserContext,
    currentBitzId?: string
  ): RecommendedBitz[] {
    const watchedSet = new Set(userContext.watchedContent.bitz || []);

    return bitz.filter(bit => {
      if (currentBitzId && bit.id === currentBitzId) {
        return false;
      }
      if (watchedSet.has(bit.id)) {
        return false;
      }
      return true;
    });
  }

  private getHoursSinceDate(date: Date, now: Date): number {
    const diffMs = now.getTime() - date.getTime();
    return diffMs / (1000 * 60 * 60);
  }
}

