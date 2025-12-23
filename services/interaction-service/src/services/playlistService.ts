import { PlaylistModel, CreatePlaylistData, UpdatePlaylistData, Playlist, PlaylistItem, AddPlaylistItemData } from '../models/Playlist';
import { ContentType } from '../models/Like';
import { NotFoundError, ForbiddenError } from '@lambrk/shared';

export class PlaylistService {
  private playlistModel: PlaylistModel;

  constructor() {
    this.playlistModel = new PlaylistModel();
  }

  async createPlaylist(data: CreatePlaylistData): Promise<Playlist> {
    return this.playlistModel.create(data);
  }

  async getPlaylist(id: string): Promise<Playlist> {
    const playlist = await this.playlistModel.findById(id);
    if (!playlist) {
      throw new NotFoundError('Playlist not found');
    }
    return playlist;
  }

  async getUserPlaylists(userId: string, limit: number = 20, offset: number = 0): Promise<Playlist[]> {
    return this.playlistModel.findByUserId(userId, limit, offset);
  }

  async getOrCreateWatchLater(userId: string): Promise<Playlist> {
    const watchLater = await this.playlistModel.findWatchLater(userId);
    if (!watchLater) {
      return this.playlistModel.create({
        userId,
        name: 'Watch Later',
        isPublic: false,
        isWatchLater: true,
      });
    }
    return watchLater;
  }

  async updatePlaylist(id: string, userId: string, data: UpdatePlaylistData): Promise<Playlist> {
    const playlist = await this.playlistModel.findById(id);
    if (!playlist) {
      throw new NotFoundError('Playlist not found');
    }

    if (playlist.userId !== userId) {
      throw new ForbiddenError('You can only update your own playlists');
    }

    if (playlist.isWatchLater) {
      throw new ForbiddenError('Cannot update Watch Later playlist');
    }

    return this.playlistModel.update(id, userId, data);
  }

  async deletePlaylist(id: string, userId: string): Promise<void> {
    const playlist = await this.playlistModel.findById(id);
    if (!playlist) {
      throw new NotFoundError('Playlist not found');
    }

    if (playlist.userId !== userId) {
      throw new ForbiddenError('You can only delete your own playlists');
    }

    if (playlist.isWatchLater) {
      throw new ForbiddenError('Cannot delete Watch Later playlist');
    }

    const deleted = await this.playlistModel.delete(id, userId);
    if (!deleted) {
      throw new NotFoundError('Playlist not found');
    }
  }

  async addToPlaylist(playlistId: string, userId: string, contentType: ContentType, contentId: string): Promise<PlaylistItem> {
    const playlist = await this.playlistModel.findById(playlistId);
    if (!playlist) {
      throw new NotFoundError('Playlist not found');
    }

    if (playlist.userId !== userId) {
      throw new ForbiddenError('You can only add items to your own playlists');
    }

    return this.playlistModel.addItem({ playlistId, contentType, contentId });
  }

  async removeFromPlaylist(playlistId: string, userId: string, contentType: ContentType, contentId: string): Promise<void> {
    const playlist = await this.playlistModel.findById(playlistId);
    if (!playlist) {
      throw new NotFoundError('Playlist not found');
    }

    if (playlist.userId !== userId) {
      throw new ForbiddenError('You can only remove items from your own playlists');
    }

    const removed = await this.playlistModel.removeItem(playlistId, contentType, contentId);
    if (!removed) {
      throw new NotFoundError('Item not found in playlist');
    }
  }

  async getPlaylistItems(playlistId: string, limit: number = 50, offset: number = 0): Promise<PlaylistItem[]> {
    const playlist = await this.playlistModel.findById(playlistId);
    if (!playlist) {
      throw new NotFoundError('Playlist not found');
    }

    return this.playlistModel.getItems(playlistId, limit, offset);
  }

  async checkItemInPlaylist(playlistId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    return this.playlistModel.checkItemExists(playlistId, contentType, contentId);
  }
}
