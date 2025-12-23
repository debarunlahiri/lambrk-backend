import { Pool } from 'pg';
import { CreateSubscriptionData, Subscription } from '../models/Subscription';

export class SubscriptionQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateSubscriptionData): Promise<Subscription> {
    const query = `
      INSERT INTO subscriptions (subscriber_id, channel_id)
      VALUES ($1, $2)
      ON CONFLICT (subscriber_id, channel_id) DO NOTHING
      RETURNING id, subscriber_id, channel_id, created_at
    `;

    const values = [data.subscriberId, data.channelId];
    const result = await this.pool.query(query, values);
    
    if (result.rows.length === 0) {
      throw new Error('Already subscribed');
    }
    
    return this.mapRowToSubscription(result.rows[0]);
  }

  async remove(subscriberId: string, channelId: string): Promise<boolean> {
    const query = `
      DELETE FROM subscriptions
      WHERE subscriber_id = $1 AND channel_id = $2
    `;

    const result = await this.pool.query(query, [subscriberId, channelId]);
    return (result.rowCount ?? 0) > 0;
  }

  async checkSubscription(subscriberId: string, channelId: string): Promise<boolean> {
    const query = `
      SELECT 1 FROM subscriptions
      WHERE subscriber_id = $1 AND channel_id = $2
    `;

    const result = await this.pool.query(query, [subscriberId, channelId]);
    return result.rows.length > 0;
  }

  async getSubscriptions(subscriberId: string, limit: number = 50, offset: number = 0): Promise<string[]> {
    const query = `
      SELECT channel_id
      FROM subscriptions
      WHERE subscriber_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [subscriberId, limit, offset]);
    return result.rows.map((row: any) => row.channel_id);
  }

  async getSubscribers(channelId: string, limit: number = 50, offset: number = 0): Promise<string[]> {
    const query = `
      SELECT subscriber_id
      FROM subscriptions
      WHERE channel_id = $1
      ORDER BY created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [channelId, limit, offset]);
    return result.rows.map((row: any) => row.subscriber_id);
  }

  async getSubscriberCount(channelId: string): Promise<number> {
    const query = `
      SELECT COUNT(*) as count
      FROM subscriptions
      WHERE channel_id = $1
    `;

    const result = await this.pool.query(query, [channelId]);
    return parseInt(result.rows[0].count) || 0;
  }

  async getSubscriptionCount(subscriberId: string): Promise<number> {
    const query = `
      SELECT COUNT(*) as count
      FROM subscriptions
      WHERE subscriber_id = $1
    `;

    const result = await this.pool.query(query, [subscriberId]);
    return parseInt(result.rows[0].count) || 0;
  }

  private mapRowToSubscription(row: any): Subscription {
    return {
      id: row.id,
      subscriberId: row.subscriber_id,
      channelId: row.channel_id,
      createdAt: row.created_at,
    };
  }
}
