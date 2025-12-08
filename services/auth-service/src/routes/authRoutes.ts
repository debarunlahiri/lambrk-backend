import { Router } from 'express';
import passport from 'passport';
import {
  signup,
  signin,
  refreshToken,
  getProfile,
  googleCallback,
  firebaseAuth,
  signupValidation,
  signinValidation,
  firebaseAuthValidation,
} from '../controllers/authController';
import { authenticate, createAuthRateLimiter } from '@lambrk/shared';

const router = Router();

// Apply strict rate limiting to authentication endpoints
const authLimiter = createAuthRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // 5 attempts per 15 minutes
});

router.post('/signup', authLimiter, signupValidation, signup);
router.post('/signin', authLimiter, signinValidation, signin);
router.post('/refresh-token', authLimiter, refreshToken);
router.post('/firebase', authLimiter, firebaseAuthValidation, firebaseAuth);
router.get('/profile', authenticate as any, getProfile as any);

// Google OAuth routes - only available if credentials are configured
const googleClientID = process.env.GOOGLE_CLIENT_ID;
const googleClientSecret = process.env.GOOGLE_CLIENT_SECRET;

if (googleClientID && googleClientSecret) {
  router.get(
    '/google',
    passport.authenticate('google', {
      scope: ['profile', 'email'],
    })
  );

  router.get(
    '/google/callback',
    passport.authenticate('google', {
      session: false,
      failureRedirect: '/auth/google/failure',
    }),
    googleCallback
  );
} else {
  // Return error if Google OAuth is not configured
  router.get('/google', (req, res) => {
    res.status(503).json({
      success: false,
      error: {
        message: 'Google OAuth is not configured',
        statusCode: 503,
      },
    });
  });

  router.get('/google/callback', (req, res) => {
    res.status(503).json({
      success: false,
      error: {
        message: 'Google OAuth is not configured',
        statusCode: 503,
      },
    });
  });
}

router.get('/google/failure', (req, res) => {
  res.status(401).json({
    success: false,
    error: {
      message: 'Google authentication failed',
    },
  });
});

export default router;

