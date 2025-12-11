import rateLimit from 'express-rate-limit';
import slowDown from 'express-slow-down';

export const createGatewayRateLimiter = () => {
  return rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 300, // 300 requests per 15 minutes per IP
    message: {
      success: false,
      error: {
        message: 'Too many requests from this IP, please try again later.',
        statusCode: 429,
      },
    },
    standardHeaders: true,
    legacyHeaders: false,
  });
};

export const createGatewaySlowDown = () => {
  return slowDown({
    windowMs: 1 * 60 * 1000, // 1 minute
    delayAfter: 150, // Start delaying after 150 requests
    delayMs: () => 100, // Add 100ms delay per request (v2 syntax)
    maxDelayMs: 2000, // Max 2 seconds delay
    validate: {
      delayMs: false, // Disable validation warning
    },
  });
};

export const applyRateLimiting = (rateLimiter: ReturnType<typeof createGatewayRateLimiter>, slowDownMiddleware: ReturnType<typeof createGatewaySlowDown>) => {
  return (req: any, res: any, next: any) => {
    // Skip rate limiting for health checks
    if (req.path === '/health') {
      return next();
    }
    // Apply rate limiter first
    rateLimiter(req, res, () => {
      // Then apply slow down
      slowDownMiddleware(req, res, next);
    });
  };
};

