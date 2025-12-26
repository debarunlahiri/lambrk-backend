import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { PORT, CORS_ORIGIN } from './config/proxy';
import { createGatewayRateLimiter, createGatewaySlowDown, applyRateLimiting } from './middleware/rateLimiter';
import { createAuthProxy, createVideoProxy, createBitzProxy, createPostsProxy, createInteractionProxy, createCompressionProxy } from './middleware/proxy';
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
app.use('/api/recommendations', createInteractionProxy());
app.use('/api/compression', createCompressionProxy());

// Start server
app.listen(PORT, () => {
  console.log(`API Gateway running on port ${PORT}`);
  console.log(`Proxying auth service: ${process.env.AUTH_SERVICE_URL || 'http://localhost:4401'}`);
  console.log(`Proxying video service: ${process.env.VIDEO_SERVICE_URL || 'http://localhost:4402'}`);
  console.log(`Proxying bitz service: ${process.env.BITZ_SERVICE_URL || 'http://localhost:4403'}`);
  console.log(`Proxying posts service: ${process.env.POSTS_SERVICE_URL || 'http://localhost:4404'}`);
  console.log(`Proxying interaction service: ${process.env.INTERACTION_SERVICE_URL || 'http://localhost:4405'}`);
  console.log(`Proxying compression service: ${process.env.COMPRESSION_SERVICE_URL || 'http://localhost:4500'}`);
});
