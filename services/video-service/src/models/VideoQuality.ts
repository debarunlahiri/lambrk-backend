import { getPool } from '@lambrk/shared';
import { VideoQueries } from '../queries/videoQueries';

export type VideoQualityType = '144p' | '240p' | '360p' | '480p' | '720p' | '1080p' | '1440p' | '2160p' | 'original';

export interface VideoQuality {
  id: string;
  videoId: string;
  quality: VideoQualityType;
  url: string;
  fileSize?: number;
  bitrate?: number;
  resolutionWidth?: number;
  resolutionHeight?: number;
  codec?: string;
  container?: string;
  duration?: number;
  isDefault: boolean;
  status: 'processing' | 'ready' | 'failed';
  createdAt: Date;
  updatedAt: Date;
}

export interface CreateVideoQualityData {
  videoId: string;
  quality: VideoQualityType;
  url: string;
  fileSize?: number;
  bitrate?: number;
  resolutionWidth?: number;
  resolutionHeight?: number;
  codec?: string;
  container?: string;
  duration?: number;
  isDefault?: boolean;
  status?: 'processing' | 'ready' | 'failed';
}

export interface UpdateVideoQualityData {
  quality?: VideoQualityType;
  url?: string;
  fileSize?: number;
  bitrate?: number;
  resolutionWidth?: number;
  resolutionHeight?: number;
  codec?: string;
  container?: string;
  duration?: number;
  isDefault?: boolean;
  status?: 'processing' | 'ready' | 'failed';
}

import { VideoQualityQueries } from '../queries/videoQualityQueries';

export class VideoQualityModel {
  private queries: VideoQualityQueries | null = null;

  private getQueries(): VideoQualityQueries {
    if (!this.queries) {
      this.queries = new VideoQualityQueries(getPool());
    }
    return this.queries;
  }

  async create(data: CreateVideoQualityData): Promise<VideoQuality> {
    return this.getQueries().create(data);
  }

  async findById(id: string): Promise<VideoQuality | null> {
    return this.getQueries().findById(id);
  }

  async findByVideoId(videoId: string): Promise<VideoQuality[]> {
    return this.getQueries().findByVideoId(videoId);
  }

  async findDefaultByVideoId(videoId: string): Promise<VideoQuality | null> {
    return this.getQueries().findDefaultByVideoId(videoId);
  }

  async findByVideoIdAndQuality(videoId: string, quality: VideoQualityType): Promise<VideoQuality | null> {
    return this.getQueries().findByVideoIdAndQuality(videoId, quality);
  }

  async update(id: string, data: UpdateVideoQualityData): Promise<VideoQuality> {
    return this.getQueries().update(id, data);
  }

  async delete(id: string): Promise<boolean> {
    return this.getQueries().delete(id);
  }

  async setAsDefault(id: string, videoId: string): Promise<VideoQuality> {
    return this.getQueries().setAsDefault(id, videoId);
  }
}

