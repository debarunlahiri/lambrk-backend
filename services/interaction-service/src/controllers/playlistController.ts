import { Request, Response, NextFunction } from 'express';
import { PlaylistService } from '../services/playlistService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType } from '../models/Like';

const playlistService = new PlaylistService();

export const createPlaylist = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { name, description, isPublic } = req.body;

    const playlist = await playlistService.createPlaylist({
      userId,
      name,
      description,
      isPublic,
    });

    res.status(201).json({
      success: true,
      data: { playlist },
    });
  } catch (error) {
    next(error);
  }
};

export const getPlaylist = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const playlist = await playlistService.getPlaylist(id);

    res.status(200).json({
      success: true,
      data: { playlist },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserPlaylists = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const playlists = await playlistService.getUserPlaylists(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { playlists },
    });
  } catch (error) {
    next(error);
  }
};

export const getWatchLater = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const playlist = await playlistService.getOrCreateWatchLater(userId);

    res.status(200).json({
      success: true,
      data: { playlist },
    });
  } catch (error) {
    next(error);
  }
};

export const updatePlaylist = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { name, description, isPublic } = req.body;

    const playlist = await playlistService.updatePlaylist(id, userId, { name, description, isPublic });

    res.status(200).json({
      success: true,
      data: { playlist },
    });
  } catch (error) {
    next(error);
  }
};

export const deletePlaylist = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    await playlistService.deletePlaylist(id, userId);

    res.status(200).json({
      success: true,
      message: 'Playlist deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const addToPlaylist = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { contentType, contentId } = req.body;

    const item = await playlistService.addToPlaylist(id, userId, contentType as ContentType, contentId);

    res.status(201).json({
      success: true,
      data: { item },
    });
  } catch (error) {
    next(error);
  }
};

export const removeFromPlaylist = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id, contentType, contentId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    await playlistService.removeFromPlaylist(id, userId, contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      message: 'Item removed from playlist',
    });
  } catch (error) {
    next(error);
  }
};

export const getPlaylistItems = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const limit = parseInt(req.query.limit as string) || 50;
    const offset = parseInt(req.query.offset as string) || 0;

    const items = await playlistService.getPlaylistItems(id, limit, offset);

    res.status(200).json({
      success: true,
      data: { items },
    });
  } catch (error) {
    next(error);
  }
};

export const checkItemInPlaylist = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id, contentType, contentId } = req.params;

    const exists = await playlistService.checkItemInPlaylist(id, contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      data: { exists },
    });
  } catch (error) {
    next(error);
  }
};

export const createPlaylistValidation = [
  body('name').trim().notEmpty().withMessage('Name is required'),
  body('description').optional().trim(),
  body('isPublic').optional().isBoolean().withMessage('isPublic must be a boolean'),
];

export const updatePlaylistValidation = [
  body('name').optional().trim().notEmpty().withMessage('Name cannot be empty'),
  body('description').optional().trim(),
  body('isPublic').optional().isBoolean().withMessage('isPublic must be a boolean'),
];

export const addToPlaylistValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
];
