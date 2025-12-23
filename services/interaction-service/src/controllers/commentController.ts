import { Request, Response, NextFunction } from 'express';
import { CommentService } from '../services/commentService';
import { body, validationResult } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType } from '../models/Like';

const commentService = new CommentService();

export const createComment = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { contentType, contentId, parentCommentId, commentText } = req.body;

    const comment = await commentService.createComment({
      userId,
      contentType: contentType as ContentType,
      contentId,
      parentCommentId,
      commentText,
    });

    res.status(201).json({
      success: true,
      data: { comment },
    });
  } catch (error) {
    next(error);
  }
};

export const getComment = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const comment = await commentService.getComment(id);

    res.status(200).json({
      success: true,
      data: { comment },
    });
  } catch (error) {
    next(error);
  }
};

export const getContentComments = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const comments = await commentService.getContentComments(contentType as ContentType, contentId, limit, offset);

    res.status(200).json({
      success: true,
      data: { comments },
    });
  } catch (error) {
    next(error);
  }
};

export const getCommentReplies = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { commentId } = req.params;
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const replies = await commentService.getCommentReplies(commentId, limit, offset);

    res.status(200).json({
      success: true,
      data: { replies },
    });
  } catch (error) {
    next(error);
  }
};

export const updateComment = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { id } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { commentText } = req.body;

    const comment = await commentService.updateComment(id, userId, { commentText });

    res.status(200).json({
      success: true,
      data: { comment },
    });
  } catch (error) {
    next(error);
  }
};

export const deleteComment = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    await commentService.deleteComment(id, userId);

    res.status(200).json({
      success: true,
      message: 'Comment deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

export const getCommentCount = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;

    const count = await commentService.getCommentCount(contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      data: { count },
    });
  } catch (error) {
    next(error);
  }
};

export const createCommentValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
  body('parentCommentId').optional().isUUID().withMessage('Valid parent comment ID is required'),
  body('commentText').trim().notEmpty().withMessage('Comment text is required'),
];

export const updateCommentValidation = [
  body('commentText').trim().notEmpty().withMessage('Comment text is required'),
];
