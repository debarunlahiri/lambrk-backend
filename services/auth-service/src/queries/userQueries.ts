import { Pool } from 'pg';
import { CreateUserData, User } from '../models/User';

export class UserQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreateUserData): Promise<User> {
    const query = `
      INSERT INTO users (username, email, password, google_id, first_name, last_name, avatar)
      VALUES ($1, $2, $3, $4, $5, $6, $7)
      RETURNING id, username, email, password, google_id, first_name, last_name, avatar, created_at, updated_at
    `;

    const values = [
      data.username,
      data.email,
      data.password || null,
      data.googleId || null,
      data.firstName || null,
      data.lastName || null,
      data.avatar || null,
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToUser(result.rows[0]);
  }

  async findByEmail(email: string): Promise<User | null> {
    const query = `
      SELECT id, username, email, password, google_id, first_name, last_name, avatar, created_at, updated_at
      FROM users
      WHERE email = $1
    `;

    const result = await this.pool.query(query, [email]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToUser(result.rows[0]);
  }

  async findByUsername(username: string): Promise<User | null> {
    const query = `
      SELECT id, username, email, password, google_id, first_name, last_name, avatar, created_at, updated_at
      FROM users
      WHERE username = $1
    `;

    const result = await this.pool.query(query, [username]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToUser(result.rows[0]);
  }

  async findByGoogleId(googleId: string): Promise<User | null> {
    const query = `
      SELECT id, username, email, password, google_id, first_name, last_name, avatar, created_at, updated_at
      FROM users
      WHERE google_id = $1
    `;

    const result = await this.pool.query(query, [googleId]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToUser(result.rows[0]);
  }

  async findById(id: string): Promise<User | null> {
    const query = `
      SELECT id, username, email, password, google_id, first_name, last_name, avatar, created_at, updated_at
      FROM users
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToUser(result.rows[0]);
  }

  async update(id: string, data: Partial<CreateUserData>): Promise<User> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramCount = 1;

    if (data.firstName !== undefined) {
      updates.push(`first_name = $${paramCount++}`);
      values.push(data.firstName);
    }
    if (data.lastName !== undefined) {
      updates.push(`last_name = $${paramCount++}`);
      values.push(data.lastName);
    }
    if (data.avatar !== undefined) {
      updates.push(`avatar = $${paramCount++}`);
      values.push(data.avatar);
    }
    if (data.password !== undefined) {
      updates.push(`password = $${paramCount++}`);
      values.push(data.password);
    }
    if (data.googleId !== undefined) {
      updates.push(`google_id = $${paramCount++}`);
      values.push(data.googleId);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id);

    const query = `
      UPDATE users
      SET ${updates.join(', ')}
      WHERE id = $${paramCount}
      RETURNING id, username, email, password, google_id, first_name, last_name, avatar, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    return this.mapRowToUser(result.rows[0]);
  }

  private mapRowToUser(row: any): User {
    return {
      id: row.id,
      username: row.username,
      email: row.email,
      password: row.password,
      googleId: row.google_id,
      firstName: row.first_name,
      lastName: row.last_name,
      avatar: row.avatar,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }
}

