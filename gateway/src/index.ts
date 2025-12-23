import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { PORT, CORS_ORIGIN } from './config/proxy';
import { createGatewayRateLimiter, createGatewaySlowDown, applyRateLimiting } from './middleware/rateLimiter';
import { createAuthProxy, createVideoProxy, createBitzProxy, createPostsProxy, createInteractionProxy } from './middleware/proxy';
import gatewayRoutes from './routes/gatewayRoutes';

dotenv.config();

const app = express();

// CORS configuration
app.use(cors({
  origin: CORS_ORIGIN,
  credentials: true,
}));

// Body parsing middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Rate limiting and DDoS protection
const gatewayRateLimiter = createGatewayRateLimiter();
const gatewaySlowDown = createGatewaySlowDown();
app.use(applyRateLimiting(gatewayRateLimiter, gatewaySlowDown));

// Gateway routes (health check, etc.)
app.use('/', gatewayRoutes);

// Proxy middleware for microservices
app.use('/api/auth', createAuthProxy());
app.use('/api/videos', createVideoProxy());
app.use('/api/bitz', createBitzProxy());
app.use('/api/posts', createPostsProxy());
app.use('/api/likes', createInteractionProxy());
app.use('/api/comments', createInteractionProxy());
app.use('/api/playlists', createInteractionProxy());
app.use('/api/subscriptions', createInteractionProxy());
app.use('/api/downloads', createInteractionProxy());
app.use('/api/trending', createInteractionProxy());

// Start server
app.listen(PORT, () => {
  console.log(`API Gateway running on port ${PORT}`);
  console.log(`Proxying auth service: ${process.env.AUTH_SERVICE_URL || 'http://localhost:3101'}`);
  console.log(`Proxying video service: ${process.env.VIDEO_SERVICE_URL || 'http://localhost:3102'}`);
  console.log(`Proxying bitz service: ${process.env.BITZ_SERVICE_URL || 'http://localhost:3103'}`);
  console.log(`Proxying posts service: ${process.env.POSTS_SERVICE_URL || 'http://localhost:3104'}`);
  console.log(`Proxying interaction service: ${process.env.INTERACTION_SERVICE_URL || 'http://localhost:3105'}`);
});
