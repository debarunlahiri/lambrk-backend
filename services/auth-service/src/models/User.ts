import { getPool } from '@lambrk/shared';
import { UserQueries } from '../queries/userQueries';

export interface User {
  id: string;
  username: string;
  email: string;
  password?: string;
  googleId?: string;
  firstName?: string;
  lastName?: string;
  avatar?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateUserData {
  username: string;
  email: string;
  password?: string;
  googleId?: string;
  firstName?: string;
  lastName?: string;
  avatar?: string;
}

export class UserModel {
  private queries: UserQueries | null = null;

  private getQueries(): UserQueries {
    if (!this.queries) {
      this.queries = new UserQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateUserData): Promise<User> {
    return this.getQueries().create(data);
  }

  async findByEmail(email: string): Promise<User | null> {
    return this.getQueries().findByEmail(email);
  }

  async findByUsername(username: string): Promise<User | null> {
    return this.getQueries().findByUsername(username);
  }

  async findByGoogleId(googleId: string): Promise<User | null> {
    return this.getQueries().findByGoogleId(googleId);
  }

  async findById(id: string): Promise<User | null> {
    return this.getQueries().findById(id);
  }

  async update(id: string, data: Partial<CreateUserData>): Promise<User> {
    return this.getQueries().update(id, data);
  }
}

