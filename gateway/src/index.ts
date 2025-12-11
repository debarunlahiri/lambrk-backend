import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { PORT, CORS_ORIGIN } from './config/proxy';
import { createGatewayRateLimiter, createGatewaySlowDown, applyRateLimiting } from './middleware/rateLimiter';
import { createAuthProxy, createVideoProxy } from './middleware/proxy';
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

// Start server
app.listen(PORT, () => {
  console.log(`API Gateway running on port ${PORT}`);
  console.log(`Proxying auth service: ${process.env.AUTH_SERVICE_URL || 'http://localhost:3101'}`);
  console.log(`Proxying video service: ${process.env.VIDEO_SERVICE_URL || 'http://localhost:3102'}`);
});
