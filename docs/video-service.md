# Video Service API Documentation

Full-length video management service for the Lambrk platform.

**Base URL**: `http://localhost:3102` (Direct) or `http://localhost:3100/api/videos` (Gateway)

## Table of Contents

- [Overview](#overview)
- [Endpoints](#endpoints)
- [Video Quality Management](#video-quality-management)
- [Data Models](#data-models)
- [Examples](#examples)

## Overview

The Video Service handles:
- Video CRUD operations
- Multiple quality versions (4K, 2K, HD, 720p, 480p, 360p, Auto)
- View counting
- Like/dislike integration
- Video status management (draft, published, archived, deleted)
- Processing status tracking (pending, queued, processing, completed, failed)
- Video metadata (format, codec, resolution, bitrate, frame rate)
- Content organization (category, tags, privacy settings)
- Live streaming support
- Scheduled publishing

## Endpoints

### 1. Create Video

Upload/create a new video.

**Endpoint:** `POST /api/videos`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "My New Video",
  "description": "Video description",
  "url": "https://example.com/videos/video.mp4",
  "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
  "duration": 3600,
  "status": "draft",
  "processingStatus": "pending",
  "fileSize": 104857600,
  "format": "mp4",
  "codec": "h264",
  "resolutionWidth": 1920,
  "resolutionHeight": 1080,
  "bitrate": 5000,
  "frameRate": 30.0,
  "category": "Technology",
  "tags": ["tech", "tutorial", "coding"],
  "privacy": "public",
  "isLive": false,
  "liveStreamUrl": null,
  "publishedAt": null,
  "scheduledPublishAt": null,
  "language": "en",
  "location": "San Francisco, CA"
}
```

**Validation Rules:**
- `title`: Required, non-empty string
- `url`: Required, valid URL
- `description`: Optional string
- `thumbnailUrl`: Optional, valid URL
- `duration`: Optional integer (seconds)
- `status`: Optional, one of: 'draft', 'published', 'archived', 'deleted'
- `processingStatus`: Optional, one of: 'pending', 'queued', 'processing', 'completed', 'failed' (default: 'pending')
- `fileSize`: Optional integer (bytes)
- `format`: Optional string, max 50 characters
- `codec`: Optional string, max 50 characters
- `resolutionWidth`: Optional integer (pixels)
- `resolutionHeight`: Optional integer (pixels)
- `bitrate`: Optional integer (bits per second)
- `frameRate`: Optional float (frames per second)
- `category`: Optional string, max 100 characters
- `tags`: Optional array of strings
- `privacy`: Optional, one of: 'public', 'unlisted', 'private' (default: 'public')
- `isLive`: Optional boolean (default: false)
- `liveStreamUrl`: Optional, valid URL
- `publishedAt`: Optional ISO 8601 date string
- `scheduledPublishAt`: Optional ISO 8601 date string
- `language`: Optional string, max 10 characters (ISO 639-1 code)
- `location`: Optional string, max 255 characters

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "video": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My New Video",
      "description": "Video description",
      "url": "https://example.com/videos/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
      "duration": 3600,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 0,
      "likes": 0,
      "dislikes": 0,
      "status": "draft",
      "processingStatus": "pending",
      "fileSize": 104857600,
      "format": "mp4",
      "codec": "h264",
      "resolutionWidth": 1920,
      "resolutionHeight": 1080,
      "bitrate": 5000,
      "frameRate": 30.0,
      "category": "Technology",
      "tags": ["tech", "tutorial", "coding"],
      "privacy": "public",
      "isLive": false,
      "liveStreamUrl": null,
      "publishedAt": null,
      "scheduledPublishAt": null,
      "language": "en",
      "location": "San Francisco, CA",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get All Videos

Retrieve a paginated list of videos.

**Endpoint:** `GET /api/videos`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of videos per page (default: 20)
- `offset`: Pagination offset (default: 0)
- `status`: Filter by status: 'draft', 'published', 'archived', 'deleted'

**Example:**
```
GET /api/videos?limit=10&offset=0&status=published
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "videos": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "My Awesome Video",
        "description": "Great video about technology",
        "url": "https://example.com/videos/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 3600,
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 1250,
        "likes": 89,
        "dislikes": 5,
        "status": "published",
        "processingStatus": "completed",
        "fileSize": 104857600,
        "format": "mp4",
        "codec": "h264",
        "resolutionWidth": 1920,
        "resolutionHeight": 1080,
        "bitrate": 5000,
        "frameRate": 30.0,
        "category": "Technology",
        "tags": ["tech", "tutorial"],
        "privacy": "public",
        "isLive": false,
        "publishedAt": "2024-01-01T12:00:00.000Z",
        "language": "en",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 3. Get User's Videos

Get all videos created by the authenticated user.

**Endpoint:** `GET /api/videos/my-videos`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of videos per page (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "videos": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "My Video",
        "description": "My video description",
        "url": "https://example.com/videos/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 1800,
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 50,
        "likes": 5,
        "dislikes": 1,
        "status": "draft",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 4. Get Video by ID

Retrieve a specific video by its ID.

**Endpoint:** `GET /api/videos/:id`

**Authentication:** Not required

**Query Parameters (Optional):**
- `quality`: Get specific quality version (e.g., '720p')
- `includeQualities`: Include all available qualities (true/false)

**Example:**
```
GET /api/videos/550e8400-e29b-41d4-a716-446655440000
GET /api/videos/550e8400-e29b-41d4-a716-446655440000?quality=720p
GET /api/videos/550e8400-e29b-41d4-a716-446655440000?includeQualities=true
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "video": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My Awesome Video",
      "description": "Great video about technology",
      "url": "https://example.com/videos/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
      "duration": 3600,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 1250,
      "likes": 89,
      "dislikes": 5,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    },
    "quality": null,
    "qualities": []
  }
}
```

---

### 5. Update Video

Update an existing video. Users can only update their own videos.

**Endpoint:** `PUT /api/videos/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "Updated Video Title",
  "description": "Updated description",
  "thumbnailUrl": "https://example.com/thumbnails/new-thumb.jpg",
  "status": "published",
  "processingStatus": "completed",
  "category": "Entertainment",
  "tags": ["funny", "comedy"],
  "privacy": "public",
  "publishedAt": "2024-01-02T10:00:00.000Z"
}
```

**Note:** All fields are optional. Only provided fields will be updated.

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "video": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Updated Video Title",
      "description": "Updated description",
      "url": "https://example.com/videos/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/new-thumb.jpg",
      "duration": 3600,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 1250,
      "likes": 89,
      "dislikes": 5,
      "status": "published",
      "processingStatus": "completed",
      "category": "Entertainment",
      "tags": ["funny", "comedy"],
      "privacy": "public",
      "publishedAt": "2024-01-02T10:00:00.000Z",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-02T00:00:00.000Z"
    }
  }
}
```

**Error Response (403):**
```json
{
  "success": false,
  "error": {
    "message": "You can only update your own videos",
    "statusCode": 403
  }
}
```

---

### 6. Delete Video

Delete a video. Users can only delete their own videos.

**Endpoint:** `DELETE /api/videos/:id`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Video deleted successfully"
}
```

---

### 7. Increment Views

Increment the view count for a video.

**Endpoint:** `POST /api/videos/:id/views`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Views incremented"
}
```

---

### 8. Update Processing Status

Update the processing status of a video. Useful for video processing workflows.

**Endpoint:** `PUT /api/videos/:id/processing-status`

**Authentication:** Required

**Request Body:**
```json
{
  "processingStatus": "processing"
}
```

**Processing Status Values:**
- `pending`: Video is waiting to be processed
- `queued`: Video is in processing queue
- `processing`: Video is currently being processed
- `completed`: Video processing completed successfully
- `failed`: Video processing failed

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "video": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My Video",
      "processingStatus": "processing",
      "status": "draft",
      "updatedAt": "2024-01-01T12:00:00.000Z"
    }
  }
}
```

**Example Workflow:**
```javascript
// 1. Create video with pending status
const video = await createVideo({ title: "My Video", url: "...", processingStatus: "pending" });

// 2. Update to queued when processing starts
await updateProcessingStatus(video.id, { processingStatus: "queued" });

// 3. Update to processing
await updateProcessingStatus(video.id, { processingStatus: "processing" });

// 4. Update to completed when done
await updateProcessingStatus(video.id, { processingStatus: "completed" });
```

---

### 9. Increment Likes (Deprecated)

**Note:** This endpoint is deprecated. Use the [Interaction Service Likes API](./interaction-service.md#likesdislikes-api) instead for like/dislike functionality.

**Endpoint:** `POST /api/videos/:id/likes`

---

## Video Quality Management

The video service supports multiple quality versions for adaptive streaming.

### Supported Quality Types

- `144p`, `240p`, `360p`, `480p`, `720p`, `1080p`, `1440p`, `2160p` (4K), `original`

### 1. Add Video Quality

Add a new quality version to a video.

**Endpoint:** `POST /api/videos/:videoId/qualities`

**Authentication:** Required

**Request Body:**
```json
{
  "quality": "720p",
  "url": "https://example.com/videos/video-720p.mp4",
  "fileSize": 104857600,
  "bitrate": 5000,
  "resolutionWidth": 1280,
  "resolutionHeight": 720,
  "codec": "h264",
  "container": "mp4",
  "duration": 3600,
  "isDefault": true,
  "status": "ready"
}
```

**Validation:**
- `quality`: Required, one of the supported quality types
- `url`: Required, valid URL
- `status`: Optional, one of: 'processing', 'ready', 'failed'

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "quality": {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "videoId": "550e8400-e29b-41d4-a716-446655440000",
      "quality": "720p",
      "url": "https://example.com/videos/video-720p.mp4",
      "fileSize": 104857600,
      "bitrate": 5000,
      "resolutionWidth": 1280,
      "resolutionHeight": 720,
      "codec": "h264",
      "container": "mp4",
      "duration": 3600,
      "isDefault": true,
      "status": "ready",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get All Video Qualities

Get all quality versions for a video.

**Endpoint:** `GET /api/videos/:videoId/qualities`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "qualities": [
      {
        "id": "770e8400-e29b-41d4-a716-446655440000",
        "videoId": "550e8400-e29b-41d4-a716-446655440000",
        "quality": "720p",
        "url": "https://example.com/videos/video-720p.mp4",
        "isDefault": true,
        "status": "ready"
      },
      {
        "id": "880e8400-e29b-41d4-a716-446655440000",
        "videoId": "550e8400-e29b-41d4-a716-446655440000",
        "quality": "1080p",
        "url": "https://example.com/videos/video-1080p.mp4",
        "isDefault": false,
        "status": "ready"
      }
    ]
  }
}
```

---

### 3. Get Specific Video Quality

Get a specific quality version by quality type.

**Endpoint:** `GET /api/videos/:videoId/qualities/:quality`

**Authentication:** Not required

**Example:**
```
GET /api/videos/550e8400-e29b-41d4-a716-446655440000/qualities/720p
```

---

### 4. Update Video Quality

Update a video quality version.

**Endpoint:** `PUT /api/videos/:videoId/qualities/:qualityId`

**Authentication:** Required

**Request Body:**
```json
{
  "status": "ready",
  "bitrate": 6000
}
```

---

### 5. Delete Video Quality

Delete a video quality version.

**Endpoint:** `DELETE /api/videos/:videoId/qualities/:qualityId`

**Authentication:** Required

---

### 6. Set Default Quality

Set a quality version as the default for a video.

**Endpoint:** `POST /api/videos/:videoId/qualities/:qualityId/set-default`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "quality": {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "videoId": "550e8400-e29b-41d4-a716-446655440000",
      "quality": "720p",
      "isDefault": true,
      "status": "ready"
    }
  },
  "message": "Default quality set successfully"
}
```

---

## Data Models

### Video Model

```typescript
interface Video {
  id: string;
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  views: number;
  likes: number;
  dislikes: number;
  status: 'draft' | 'published' | 'archived' | 'deleted';
  processingStatus: 'pending' | 'queued' | 'processing' | 'completed' | 'failed';
  fileSize?: number;
  format?: string;
  codec?: string;
  resolutionWidth?: number;
  resolutionHeight?: number;
  bitrate?: number;
  frameRate?: number;
  category?: string;
  tags?: string[];
  privacy: 'public' | 'unlisted' | 'private';
  isLive: boolean;
  liveStreamUrl?: string;
  publishedAt?: Date;
  scheduledPublishAt?: Date;
  language?: string;
  location?: string;
  createdAt: Date;
  updatedAt: Date;
}
```

**Field Descriptions:**

- **status**: Video publication status
  - `draft`: Not published yet
  - `published`: Publicly available
  - `archived`: Archived but still accessible
  - `deleted`: Soft deleted (can be restored)

- **processingStatus**: Video processing pipeline status
  - `pending`: Waiting to be processed
  - `queued`: In processing queue
  - `processing`: Currently being processed
  - `completed`: Processing finished successfully
  - `failed`: Processing failed

- **privacy**: Video visibility
  - `public`: Visible to everyone
  - `unlisted`: Accessible via direct link only
  - `private`: Only visible to owner

- **tags**: Array of tags for categorization and search

- **scheduledPublishAt**: Schedule video to be published at a specific time

- **isLive**: Indicates if this is a live stream

- **language**: ISO 639-1 language code (e.g., 'en', 'es', 'fr')

### VideoQuality Model

```typescript
interface VideoQuality {
  id: string;
  videoId: string;
  quality: '144p' | '240p' | '360p' | '480p' | '720p' | '1080p' | '1440p' | '2160p' | 'original';
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
```

---

## Examples

### Complete Video Upload Flow

```javascript
// 1. Create video (initially draft with pending processing)
const createResponse = await fetch('http://localhost:3100/api/videos', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    title: 'My Video',
    description: 'An amazing video about technology',
    url: 'https://example.com/original.mp4',
    thumbnailUrl: 'https://example.com/thumb.jpg',
    duration: 600,
    status: 'draft',
    processingStatus: 'pending',
    category: 'Technology',
    tags: ['tech', 'tutorial', 'coding'],
    privacy: 'public',
    language: 'en'
  })
});

const { data: { video } } = await createResponse.json();

// 2. Update processing status as video is processed
await fetch(`http://localhost:3100/api/videos/${video.id}/processing-status`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    processingStatus: 'processing'
  })
});

// 3. Add multiple quality versions
const qualities = ['360p', '720p', '1080p'];
for (const quality of qualities) {
  await fetch(`http://localhost:3100/api/videos/${video.id}/qualities`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      quality,
      url: `https://example.com/${quality}.mp4`,
      status: 'ready',
      isDefault: quality === '720p'
    })
  });
}

// 4. Update processing status to completed
await fetch(`http://localhost:3100/api/videos/${video.id}/processing-status`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    processingStatus: 'completed'
  })
});

// 5. Publish video
await fetch(`http://localhost:3100/api/videos/${video.id}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: 'published',
    publishedAt: new Date().toISOString()
  })
});
```

### Track Video View

```javascript
// When user starts watching
await fetch(`http://localhost:3100/api/videos/${videoId}/views`, {
  method: 'POST'
});
```

### Schedule Video Publishing

```javascript
// Create video and schedule it to be published later
const video = await createVideo({
  title: 'Scheduled Video',
  url: 'https://example.com/video.mp4',
  status: 'draft',
  scheduledPublishAt: '2024-12-25T00:00:00.000Z' // Christmas Day
});

// Later, when scheduled time arrives, update status
await updateVideo(video.id, {
  status: 'published',
  publishedAt: new Date().toISOString()
});
```

### Create Live Stream Video

```javascript
// Create a live stream video
const liveVideo = await createVideo({
  title: 'Live Stream',
  url: 'https://example.com/stream.m3u8',
  isLive: true,
  liveStreamUrl: 'https://example.com/live-stream',
  status: 'published',
  privacy: 'public'
});
```

### Update Video Metadata After Processing

```javascript
// After video processing completes, update metadata
await updateVideo(videoId, {
  processingStatus: 'completed',
  fileSize: 104857600,
  format: 'mp4',
  codec: 'h264',
  resolutionWidth: 1920,
  resolutionHeight: 1080,
  bitrate: 5000,
  frameRate: 30.0
});
```

---

## Rate Limiting

- **Create/Update/Delete**: 10 operations per hour per user
- **Read Operations**: 100 requests per 15 minutes per IP

---

[Back to Documentation Index](./README.md)
