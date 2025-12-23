import dotenv from 'dotenv';

dotenv.config();

export const AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:3101';
export const VIDEO_SERVICE_URL = process.env.VIDEO_SERVICE_URL || 'http://localhost:3102';
export const BITZ_SERVICE_URL = process.env.BITZ_SERVICE_URL || 'http://localhost:3103';
export const POSTS_SERVICE_URL = process.env.POSTS_SERVICE_URL || 'http://localhost:3104';
export const INTERACTION_SERVICE_URL = process.env.INTERACTION_SERVICE_URL || 'http://localhost:3105';
export const CORS_ORIGIN = process.env.CORS_ORIGIN || 'http://localhost:3100';
export const PORT = process.env.PORT || 3100;

