import { Request, Response, NextFunction } from 'express';
import { ReportService } from '../services/reportService';
import { body, validationResult, query } from 'express-validator';
import { AuthRequest } from '@lambrk/shared';
import { ContentType, ReportStatus } from '../models/Report';

const reportService = new ReportService();

export const createReport = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { contentType, contentId, reason, description } = req.body;

    const report = await reportService.createReport({
      userId,
      contentType: contentType as ContentType,
      contentId,
      reason,
      description,
    });

    res.status(201).json({
      success: true,
      data: { report },
    });
  } catch (error) {
    next(error);
  }
};

export const getReport = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { id } = req.params;
    const report = await reportService.getReport(id);

    res.status(200).json({
      success: true,
      data: { report },
    });
  } catch (error) {
    next(error);
  }
};

export const updateReport = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
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

    const { id } = req.params;
    const { status, reviewedBy } = req.body;

    const report = await reportService.updateReport(id, {
      status: status as ReportStatus,
      reviewedBy: reviewedBy || userId,
    });

    res.status(200).json({
      success: true,
      data: { report },
    });
  } catch (error) {
    next(error);
  }
};

export const getContentReports = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const reports = await reportService.getContentReports(
      contentType as ContentType,
      contentId,
      limit,
      offset
    );

    res.status(200).json({
      success: true,
      data: { reports },
    });
  } catch (error) {
    next(error);
  }
};

export const getUserReports = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const reports = await reportService.getUserReports(userId, limit, offset);

    res.status(200).json({
      success: true,
      data: { reports },
    });
  } catch (error) {
    next(error);
  }
};

export const getReportsByStatus = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { status } = req.params;
    const limit = parseInt(req.query.limit as string) || 20;
    const offset = parseInt(req.query.offset as string) || 0;

    const reports = await reportService.getReportsByStatus(status as ReportStatus, limit, offset);

    res.status(200).json({
      success: true,
      data: { reports },
    });
  } catch (error) {
    next(error);
  }
};

export const getReportCount = async (req: Request, res: Response, next: NextFunction): Promise<void> => {
  try {
    const { contentType, contentId } = req.params;

    const count = await reportService.getReportCount(contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      data: { count },
    });
  } catch (error) {
    next(error);
  }
};

export const checkUserReported = async (req: AuthRequest, res: Response, next: NextFunction): Promise<void> => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      res.status(401).json({
        success: false,
        error: { message: 'Unauthorized' },
      });
      return;
    }

    const { contentType, contentId } = req.params;

    const reported = await reportService.checkUserReported(userId, contentType as ContentType, contentId);

    res.status(200).json({
      success: true,
      data: { reported },
    });
  } catch (error) {
    next(error);
  }
};

export const createReportValidation = [
  body('contentType').isIn(['video', 'bitz', 'post']).withMessage('Invalid content type'),
  body('contentId').isUUID().withMessage('Valid content ID is required'),
  body('reason').notEmpty().withMessage('Reason is required').isLength({ max: 100 }).withMessage('Reason must be less than 100 characters'),
  body('description').optional().isString().isLength({ max: 1000 }).withMessage('Description must be less than 1000 characters'),
];

export const updateReportValidation = [
  body('status').isIn(['pending', 'reviewed', 'resolved', 'dismissed']).withMessage('Invalid status'),
  body('reviewedBy').optional().isUUID().withMessage('Valid reviewer ID is required'),
];

