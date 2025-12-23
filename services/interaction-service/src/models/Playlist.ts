import { getPool } from '@lambrk/shared';
import { PlaylistQueries } from '../queries/playlistQueries';
import { ContentType } from './Like';

export interface Playlist {
  id: string;
  userId: string;
  name: string;
  description?: string;
  isPublic: boolean;
  isWatchLater: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface CreatePlaylistData {
  userId: string;
  name: string;
  description?: string;
  isPublic?: boolean;
  isWatchLater?: boolean;
}

export interface UpdatePlaylistData {
  name?: string;
  description?: string;
  isPublic?: boolean;
}

export interface PlaylistItem {
  id: string;
  playlistId: string;
  contentType: ContentType;
  contentId: string;
  position: number;
  createdAt: Date;
}

export interface AddPlaylistItemData {
  playlistId: string;
  contentType: ContentType;
  contentId: string;
  position?: number;
}

export class PlaylistModel {
  private queries: PlaylistQueries | null = null;

  private getQueries(): PlaylistQueries {
    if (!this.queries) {
      this.queries = new PlaylistQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreatePlaylistData): Promise<Playlist> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<Playlist | null> {
    return this.getQueries().findById(id);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Playlist[]> {
    return this.getQueries().findByUserId(userId, limit, offset);
  }

  async findWatchLater(userId: string): Promise<Playlist | null> {
    return this.getQueries().findWatchLater(userId);
  }

  async update(id: string, userId: string, data: UpdatePlaylistData): Promise<Playlist> {
    return this.getQueries().update(id, userId, data);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    return this.getQueries().delete(id, userId);
  }

  async addItem(data: AddPlaylistItemData): Promise<PlaylistItem> {
    return this.getQueries().addItem(data);
  }

  async removeItem(playlistId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    return this.getQueries().removeItem(playlistId, contentType, contentId);
  }

  async getItems(playlistId: string, limit: number = 50, offset: number = 0): Promise<PlaylistItem[]> {
    return this.getQueries().getItems(playlistId, limit, offset);
  }

  async checkItemExists(playlistId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    return this.getQueries().checkItemExists(playlistId, contentType, contentId);
  }
}
