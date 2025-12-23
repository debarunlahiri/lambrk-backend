import { Request, Response, NextFunction } from 'express';
import { TrendingService } from '../services/trendingService';

const trendingService = new TrendingService();

export const getTrendingVideos = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const videos = await trendingService.getTrendingVideos(limit, offset);

    res.status(200).json({
      success: true,
      data: { videos },
    });
  } catch (error) {
    next(error);
  }
};

export const getTrendingBitz = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const bitz = await trendingService.getTrendingBitz(limit, offset);

    res.status(200).json({
      success: true,
      data: { bitz },
    });
  } catch (error) {
    next(error);
  }
};

export const getTrendingPosts = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const posts = await trendingService.getTrendingPosts(limit, offset);

    res.status(200).json({
      success: true,
      data: { posts },
    });
  } catch (error) {
    next(error);
  }
};

export const refreshTrending = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    await trendingService.refreshTrending();

    res.status(200).json({
      success: true,
      message: 'Trending data refreshed successfully',
    });
  } catch (error) {
    next(error);
  }
};
