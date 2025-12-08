import { Request, Response, NextFunction } from 'express';
import { VideoService } from '../services/videoService';
import { body, query, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';

const videoService = new VideoService();

export const createVideo = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const video = await videoService.createVideo({
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
      data: { video },
    });
  } catch (error) {
    next(error);
  }
};

export const getVideo = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const { quality, includeQualities } = req.query;
    const video = await videoService.getVideoById(id);

    // If quality is specified, get that specific quality
    // If includeQualities is true, get all qualities
    let videoQuality = null;
    let allQualities = null;

    if (quality) {
      try {
        videoQuality = await videoService.getVideoQuality(id, quality as any);
      } catch (error) {
        // Quality not found, continue without it
      }
    } else if (includeQualities === 'true') {
      allQualities = await videoService.getVideoQualities(id);
    }

    res.status(200).json({
      success: true,
      data: {
        video,
        quality: videoQuality,
        qualities: allQualities,
      },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserVideos = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const videos = await videoService.getUserVideos(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { videos },
    });
  } catch (error) {
    next(error);
  }
};

export const getAllVideos = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;
    const status = req.query.status as string | undefined;

    const videos = await videoService.getAllVideos(limit, offset, status);

    res.status(200).json({
      success: true,
      data: { videos },
    });
  } catch (error) {
    next(error);
  }
};

export const updateVideo = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const video = await videoService.updateVideo(id, userId, {
      title,
      description,
      thumbnailUrl,
      status,
    });

    res.status(200).json({
      success: true,
      data: { video },
    });
  } catch (error) {
    next(error);
  }
};

export const deleteVideo = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    await videoService.deleteVideo(id, userId);

    res.status(200).json({
      success: true,
      message: 'Video deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const incrementViews = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    await videoService.incrementViews(id);

    res.status(200).json({
      success: true,
      message: 'Views incremented',
    });
  } catch (error) {
    next(error);
  }
};

export const incrementLikes = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    await videoService.incrementLikes(id);

    res.status(200).json({
      success: true,
      message: 'Likes incremented',
    });
  } catch (error) {
    next(error);
  }
};

export const createVideoValidation = [
  body('title').trim().notEmpty().withMessage('Title is required'),
  body('url').isURL().withMessage('Valid URL is required'),
  body('status').optional().isIn(['draft', 'published', 'processing']).withMessage('Invalid status'),
];

export const updateVideoValidation = [
  body('title').optional().trim().notEmpty().withMessage('Title cannot be empty'),
  body('status').optional().isIn(['draft', 'published', 'processing']).withMessage('Invalid status'),
];

// Video Quality Controllers
export const addVideoQuality = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { videoId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { quality, url, fileSize, bitrate, resolutionWidth, resolutionHeight, codec, container, duration, isDefault, status } = req.body;

    const videoQuality = await videoService.addVideoQuality({
      videoId,
      quality,
      url,
      fileSize,
      bitrate,
      resolutionWidth,
      resolutionHeight,
      codec,
      container,
      duration,
      isDefault,
      status,
    });

    res.status(201).json({
      success: true,
      data: { quality: videoQuality },
    });
  } catch (error) {
    next(error);
  }
};

export const getVideoQualities = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { videoId } = req.params;
    const qualities = await videoService.getVideoQualities(videoId);

    res.status(200).json({
      success: true,
      data: { qualities },
    });
  } catch (error) {
    next(error);
  }
};

export const getVideoQuality = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { videoId, quality } = req.params;
    const videoQuality = await videoService.getVideoQuality(videoId, quality as any);

    res.status(200).json({
      success: true,
      data: { quality: videoQuality },
    });
  } catch (error) {
    next(error);
  }
};

export const updateVideoQuality = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { videoId, qualityId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { quality, url, fileSize, bitrate, resolutionWidth, resolutionHeight, codec, container, duration, isDefault, status } = req.body;

    const videoQuality = await videoService.updateVideoQuality(qualityId, videoId, userId, {
      quality,
      url,
      fileSize,
      bitrate,
      resolutionWidth,
      resolutionHeight,
      codec,
      container,
      duration,
      isDefault,
      status,
    });

    res.status(200).json({
      success: true,
      data: { quality: videoQuality },
    });
  } catch (error) {
    next(error);
  }
};

export const deleteVideoQuality = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { videoId, qualityId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    await videoService.deleteVideoQuality(qualityId, videoId, userId);

    res.status(200).json({
      success: true,
      message: 'Video quality deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const setDefaultQuality = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { videoId, qualityId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const videoQuality = await videoService.setDefaultQuality(qualityId, videoId, userId);

    res.status(200).json({
      success: true,
      data: { quality: videoQuality },
      message: 'Default quality set successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const addVideoQualityValidation = [
  body('videoId').isUUID().withMessage('Valid video ID is required'),
  body('quality').isIn(['144p', '240p', '360p', '480p', '720p', '1080p', '1440p', '2160p', 'original']).withMessage('Invalid quality'),
  body('url').isURL().withMessage('Valid URL is required'),
  body('status').optional().isIn(['processing', 'ready', 'failed']).withMessage('Invalid status'),
];

export const updateVideoQualityValidation = [
  body('quality').optional().isIn(['144p', '240p', '360p', '480p', '720p', '1080p', '1440p', '2160p', 'original']).withMessage('Invalid quality'),
  body('url').optional().isURL().withMessage('Valid URL is required'),
  body('status').optional().isIn(['processing', 'ready', 'failed']).withMessage('Invalid status'),
];

