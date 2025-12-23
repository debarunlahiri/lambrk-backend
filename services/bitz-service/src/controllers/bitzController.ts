import { Request, Response, NextFunction } from 'express';
import { BitzService } from '../services/bitzService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';

const bitzService = new BitzService();

export const createBitz = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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
        error: {
          message: 'Unauthorized',
        },
      });
      return;
    }

    const { title, description, url, thumbnailUrl, duration, status } = req.body;

    const bitz = await bitzService.createBitz({
      title,
      description,
      url,
      thumbnailUrl,
      duration,
      userId,
      status,
    });

    res.status(201).json({
      success: true,
      data: { bitz },
    });
  } catch (error) {
    next(error);
  }
};

export const getBitz = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const bitz = await bitzService.getBitzById(id);

    res.status(200).json({
      success: true,
      data: { bitz },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserBitz = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: {
          message: 'Unauthorized',
        },
      });
      return;
    }

    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const bitz = await bitzService.getUserBitz(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { bitz },
    });
  } catch (error) {
    next(error);
  }
};

export const getAllBitz = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;
    const status = req.query.status as string | undefined;

    const bitz = await bitzService.getAllBitz(limit, offset, status);

    res.status(200).json({
      success: true,
      data: { bitz },
    });
  } catch (error) {
    next(error);
  }
};

export const updateBitz = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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
        error: {
          message: 'Unauthorized',
        },
      });
      return;
    }

    const { title, description, thumbnailUrl, status } = req.body;

    const bitz = await bitzService.updateBitz(id, userId, {
      title,
      description,
      thumbnailUrl,
      status,
    });

    res.status(200).json({
      success: true,
      data: { bitz },
    });
  } catch (error) {
    next(error);
  }
};

export const deleteBitz = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: {
          message: 'Unauthorized',
        },
      });
      return;
    }

    await bitzService.deleteBitz(id, userId);

    res.status(200).json({
      success: true,
      message: 'Bitz deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const incrementViews = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    await bitzService.incrementViews(id);

    res.status(200).json({
      success: true,
      message: 'Views incremented',
    });
  } catch (error) {
    next(error);
  }
};

export const createBitzValidation = [
  body('title').trim().notEmpty().withMessage('Title is required'),
  body('url').isURL().withMessage('Valid URL is required'),
  body('status').optional().isIn(['draft', 'published', 'processing']).withMessage('Invalid status'),
];

export const updateBitzValidation = [
  body('title').optional().trim().notEmpty().withMessage('Title cannot be empty'),
  body('status').optional().isIn(['draft', 'published', 'processing']).withMessage('Invalid status'),
];
