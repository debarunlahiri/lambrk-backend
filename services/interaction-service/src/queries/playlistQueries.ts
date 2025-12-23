import { Pool } from 'pg';
import { CreatePlaylistData, UpdatePlaylistData, Playlist, PlaylistItem, AddPlaylistItemData } from '../models/Playlist';
import { ContentType } from '../models/Like';

export class PlaylistQueries {
  private pool: Pool;

  constructor(pool: Pool) {
    this.pool = pool;
  }

  async create(data: CreatePlaylistData): Promise<Playlist> {
    const query = `
      INSERT INTO playlists (user_id, name, description, is_public, is_watch_later)
      VALUES ($1, $2, $3, $4, $5)
      RETURNING id, user_id, name, description, is_public, is_watch_later, created_at, updated_at
    `;

    const values = [
      data.userId,
      data.name,
      data.description || null,
      data.isPublic !== undefined ? data.isPublic : true,
      data.isWatchLater || false,
    ];

    const result = await this.pool.query(query, values);
    return this.mapRowToPlaylist(result.rows[0]);
  }

  async findById(id: string): Promise<Playlist | null> {
    const query = `
      SELECT id, user_id, name, description, is_public, is_watch_later, created_at, updated_at
      FROM playlists
      WHERE id = $1
    `;

    const result = await this.pool.query(query, [id]);
    if (result.rows.length === 0) {
      return null;
    }
    return this.mapRowToPlaylist(result.rows[0]);
  }

  async findByUserId(userId: string, limit: number = 20, offset: number = 0): Promise<Playlist[]> {
    const query = `
      SELECT id, user_id, name, description, is_public, is_watch_later, created_at, updated_at
      FROM playlists
      WHERE user_id = $1
      ORDER BY is_watch_later DESC, created_at DESC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [userId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToPlaylist(row));
  }

  async findWatchLater(userId: string): Promise<Playlist | null> {
    const query = `
      SELECT id, user_id, name, description, is_public, is_watch_later, created_at, updated_at
      FROM playlists
      WHERE user_id = $1 AND is_watch_later = true
      LIMIT 1
    `;

    const result = await this.pool.query(query, [userId]);
    if (result.rows.length === 0) {
      // Auto-create watch later playlist if it doesn't exist
      return this.create({
        userId,
        name: 'Watch Later',
        isPublic: false,
        isWatchLater: true,
      });
    }
    return this.mapRowToPlaylist(result.rows[0]);
  }

  async update(id: string, userId: string, data: UpdatePlaylistData): Promise<Playlist> {
    const updates: string[] = [];
    const values: any[] = [];
    let paramCount = 1;

    if (data.name !== undefined) {
      updates.push(`name = $${paramCount++}`);
      values.push(data.name);
    }
    if (data.description !== undefined) {
      updates.push(`description = $${paramCount++}`);
      values.push(data.description);
    }
    if (data.isPublic !== undefined) {
      updates.push(`is_public = $${paramCount++}`);
      values.push(data.isPublic);
    }

    updates.push(`updated_at = NOW()`);
    values.push(id, userId);

    const query = `
      UPDATE playlists
      SET ${updates.join(', ')}
      WHERE id = $${paramCount} AND user_id = $${paramCount + 1}
      RETURNING id, user_id, name, description, is_public, is_watch_later, created_at, updated_at
    `;

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Playlist not found or unauthorized');
    }
    return this.mapRowToPlaylist(result.rows[0]);
  }

  async delete(id: string, userId: string): Promise<boolean> {
    const query = `
      DELETE FROM playlists
      WHERE id = $1 AND user_id = $2 AND is_watch_later = false
    `;

    const result = await this.pool.query(query, [id, userId]);
    return (result.rowCount ?? 0) > 0;
  }

  async addItem(data: AddPlaylistItemData): Promise<PlaylistItem> {
    const query = `
      INSERT INTO playlist_items (playlist_id, content_type, content_id, position)
      VALUES ($1, $2, $3, $4)
      ON CONFLICT (playlist_id, content_type, content_id) DO NOTHING
      RETURNING id, playlist_id, content_type, content_id, position, created_at
    `;

    const position = data.position !== undefined ? data.position : await this.getNextPosition(data.playlistId);
    const values = [data.playlistId, data.contentType, data.contentId, position];

    const result = await this.pool.query(query, values);
    if (result.rows.length === 0) {
      throw new Error('Item already exists in playlist');
    }
    return this.mapRowToPlaylistItem(result.rows[0]);
  }

  async removeItem(playlistId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    const query = `
      DELETE FROM playlist_items
      WHERE playlist_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [playlistId, contentType, contentId]);
    return (result.rowCount ?? 0) > 0;
  }

  async getItems(playlistId: string, limit: number = 50, offset: number = 0): Promise<PlaylistItem[]> {
    const query = `
      SELECT id, playlist_id, content_type, content_id, position, created_at
      FROM playlist_items
      WHERE playlist_id = $1
      ORDER BY position ASC
      LIMIT $2 OFFSET $3
    `;

    const result = await this.pool.query(query, [playlistId, limit, offset]);
    return result.rows.map((row: any) => this.mapRowToPlaylistItem(row));
  }

  async checkItemExists(playlistId: string, contentType: ContentType, contentId: string): Promise<boolean> {
    const query = `
      SELECT 1 FROM playlist_items
      WHERE playlist_id = $1 AND content_type = $2 AND content_id = $3
    `;

    const result = await this.pool.query(query, [playlistId, contentType, contentId]);
    return result.rows.length > 0;
  }

  private async getNextPosition(playlistId: string): Promise<number> {
    const query = `
      SELECT COALESCE(MAX(position), -1) + 1 as next_position
      FROM playlist_items
      WHERE playlist_id = $1
    `;

    const result = await this.pool.query(query, [playlistId]);
    return result.rows[0].next_position;
  }

  private mapRowToPlaylist(row: any): Playlist {
    return {
      id: row.id,
      userId: row.user_id,
      name: row.name,
      description: row.description,
      isPublic: row.is_public,
      isWatchLater: row.is_watch_later,
      createdAt: row.created_at,
      updatedAt: row.updated_at,
    };
  }

  private mapRowToPlaylistItem(row: any): PlaylistItem {
    return {
      id: row.id,
      playlistId: row.playlist_id,
      contentType: row.content_type,
      contentId: row.content_id,
      position: row.position,
      createdAt: row.created_at,
    };
  }
}
