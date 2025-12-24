import { Router } from 'express';
import {
  createVideo,
  getVideo,
  getUserVideos,
  getAllVideos,
  updateVideo,
  deleteVideo,
  incrementViews,
  incrementLikes,
  updateProcessingStatus,
  addVideoQuality,
  getVideoQualities,
  getVideoQuality,
  updateVideoQuality,
  deleteVideoQuality,
  setDefaultQuality,
  createVideoValidation,
  updateVideoValidation,
  updateProcessingStatusValidation,
  addVideoQualityValidation,
  updateVideoQualityValidation,
} from '../controllers/videoController';
import { authenticate, createVideoRateLimiter, createGeneralRateLimiter } from '@lambrk/shared';

const router = Router();

// Rate limiter for video creation/update/delete operations
const videoOperationLimiter = createVideoRateLimiter({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 10, // 10 operations per hour
});

// General rate limiter for read operations
const readLimiter = createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per 15 minutes
});

// Video routes
router.post('/', videoOperationLimiter, authenticate as any, createVideoValidation, createVideo as any);
router.get('/', readLimiter, getAllVideos);
router.get('/my-videos', readLimiter, authenticate as any, getUserVideos as any);
router.get('/:id', readLimiter, getVideo);
router.put('/:id', videoOperationLimiter, authenticate as any, updateVideoValidation, updateVideo as any);
router.delete('/:id', videoOperationLimiter, authenticate as any, deleteVideo as any);
router.post('/:id/views', readLimiter, incrementViews);
router.post('/:id/likes', readLimiter, incrementLikes);
router.put('/:id/processing-status', videoOperationLimiter, authenticate as any, updateProcessingStatusValidation, updateProcessingStatus as any);

// Video Quality routes
router.post('/:videoId/qualities', videoOperationLimiter, authenticate as any, addVideoQualityValidation, addVideoQuality as any);
router.get('/:videoId/qualities', readLimiter, getVideoQualities);
router.get('/:videoId/qualities/:quality', readLimiter, getVideoQuality);
router.put('/:videoId/qualities/:qualityId', videoOperationLimiter, authenticate as any, updateVideoQualityValidation, updateVideoQuality as any);
router.delete('/:videoId/qualities/:qualityId', videoOperationLimiter, authenticate as any, deleteVideoQuality as any);
router.post('/:videoId/qualities/:qualityId/set-default', videoOperationLimiter, authenticate as any, setDefaultQuality as any);

export default router;

