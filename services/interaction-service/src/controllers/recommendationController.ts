import { Request, Response } from 'express';
import { RecommendationService } from '../services/recommendationService';
import { ContentType } from '../models/Like';

const recommendationService = new RecommendationService();

export const getRecommendedVideos = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = (req as any).user?.id;
    if (!userId) {
      res.status(401).json({ success: false, error: 'Unauthorized' });
      return;
    }

    const currentVideoId = req.query.currentVideoId as string | undefined;
    const limit = parseInt(req.query.limit as string) || 20;

    const recommendations = await recommendationService.getRecommendedVideos(
      userId,
      currentVideoId,
      limit
    );

    res.status(200).json({
      success: true,
      data: {
        videos: recommendations,
        count: recommendations.length,
      },
    });
  } catch (error: any) {
    console.error('Error getting recommended videos:', error);
    res.status(500).json({
      success: false,
      error: error.message || 'Internal server error',
    });
  }
};

export const getRecommendedPosts = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = (req as any).user?.id;
    if (!userId) {
      res.status(401).json({ success: false, error: 'Unauthorized' });
      return;
    }

    const currentPostId = req.query.currentPostId as string | undefined;
    const limit = parseInt(req.query.limit as string) || 15;

    const recommendations = await recommendationService.getRecommendedPosts(
      userId,
      currentPostId,
      limit
    );

    res.status(200).json({
      success: true,
      data: {
        posts: recommendations,
        count: recommendations.length,
      },
    });
  } catch (error: any) {
    console.error('Error getting recommended posts:', error);
    res.status(500).json({
      success: false,
      error: error.message || 'Internal server error',
    });
  }
};

export const getRecommendedBitz = async (req: Request, res: Response): Promise<void> => {
  try {
    const userId = (req as any).user?.id;
    if (!userId) {
      res.status(401).json({ success: false, error: 'Unauthorized' });
      return;
    }

    const currentBitzId = req.query.currentBitzId as string | undefined;
    const limit = parseInt(req.query.limit as string) || 10;

    const recommendations = await recommendationService.getRecommendedBitz(
      userId,
      currentBitzId,
      limit
    );

    res.status(200).json({
      success: true,
      data: {
        bitz: recommendations,
        count: recommendations.length,
      },
    });
  } catch (error: any) {
    console.error('Error getting recommended bitz:', error);
    res.status(500).json({
      success: false,
      error: error.message || 'Internal server error',
    });
  }
};

export const getRecommendedTrending = async (req: Request, res: Response): Promise<void> => {
  try {
    const contentType = req.params.contentType as ContentType;
    const timeWindow = (req.query.timeWindow as '24h' | '7d' | '30d') || '7d';
    const limit = parseInt(req.query.limit as string) || 10;

    if (!['video', 'bitz', 'post'].includes(contentType)) {
      res.status(400).json({
        success: false,
        error: 'Invalid content type. Must be video, bitz, or post',
      });
      return;
    }

    if (!['24h', '7d', '30d'].includes(timeWindow)) {
      res.status(400).json({
        success: false,
        error: 'Invalid time window. Must be 24h, 7d, or 30d',
      });
      return;
    }

    const trending = await recommendationService.getTrendingContent(
      contentType,
      timeWindow,
      limit
    );

    res.status(200).json({
      success: true,
      data: {
        content: trending,
        contentType,
        timeWindow,
        count: trending.length,
      },
    });
  } catch (error: any) {
    console.error('Error getting trending content:', error);
    res.status(500).json({
      success: false,
      error: error.message || 'Internal server error',
    });
  }
};

