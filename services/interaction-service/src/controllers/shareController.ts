import { Request, Response, NextFunction } from 'express';
import { ShareService } from '../services/shareService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType } from '../models/Share';

const shareService = new ShareService();

export const createShare = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { contentType, contentId, platform, shareUrl } = req.body;

    const share = await shareService.createShare({
      userId,
      contentType: contentType as ContentType,
      contentId,
      platform,
      shareUrl,
    });

    res.status(201).json({
      success: true,
      data: { share },
    });
  } catch (error) {
    next(error);
  }
};

export const getShare = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const share = await shareService.getShare(id);

    res.status(200).json({
      success: true,
      data: { share },
    });
  } catch (error) {
    next(error);
  }
};

export const getContentShares = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const shares = await shareService.getContentShares(contentType as ContentType, contentId, limit, offset);

    res.status(200).json({
      success: true,
      data: { shares },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserShares = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const shares = await shareService.getUserShares(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { shares },
    });
  } catch (error) {
    next(error);
  }
};

export const getShareCount = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;

    const count = await shareService.getShareCount(contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      data: { count },
    });
  } catch (error) {
    next(error);
  }
};

export const getSharesByPlatform = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { platform } = req.params;
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const shares = await shareService.getSharesByPlatform(platform, limit, offset);

    res.status(200).json({
      success: true,
      data: { shares },
    });
  } catch (error) {
    next(error);
  }
};

export const createShareValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
  body('platform').optional().isString().isLength({ max: 50 }).withMessage('Platform must be less than 50 characters'),
  body('shareUrl').optional().isURL().withMessage('Valid share URL is required'),
];

