import { BitzModel, CreateBitzData, UpdateBitzData, Bitz } from '../models/Bitz';
import { NotFoundError, ForbiddenError } from '@lambrk/shared';

export class BitzService {
  private bitzModel: BitzModel;

  constructor() {
    this.bitzModel = new BitzModel();
  }

  async createBitz(data: CreateBitzData): Promise<Bitz> {
    return this.bitzModel.create(data);
  }

  async getBitzById(id: string): Promise<Bitz> {
    const bitz = await this.bitzModel.findById(id);
    if (!bitz) {
      throw new NotFoundError('Bitz not found');
    }
    return bitz;
  }

  async getUserBitz(userId: string, limit: number = 20, offset: number = 0): Promise<Bitz[]> {
    return this.bitzModel.findByUserId(userId, limit, offset);
  }

  async getAllBitz(limit: number = 20, offset: number = 0, status?: string): Promise<Bitz[]> {
    return this.bitzModel.findAll(limit, offset, status);
  }

  async updateBitz(id: string, userId: string, data: UpdateBitzData): Promise<Bitz> {
    const bitz = await this.bitzModel.findById(id);
    if (!bitz) {
      throw new NotFoundError('Bitz not found');
    }

    if (bitz.userId !== userId) {
      throw new ForbiddenError('You can only update your own bitz');
    }

    return this.bitzModel.update(id, userId, data);
  }

  async deleteBitz(id: string, userId: string): Promise<void> {
    const bitz = await this.bitzModel.findById(id);
    if (!bitz) {
      throw new NotFoundError('Bitz not found');
    }

    if (bitz.userId !== userId) {
      throw new ForbiddenError('You can only delete your own bitz');
    }

    const deleted = await this.bitzModel.delete(id, userId);
    if (!deleted) {
      throw new NotFoundError('Bitz not found');
    }
  }

  async incrementViews(id: string): Promise<void> {
    await this.bitzModel.incrementViews(id);
  }
}
