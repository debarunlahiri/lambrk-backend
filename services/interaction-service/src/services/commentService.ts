import { CommentModel, CreateCommentData, UpdateCommentData, Comment } from '../models/Comment';
import { ContentType } from '../models/Like';
import { NotFoundError, ForbiddenError } from '@lambrk/shared';

export class CommentService {
  private commentModel: CommentModel;

  constructor() {
    this.commentModel = new CommentModel();
  }

  async createComment(data: CreateCommentData): Promise<Comment> {
    return this.commentModel.create(data);
  }

  async getComment(id: string): Promise<Comment> {
    const comment = await this.commentModel.findById(id);
    if (!comment) {
      throw new NotFoundError('Comment not found');
    }
    return comment;
  }

  async getContentComments(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Comment[]> {
    return this.commentModel.findByContent(contentType, contentId, limit, offset);
  }

  async getCommentReplies(parentCommentId: string, limit: number = 20, offset: number = 0): Promise<Comment[]> {
    return this.commentModel.findReplies(parentCommentId, limit, offset);
  }

  async updateComment(id: string, userId: string, data: UpdateCommentData): Promise<Comment> {
    const comment = await this.commentModel.findById(id);
    if (!comment) {
      throw new NotFoundError('Comment not found');
    }

    if (comment.userId !== userId) {
      throw new ForbiddenError('You can only update your own comments');
    }

    return this.commentModel.update(id, userId, data);
  }

  async deleteComment(id: string, userId: string): Promise<void> {
    const comment = await this.commentModel.findById(id);
    if (!comment) {
      throw new NotFoundError('Comment not found');
    }

    if (comment.userId !== userId) {
      throw new ForbiddenError('You can only delete your own comments');
    }

    const deleted = await this.commentModel.delete(id, userId);
    if (!deleted) {
      throw new NotFoundError('Comment not found');
    }
  }

  async getCommentCount(contentType: ContentType, contentId: string): Promise<number> {
    return this.commentModel.getCount(contentType, contentId);
  }
}
