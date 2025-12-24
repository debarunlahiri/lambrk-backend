import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { initializeDatabase } from './config/database';
import { errorHandler, createGeneralRateLimiter, createSlowDown } from '@lambrk/shared';

dotenv.config();

// Initialize database BEFORE importing routes (which import controllers, which import services, which import models)
initializeDatabase();

// Import routes after database initialization
import authRoutes from './routes/authRoutes';
import passport from './config/passport';

const app = express();
const PORT = process.env.PORT || 4401;

// Rate limiting and DDoS protection
app.use(createGeneralRateLimiter({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per 15 minutes
}));

app.use(createSlowDown({
  windowMs: 1 * 60 * 1000, // 1 minute
  delayAfter: 50, // Start delaying after 50 requests
  delayMs: 100, // Will be converted to function in createSlowDown
  maxDelayMs: 2000, // Max 2 seconds delay
}));

app.use(cors({
  origin: process.env.CORS_ORIGIN || 'http://localhost:4400',
  credentials: true,
}));

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use(passport.initialize());

app.use('/api/auth', authRoutes);

app.get('/health', (req, res) => {
  res.json({
    success: true,
    message: 'Auth service is running',
    timestamp: new Date().toISOString(),
  });
});

app.use(errorHandler);

app.listen(PORT, () => {
  console.log(`Auth service running on port ${PORT}`);
});

