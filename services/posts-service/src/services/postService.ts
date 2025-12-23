import { PostModel, CreatePostData, UpdatePostData, Post } from '../models/Post';
import { NotFoundError, ForbiddenError } from '@lambrk/shared';

export class PostService {
  private postModel: PostModel;

  constructor() {
    this.postModel = new PostModel();
  }

  async createPost(data: CreatePostData): Promise<Post> {
    return this.postModel.create(data);
  }

  async getPostById(id: string): Promise<Post> {
    const post = await this.postModel.findById(id);
    if (!post) {
      throw new NotFoundError('Post not found');
    }
    return post;
  }

  async getUserPosts(userId: string, limit: number = 20, offset: number = 0): Promise<Post[]> {
    return this.postModel.findByUserId(userId, limit, offset);
  }

  async getAllPosts(limit: number = 20, offset: number = 0, status?: string): Promise<Post[]> {
    return this.postModel.findAll(limit, offset, status);
  }

  async updatePost(id: string, userId: string, data: UpdatePostData): Promise<Post> {
    const post = await this.postModel.findById(id);
    if (!post) {
      throw new NotFoundError('Post not found');
    }

    if (post.userId !== userId) {
      throw new ForbiddenError('You can only update your own posts');
    }

    return this.postModel.update(id, userId, data);
  }

  async deletePost(id: string, userId: string): Promise<void> {
    const post = await this.postModel.findById(id);
    if (!post) {
      throw new NotFoundError('Post not found');
    }

    if (post.userId !== userId) {
      throw new ForbiddenError('You can only delete your own posts');
    }

    const deleted = await this.postModel.delete(id, userId);
    if (!deleted) {
      throw new NotFoundError('Post not found');
    }
  }

  async incrementViews(id: string): Promise<void> {
    await this.postModel.incrementViews(id);
  }
}
