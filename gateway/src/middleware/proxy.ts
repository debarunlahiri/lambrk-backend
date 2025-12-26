import { createProxyMiddleware, Options } from 'http-proxy-middleware';
import { AUTH_SERVICE_URL, VIDEO_SERVICE_URL, BITZ_SERVICE_URL, POSTS_SERVICE_URL, INTERACTION_SERVICE_URL, COMPRESSION_SERVICE_URL } from '../config/proxy';

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

export const createBitzProxy = () => {
  const options: Options = {
    target: BITZ_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
      '^/api/bitz': '/api/bitz',
    },
    timeout: 30000,
    proxyTimeout: 30000,
    onProxyReq: (proxyReq, req, res) => {
      console.log(`[Gateway] Proxying ${req.method} ${req.url} to ${BITZ_SERVICE_URL}${req.url}`);
    },
    onProxyRes: (proxyRes, req, res) => {
      console.log(`[Gateway] Response ${proxyRes.statusCode} for ${req.url}`);
    },
    onError: (err, req, res) => {
      console.error('[Gateway] Bitz service proxy error:', err);
      if (!res.headersSent) {
        res.status(503).json({
          success: false,
          error: {
            message: 'Bitz service unavailable',
            details: err.message,
          },
        });
      }
    },
  };

  return createProxyMiddleware(options);
};

export const createPostsProxy = () => {
  const options: Options = {
    target: POSTS_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
      '^/api/posts': '/api/posts',
    },
    timeout: 30000,
    proxyTimeout: 30000,
    onProxyReq: (proxyReq, req, res) => {
      console.log(`[Gateway] Proxying ${req.method} ${req.url} to ${POSTS_SERVICE_URL}${req.url}`);
    },
    onProxyRes: (proxyRes, req, res) => {
      console.log(`[Gateway] Response ${proxyRes.statusCode} for ${req.url}`);
    },
    onError: (err, req, res) => {
      console.error('[Gateway] Posts service proxy error:', err);
      if (!res.headersSent) {
        res.status(503).json({
          success: false,
          error: {
            message: 'Posts service unavailable',
            details: err.message,
          },
        });
      }
    },
  };

  return createProxyMiddleware(options);
};

export const createInteractionProxy = () => {
  const options: Options = {
    target: INTERACTION_SERVICE_URL,
    changeOrigin: true,
    timeout: 30000,
    proxyTimeout: 30000,
    onProxyReq: (proxyReq, req, res) => {
      console.log(`[Gateway] Proxying ${req.method} ${req.url} to ${INTERACTION_SERVICE_URL}${req.url}`);
    },
    onProxyRes: (proxyRes, req, res) => {
      console.log(`[Gateway] Response ${proxyRes.statusCode} for ${req.url}`);
    },
    onError: (err, req, res) => {
      console.error('[Gateway] Interaction service proxy error:', err);
      if (!res.headersSent) {
        res.status(503).json({
          success: false,
          error: {
            message: 'Interaction service unavailable',
            details: err.message,
          },
        });
      }
    },
  };

  return createProxyMiddleware(options);
};

export const createCompressionProxy = () => {
  const options: Options = {
    target: COMPRESSION_SERVICE_URL,
    changeOrigin: true,
    pathRewrite: {
      '^/api/compression': '/api/compression',
    },
    timeout: 600000,
    proxyTimeout: 600000,
    onProxyReq: (proxyReq, req, res) => {
      console.log(`[Gateway] Proxying ${req.method} ${req.url} to ${COMPRESSION_SERVICE_URL}${req.url}`);
    },
    onProxyRes: (proxyRes, req, res) => {
      console.log(`[Gateway] Response ${proxyRes.statusCode} for ${req.url}`);
    },
    onError: (err, req, res) => {
      console.error('[Gateway] Compression service proxy error:', err);
      if (!res.headersSent) {
        res.status(503).json({
          success: false,
          error: {
            message: 'Compression service unavailable',
            details: err.message,
          },
        });
      }
    },
  };

  return createProxyMiddleware(options);
};

