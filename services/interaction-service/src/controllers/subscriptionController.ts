import { Request, Response, NextFunction } from 'express';
import { SubscriptionService } from '../services/subscriptionService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';

const subscriptionService = new SubscriptionService();

export const subscribe = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
      res.status(400).json({
        success: false,
        error: {
          message: 'Validation failed',
          errors: errors.array(),
        },
      });
      return;
    }

    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { channelId } = req.body;

    const result = await subscriptionService.subscribe(userId, channelId);

    res.status(201).json({
      success: true,
      data: result,
    });
  } catch (error) {
    next(error);
  }
};

export const unsubscribe = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { channelId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const result = await subscriptionService.unsubscribe(userId, channelId);

    res.status(200).json({
      success: true,
      data: result,
    });
  } catch (error) {
    next(error);
  }
};

export const checkSubscription = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { channelId } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const subscribed = await subscriptionService.checkSubscription(userId, channelId);

    res.status(200).json({
      success: true,
      data: { subscribed },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserSubscriptions = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const limit = parseInt(req.query.limit as string) || 50;
    const offset = parseInt(req.query.offset as string) || 0;

    const channelIds = await subscriptionService.getUserSubscriptions(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { channelIds },
    });
  } catch (error) {
    next(error);
  }
};

export const getChannelSubscribers = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { channelId } = req.params;
    const limit = parseInt(req.query.limit as string) || 50;
    const offset = parseInt(req.query.offset as string) || 0;

    const subscriberIds = await subscriptionService.getChannelSubscribers(channelId, limit, offset);

    res.status(200).json({
      success: true,
      data: { subscriberIds },
    });
  } catch (error) {
    next(error);
  }
};

export const getSubscriberCount = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { channelId } = req.params;

    const count = await subscriptionService.getSubscriberCount(channelId);

    res.status(200).json({
      success: true,
      data: { count },
    });
  } catch (error) {
    next(error);
  }
};

export const subscribeValidation = [
  body('channelId').isUUID().withMessage('Valid channel ID is required'),
];
