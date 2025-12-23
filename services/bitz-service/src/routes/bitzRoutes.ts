import { Router } from 'express';
import {
  createBitz,
  getBitz,
  getUserBitz,
  getAllBitz,
  updateBitz,
  deleteBitz,
  incrementViews,
  createBitzValidation,
  updateBitzValidation,
} from '../controllers/bitzController';
import { authenticate, createVideoRateLimiter, createGeneralRateLimiter } from '@lambrk/shared';

const router = Router();

// Rate limiters
const bitzOperationLimiter = createVideoRateLimiter({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 20, // 20 operations per hour (bitz are shorter, allow more)
});

const readLimiter = createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per 15 minutes
});

// Bitz routes
router.post('/', bitzOperationLimiter, authenticate as any, createBitzValidation, createBitz as any);
router.get('/', readLimiter, getAllBitz);
router.get('/my-bitz', readLimiter, authenticate as any, getUserBitz as any);
router.get('/:id', readLimiter, getBitz);
router.put('/:id', bitzOperationLimiter, authenticate as any, updateBitzValidation, updateBitz as any);
router.delete('/:id', bitzOperationLimiter, authenticate as any, deleteBitz as any);
router.post('/:id/views', readLimiter, incrementViews);

export default router;
