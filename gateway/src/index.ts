import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { createProxyMiddleware } from 'http-proxy-middleware';
import rateLimit from 'express-rate-limit';
import slowDown from 'express-slow-down';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3100;

const AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:3101';
const VIDEO_SERVICE_URL = process.env.VIDEO_SERVICE_URL || 'http://localhost:3102';

// Gateway-level rate limiting and DDoS protection
const gatewayRateLimiter = rateLimit({
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

const gatewaySlowDown = slowDown({
  windowMs: 1 * 60 * 1000, // 1 minute
  delayAfter: 150, // Start delaying after 150 requests
  delayMs: () => 100, // Add 100ms delay per request (v2 syntax)
  maxDelayMs: 2000, // Max 2 seconds delay
  validate: {
    delayMs: false, // Disable validation warning
  },
});

app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:3100',
  credentials: true,
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Apply rate limiting and slow down - but skip for health checks
app.use((req, res, next) => {
  if (req.path === '/health') {
    return next();
  }
  gatewayRateLimiter(req, res, next);
});

app.use((req, res, next) => {
  if (req.path === '/health') {
    return next();
  }
  gatewaySlowDown(req, res, next);
});

app.use(
  '/api/auth',
  createProxyMiddleware({
    target: AUTH_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
      '^/api/auth': '/api/auth',
    },
    timeout: 30000,
    proxyTimeout: 30000,
    onProxyReq: (proxyReq, req, res) => {
      console.log(`[Gateway] Proxying ${req.method} ${req.url} to ${AUTH_SERVICE_URL}${req.url}`);
    },
    onProxyRes: (proxyRes, req, res) => {
      console.log(`[Gateway] Response ${proxyRes.statusCode} for ${req.url}`);
    },
    onError: (err, req, res) => {
      console.error('[Gateway] Auth service proxy error:', err);
      if (!res.headersSent) {
        res.status(503).json({
          success: false,
          error: {
            message: 'Auth service unavailable',
            details: err.message,
          },
        });
      }
    },
  })
);

app.use(
  '/api/videos',
  createProxyMiddleware({
    target: VIDEO_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
      '^/api/videos': '/api/videos',
    },
    onError: (err, req, res) => {
      console.error('Video service proxy error:', err);
      res.status(503).json({
        success: false,
        error: {
          message: 'Video service unavailable',
        },
      });
    },
  })
);

app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'API Gateway is running',
    services: {
      auth: AUTH_SERVICE_URL,
      video: VIDEO_SERVICE_URL,
    },
    timestamp: new Date().toISOString(),
  });
});

app.listen(PORT, () => {
  console.log(`API Gateway running on port ${PORT}`);
  console.log(`Proxying auth service: ${AUTH_SERVICE_URL}`);
  console.log(`Proxying video service: ${VIDEO_SERVICE_URL}`);
});

