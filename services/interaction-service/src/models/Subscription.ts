import { getPool } from '@lambrk/shared';
import { SubscriptionQueries } from '../queries/subscriptionQueries';

export interface Subscription {
  id: string;
  subscriberId: string;
  channelId: string;
  createdAt: Date;
}

export interface CreateSubscriptionData {
  subscriberId: string;
  channelId: string;
}

export class SubscriptionModel {
  private queries: SubscriptionQueries | null = null;

  private getQueries(): SubscriptionQueries {
    if (!this.queries) {
      this.queries = new SubscriptionQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateSubscriptionData): Promise<Subscription> {
    return this.getQueries().create(data);
  }

  async remove(subscriberId: string, channelId: string): Promise<boolean> {
    return this.getQueries().remove(subscriberId, channelId);
  }

  async checkSubscription(subscriberId: string, channelId: string): Promise<boolean> {
    return this.getQueries().checkSubscription(subscriberId, channelId);
  }

  async getSubscriptions(subscriberId: string, limit: number = 50, offset: number = 0): Promise<string[]> {
    return this.getQueries().getSubscriptions(subscriberId, limit, offset);
  }

  async getSubscribers(channelId: string, limit: number = 50, offset: number = 0): Promise<string[]> {
    return this.getQueries().getSubscribers(channelId, limit, offset);
  }

  async getSubscriberCount(channelId: string): Promise<number> {
    return this.getQueries().getSubscriberCount(channelId);
  }

  async getSubscriptionCount(subscriberId: string): Promise<number> {
    return this.getQueries().getSubscriptionCount(subscriberId);
  }
}
