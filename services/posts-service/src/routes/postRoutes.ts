import { Router } from 'express';
import {
  createPost,
  getPost,
  getUserPosts,
  getAllPosts,
  updatePost,
  deletePost,
  incrementViews,
  createPostValidation,
  updatePostValidation,
} from '../controllers/postController';
import { authenticate, createVideoRateLimiter, createGeneralRateLimiter } from '@lambrk/shared';

const router = Router();

// Rate limiters
const postOperationLimiter = createVideoRateLimiter({
  windowMs: 60 * 60 * 1000, // 1 hour
  max: 30, // 30 operations per hour (posts are quick to create)
});

const readLimiter = createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per 15 minutes
});

// Post routes
router.post('/', postOperationLimiter, authenticate as any, createPostValidation, createPost as any);
router.get('/', readLimiter, getAllPosts);
router.get('/my-posts', readLimiter, authenticate as any, getUserPosts as any);
router.get('/:id', readLimiter, getPost);
router.put('/:id', postOperationLimiter, authenticate as any, updatePostValidation, updatePost as any);
router.delete('/:id', postOperationLimiter, authenticate as any, deletePost as any);
router.post('/:id/views', readLimiter, incrementViews);

export default router;
