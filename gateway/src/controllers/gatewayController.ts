import { Request, Response } from 'express';
import { AUTH_SERVICE_URL, VIDEO_SERVICE_URL, BITZ_SERVICE_URL, POSTS_SERVICE_URL, INTERACTION_SERVICE_URL, COMPRESSION_SERVICE_URL } from '../config/proxy';

export const getHealth = (req: Request, res: Response) => {
  res.json({
    success: true,
    message: 'API Gateway is running',
    services: {
      auth: AUTH_SERVICE_URL,
      video: VIDEO_SERVICE_URL,
      bitz: BITZ_SERVICE_URL,
      posts: POSTS_SERVICE_URL,
      interaction: INTERACTION_SERVICE_URL,
      compression: COMPRESSION_SERVICE_URL,
    },
    timestamp: new Date().toISOString(),
  });
};

