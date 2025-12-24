import dotenv from 'dotenv';

dotenv.config();

export const AUTH_SERVICE_URL = process.env.AUTH_SERVICE_URL || 'http://localhost:4401';
export const VIDEO_SERVICE_URL = process.env.VIDEO_SERVICE_URL || 'http://localhost:4402';
export const BITZ_SERVICE_URL = process.env.BITZ_SERVICE_URL || 'http://localhost:4403';
export const POSTS_SERVICE_URL = process.env.POSTS_SERVICE_URL || 'http://localhost:4404';
export const INTERACTION_SERVICE_URL = process.env.INTERACTION_SERVICE_URL || 'http://localhost:4405';
export const CORS_ORIGIN = process.env.CORS_ORIGIN || 'http://localhost:4400';
export const PORT = process.env.PORT || 4400;

