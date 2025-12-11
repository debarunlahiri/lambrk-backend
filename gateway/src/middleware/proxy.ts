import { createProxyMiddleware, Options } from 'http-proxy-middleware';
import { AUTH_SERVICE_URL, VIDEO_SERVICE_URL } from '../config/proxy';

export const createAuthProxy = () => {
  const options: Options = {
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
  };

  return createProxyMiddleware(options);
};

export const createVideoProxy = () => {
  const options: Options = {
    target: VIDEO_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
      '^/api/videos': '/api/videos',
    },
    timeout: 30000,
    proxyTimeout: 30000,
    onProxyReq: (proxyReq, req, res) => {
      console.log(`[Gateway] Proxying ${req.method} ${req.url} to ${VIDEO_SERVICE_URL}${req.url}`);
    },
    onProxyRes: (proxyRes, req, res) => {
      console.log(`[Gateway] Response ${proxyRes.statusCode} for ${req.url}`);
    },
    onError: (err, req, res) => {
      console.error('[Gateway] Video service proxy error:', err);
      if (!res.headersSent) {
        res.status(503).json({
          success: false,
          error: {
            message: 'Video service unavailable',
            details: err.message,
          },
        });
      }
    },
  };

  return createProxyMiddleware(options);
};

