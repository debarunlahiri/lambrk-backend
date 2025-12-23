import { Request, Response, NextFunction } from 'express';
import { LikeService } from '../services/likeService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType, LikeType } from '../models/Like';

const likeService = new LikeService();

export const toggleLike = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        error: {
          message: 'Validation failed',
          errors: errors.array(),
        },
      });
      return;
    }

    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { contentType, contentId, likeType } = req.body;

    const result = await likeService.toggleLike(userId, contentType as ContentType, contentId, likeType as LikeType);

    res.status(200).json({
      success: true,
      data: result,
    });
  } catch (error) {
    next(error);
  }
};

export const getLikeStats = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;
    const userId = (req as AuthRequest).user?.userId;

    const stats = await likeService.getLikeStats(contentType as ContentType, contentId, userId);

    res.status(200).json({
      success: true,
      data: { stats },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserLikedContent = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { contentType } = req.params;
    const likeType = req.query.likeType as LikeType || 'like';
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const contentIds = await likeService.getUserLikedContent(userId, contentType as ContentType, likeType, limit, offset);

    res.status(200).json({
      success: true,
      data: { contentIds },
    });
  } catch (error) {
    next(error);
  }
};

export const toggleLikeValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
  body('likeType').isIn(['like', 'dislike']).withMessage('Invalid like type'),
];
