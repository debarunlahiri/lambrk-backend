import rateLimit from 'express-rate-limit';
import slowDown from 'express-slow-down';

export interface RateLimitConfig {
  windowMs?: number;
  max?: number;
  message?: string;
  standardHeaders?: boolean;
  legacyHeaders?: boolean;
  skipSuccessfulRequests?: boolean;
  skipFailedRequests?: boolean;
}

export interface SlowDownConfig {
  windowMs?: number;
  delayAfter?: number;
  delayMs?: number;
  maxDelayMs?: number;
}

// General API rate limiter - applies to all requests
export const createGeneralRateLimiter = (config: RateLimitConfig = {}) => {
  return rateLimit({
    windowMs: config.windowMs || 15 * 60 * 1000, // 15 minutes
    max: config.max || 100, // Limit each IP to 100 requests per windowMs
    message: config.message || {
      success: false,
      error: {
        message: 'Too many requests from this IP, please try again later.',
        statusCode: 429,
      },
    },
    standardHeaders: config.standardHeaders !== false, // Return rate limit info in the `RateLimit-*` headers
    legacyHeaders: config.legacyHeaders !== false, // Disable the `X-RateLimit-*` headers
    skipSuccessfulRequests: config.skipSuccessfulRequests || false,
    skipFailedRequests: config.skipFailedRequests || false,
  });
};

// Strict rate limiter for authentication endpoints
export const createAuthRateLimiter = (config: RateLimitConfig = {}) => {
  return rateLimit({
    windowMs: config.windowMs || 15 * 60 * 1000, // 15 minutes
    max: config.max || 5, // Limit each IP to 5 requests per windowMs
    message: config.message || {
      success: false,
      error: {
        message: 'Too many authentication attempts, please try again after 15 minutes.',
        statusCode: 429,
      },
    },
    standardHeaders: true,
    legacyHeaders: false,
    skipSuccessfulRequests: false,
    skipFailedRequests: false,
  });
};

// Video upload/creation rate limiter
export const createVideoRateLimiter = (config: RateLimitConfig = {}) => {
  return rateLimit({
    windowMs: config.windowMs || 60 * 60 * 1000, // 1 hour
    max: config.max || 10, // Limit each IP to 10 video operations per hour
    message: config.message || {
      success: false,
      error: {
        message: 'Too many video operations, please try again later.',
        statusCode: 429,
      },
    },
    standardHeaders: true,
    legacyHeaders: false,
  });
};

// Slow down middleware to prevent DDoS attacks
export const createSlowDown = (config: SlowDownConfig = {}) => {
  return slowDown({
    windowMs: config.windowMs || 1 * 60 * 1000, // 1 minute
    delayAfter: config.delayAfter || 50, // Start delaying after 50 requests
    delayMs: () => config.delayMs || 100, // Add delay per request after delayAfter (v2 syntax)
    maxDelayMs: config.maxDelayMs || 2000, // Maximum delay of 2 seconds
    validate: {
      delayMs: false, // Disable validation warning
    },
  });
};

// Aggressive rate limiter for suspicious activity
export const createStrictRateLimiter = (config: RateLimitConfig = {}) => {
  return rateLimit({
    windowMs: config.windowMs || 15 * 60 * 1000, // 15 minutes
    max: config.max || 3, // Limit each IP to 3 requests per windowMs
    message: config.message || {
      success: false,
      error: {
        message: 'Too many requests. Your IP has been temporarily blocked.',
        statusCode: 429,
      },
    },
    standardHeaders: true,
    legacyHeaders: false,
    skipSuccessfulRequests: false,
    skipFailedRequests: false,
  });
};

