import dotenv from 'dotenv';

dotenv.config();

export const AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:3101';
export const VIDEO_SERVICE_URL = process.env.VIDEO_SERVICE_URL || 'http://localhost:3102';
export const CORS_ORIGIN = process.env.CORS_ORIGIN || 'http://localhost:3100';
export const PORT = process.env.PORT || 3100;

