import { SubscriptionModel, CreateSubscriptionData } from '../models/Subscription';
import { ConflictError } from '@lambrk/shared';

export class SubscriptionService {
  private subscriptionModel: SubscriptionModel;

  constructor() {
    this.subscriptionModel = new SubscriptionModel();
  }

  async subscribe(subscriberId: string, channelId: string): Promise<{ subscribed: boolean; subscriberCount: number }> {
    if (subscriberId === channelId) {
      throw new ConflictError('Cannot subscribe to yourself');
    }

    try {
      await this.subscriptionModel.create({ subscriberId, channelId });
      const subscriberCount = await this.subscriptionModel.getSubscriberCount(channelId);
      return { subscribed: true, subscriberCount };
    } catch (error) {
      if (error instanceof Error && error.message === 'Already subscribed') {
        throw new ConflictError('Already subscribed to this channel');
      }
      throw error;
    }
  }

  async unsubscribe(subscriberId: string, channelId: string): Promise<{ unsubscribed: boolean; subscriberCount: number }> {
    const removed = await this.subscriptionModel.remove(subscriberId, channelId);
    if (!removed) {
      throw new ConflictError('Not subscribed to this channel');
    }
    const subscriberCount = await this.subscriptionModel.getSubscriberCount(channelId);
    return { unsubscribed: true, subscriberCount };
  }

  async checkSubscription(subscriberId: string, channelId: string): Promise<boolean> {
    return this.subscriptionModel.checkSubscription(subscriberId, channelId);
  }

  async getUserSubscriptions(subscriberId: string, limit: number = 50, offset: number = 0): Promise<string[]> {
    return this.subscriptionModel.getSubscriptions(subscriberId, limit, offset);
  }

  async getChannelSubscribers(channelId: string, limit: number = 50, offset: number = 0): Promise<string[]> {
    return this.subscriptionModel.getSubscribers(channelId, limit, offset);
  }

  async getSubscriberCount(channelId: string): Promise<number> {
    return this.subscriptionModel.getSubscriberCount(channelId);
  }

  async getSubscriptionCount(subscriberId: string): Promise<number> {
    return this.subscriptionModel.getSubscriptionCount(subscriberId);
  }
}
