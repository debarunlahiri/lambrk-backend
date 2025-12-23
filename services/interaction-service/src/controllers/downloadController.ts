import { Request, Response, NextFunction } from 'express';
import { DownloadService } from '../services/downloadService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType } from '../models/Like';

const downloadService = new DownloadService();

export const createDownload = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { contentType, contentId, downloadUrl, fileSize, status } = req.body;

    const download = await downloadService.createDownload({
      userId,
      contentType: contentType as ContentType,
      contentId,
      downloadUrl,
      fileSize,
      status,
    });

    res.status(201).json({
      success: true,
      data: { download },
    });
  } catch (error) {
    next(error);
  }
};

export const getDownload = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const download = await downloadService.getDownload(id);

    res.status(200).json({
      success: true,
      data: { download },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserDownloads = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const downloads = await downloadService.getUserDownloads(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { downloads },
    });
  } catch (error) {
    next(error);
  }
};

export const updateDownload = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { id } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { status, downloadUrl, fileSize } = req.body;

    const download = await downloadService.updateDownload(id, userId, { status, downloadUrl, fileSize });

    res.status(200).json({
      success: true,
      data: { download },
    });
  } catch (error) {
    next(error);
  }
};

export const deleteDownload = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    await downloadService.deleteDownload(id, userId);

    res.status(200).json({
      success: true,
      message: 'Download deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const createDownloadValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
  body('downloadUrl').isURL().withMessage('Valid download URL is required'),
  body('fileSize').optional().isInt({ min: 0 }).withMessage('File size must be a positive integer'),
  body('status').optional().isIn(['pending', 'completed', 'failed']).withMessage('Invalid status'),
];

export const updateDownloadValidation = [
  body('status').optional().isIn(['pending', 'completed', 'failed']).withMessage('Invalid status'),
  body('downloadUrl').optional().isURL().withMessage('Valid download URL is required'),
  body('fileSize').optional().isInt({ min: 0 }).withMessage('File size must be a positive integer'),
];
