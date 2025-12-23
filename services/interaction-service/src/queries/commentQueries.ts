import { Pool } from 'pg';
import { CreateCommentData, UpdateCommentData, Comment } from '../models/Comment';
import { ContentType } from '../models/Like';

export class CommentQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateCommentData): Promise<Comment> {
    const query = `
      INSERT INTO comments (user_id, content_type, content_id, parent_comment_id, comment_text)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id, user_id, content_type, content_id, parent_comment_id, comment_text, created_at, updated_at
    `;

    const values = [
      data.userId,
      data.contentType,
      data.contentId,
      data.parentCommentId || null,
      data.commentText,
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToComment(result.rows[0]);
  }

  async findById(id: string): Promise<Comment | null> {
    const query = `
      SELECT id, user_id, content_type, content_id, parent_comment_id, comment_text, created_at, updated_at
      FROM comments
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToComment(result.rows[0]);
  }

  async findByContent(contentType: ContentType, contentId: string, limit: number = 20, offset: number = 0): Promise<Comment[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, parent_comment_id, comment_text, created_at, updated_at
      FROM comments
      WHERE content_type = $1 AND content_id = $2 AND parent_comment_id IS NULL
      ORDER BY created_at DESC
      LIMIT $3 OFFSET $4
    `;

    const result = await this.pool.query(query, [contentType, contentId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToComment(row));
  }

  async findReplies(parentCommentId: string, limit: number = 20, offset: number = 0): Promise<Comment[]> {
    const query = `
      SELECT id, user_id, content_type, content_id, parent_comment_id, comment_text, created_at, updated_at
      FROM comments
      WHERE parent_comment_id = $1
      ORDER BY created_at ASC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [parentCommentId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToComment(row));
  }

  async update(id: string, userId: string, data: UpdateCommentData): Promise<Comment> {
    const query = `
      UPDATE comments
      SET comment_text = $1, updated_at = NOW()
      WHERE id = $2 AND user_id = $3
      RETURNING id, user_id, content_type, content_id, parent_comment_id, comment_text, created_at, updated_at
    `;

    const result = await this.pool.query(query, [data.commentText, id, userId]);
    if (result.rows.length === 0) {
      throw new Error('Comment not found or unauthorized');
    }
    return this.mapRowToComment(result.rows[0]);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    const query = `
      DELETE FROM comments
      WHERE id = $1 AND user_id = $2
    `;

    const result = await this.pool.query(query, [id, userId]);
    return (result.rowCount ?? 0) > 0;
  }

  async getCount(contentType: ContentType, contentId: string): Promise<number> {
    const query = `
      SELECT COUNT(*) as count
      FROM comments
      WHERE content_type = $1 AND content_id = $2
    `;

    const result = await this.pool.query(query, [contentType, contentId]);
    return parseInt(result.rows[0].count) || 0;
  }

  private mapRowToComment(row: any): Comment {
    return {
      id: row.id,
      userId: row.user_id,
      contentType: row.content_type,
      contentId: row.content_id,
      parentCommentId: row.parent_comment_id,
      commentText: row.comment_text,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}
