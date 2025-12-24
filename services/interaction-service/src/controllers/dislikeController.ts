import { Request, Response, NextFunction } from 'express';
import { DislikeService } from '../services/dislikeService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType } from '../models/Like';

const dislikeService = new DislikeService();

export const toggleDislike = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { contentType, contentId } = req.body;

    const result = await dislikeService.toggleDislike(userId, contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      data: result,
    });
  } catch (error) {
    next(error);
  }
};

export const getUserDislikedContent = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const contentIds = await dislikeService.getUserDislikedContent(userId, contentType as ContentType, limit, offset);

    res.status(200).json({
      success: true,
      data: { contentIds },
    });
  } catch (error) {
    next(error);
  }
};

export const toggleDislikeValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
];

