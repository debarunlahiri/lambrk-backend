import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { initializeDatabase } from './config/database';
import { errorHandler, createGeneralRateLimiter, createSlowDown } from '@lambrk/shared';

dotenv.config();

// Initialize database BEFORE importing routes (which import controllers, which import services, which import models)
initializeDatabase();

// Import routes after database initialization
import videoRoutes from './routes/videoRoutes';

const app = express();
const PORT = process.env.PORT || 3102;

// Rate limiting and DDoS protection
app.use(createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 200, // 200 requests per 15 minutes (higher for video service)
}));

app.use(createSlowDown({
  windowMs: 1 * 60 * 1000, // 1 minute
  delayAfter: 100, // Start delaying after 100 requests
  delayMs: 50, // Will be converted to function in createSlowDown
  maxDelayMs: 1000, // Max 1 second delay
}));

app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:3100',
  credentials: true,
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use('/api/videos', videoRoutes);

app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'Video service is running',
    timestamp: new Date().toISOString(),
  });
});

app.use(errorHandler);

app.listen(PORT, () => {
  console.log(`Video service running on port ${PORT}`);
});

