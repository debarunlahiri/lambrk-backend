import { VideoModel, CreateVideoData, UpdateVideoData, Video } from '../models/Video';
import { VideoQualityModel, CreateVideoQualityData, UpdateVideoQualityData, VideoQuality, VideoQualityType } from '../models/VideoQuality';
import { NotFoundError, ForbiddenError } from '@lambrk/shared';

export class VideoService {
  private videoModel: VideoModel;
  private videoQualityModel: VideoQualityModel;

  constructor() {
    this.videoModel = new VideoModel();
    this.videoQualityModel = new VideoQualityModel();
  }

  async createVideo(data: CreateVideoData): Promise<Video> {
    return this.videoModel.create(data);
  }

  async getVideoById(id: string): Promise<Video> {
    const video = await this.videoModel.findById(id);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    return video;
  }

  async getUserVideos(userId: string, limit: number = 20, offset: number = 0): Promise<Video[]> {
    return this.videoModel.findByUserId(userId, limit, offset);
  }

  async getAllVideos(limit: number = 20, offset: number = 0, status?: string): Promise<Video[]> {
    return this.videoModel.findAll(limit, offset, status);
  }

  async updateVideo(id: string, userId: string, data: UpdateVideoData): Promise<Video> {
    const video = await this.videoModel.findById(id);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    if (video.userId !== userId) {
      throw new ForbiddenError('You can only update your own videos');
    }

    return this.videoModel.update(id, userId, data);
  }

  async deleteVideo(id: string, userId: string): Promise<void> {
    const video = await this.videoModel.findById(id);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    if (video.userId !== userId) {
      throw new ForbiddenError('You can only delete your own videos');
    }

    const deleted = await this.videoModel.delete(id, userId);
    if (!deleted) {
      throw new NotFoundError('Video not found');
    }
  }

  async incrementViews(id: string): Promise<void> {
    await this.videoModel.incrementViews(id);
  }

  async incrementLikes(id: string): Promise<void> {
    await this.videoModel.incrementLikes(id);
  }

  async updateProcessingStatus(id: string, userId: string, processingStatus: 'pending' | 'queued' | 'processing' | 'completed' | 'failed'): Promise<Video> {
    const video = await this.videoModel.findById(id);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    if (video.userId !== userId) {
      throw new ForbiddenError('You can only update processing status of your own videos');
    }

    return this.videoModel.update(id, userId, { processingStatus });
  }

  // Video Quality Methods
  async addVideoQuality(data: CreateVideoQualityData): Promise<VideoQuality> {
    // Verify video exists
    const video = await this.videoModel.findById(data.videoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    return this.videoQualityModel.create(data);
  }

  async getVideoQualities(videoId: string): Promise<VideoQuality[]> {
    const video = await this.videoModel.findById(videoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    return this.videoQualityModel.findByVideoId(videoId);
  }

  async getVideoQuality(videoId: string, quality?: VideoQualityType): Promise<VideoQuality> {
    const video = await this.videoModel.findById(videoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }

    if (quality) {
      const videoQuality = await this.videoQualityModel.findByVideoIdAndQuality(videoId, quality);
      if (!videoQuality) {
        throw new NotFoundError(`Video quality '${quality}' not found for this video`);
      }
      return videoQuality;
    } else {
      const defaultQuality = await this.videoQualityModel.findDefaultByVideoId(videoId);
      if (!defaultQuality) {
        throw new NotFoundError('No default quality found for this video');
      }
      return defaultQuality;
    }
  }

  async updateVideoQuality(id: string, videoId: string, userId: string, data: UpdateVideoQualityData): Promise<VideoQuality> {
    // Verify video exists and user owns it
    const video = await this.videoModel.findById(videoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    if (video.userId !== userId) {
      throw new ForbiddenError('You can only update qualities of your own videos');
    }

    const quality = await this.videoQualityModel.findById(id);
    if (!quality || quality.videoId !== videoId) {
      throw new NotFoundError('Video quality not found');
    }

    return this.videoQualityModel.update(id, data);
  }

  async deleteVideoQuality(id: string, videoId: string, userId: string): Promise<void> {
    // Verify video exists and user owns it
    const video = await this.videoModel.findById(videoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    if (video.userId !== userId) {
      throw new ForbiddenError('You can only delete qualities of your own videos');
    }

    const quality = await this.videoQualityModel.findById(id);
    if (!quality || quality.videoId !== videoId) {
      throw new NotFoundError('Video quality not found');
    }

    const deleted = await this.videoQualityModel.delete(id);
    if (!deleted) {
      throw new NotFoundError('Video quality not found');
    }
  }

  async setDefaultQuality(id: string, videoId: string, userId: string): Promise<VideoQuality> {
    // Verify video exists and user owns it
    const video = await this.videoModel.findById(videoId);
    if (!video) {
      throw new NotFoundError('Video not found');
    }
    if (video.userId !== userId) {
      throw new ForbiddenError('You can only set default quality for your own videos');
    }

    const quality = await this.videoQualityModel.findById(id);
    if (!quality || quality.videoId !== videoId) {
      throw new NotFoundError('Video quality not found');
    }

    return this.videoQualityModel.setAsDefault(id, videoId);
  }
}

