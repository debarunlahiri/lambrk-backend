# Lambrk Backend API Documentation - Consolidated Reference

> **📚 Primary Documentation**: This is a consolidated single-file reference. For better navigation and the most up-to-date documentation, please use the **[organized documentation by service](./docs/README.md)**.
>
> **Quick Links**:
> - [Auth Service](./docs/auth-service.md)
> - [Video Service](./docs/video-service.md)
> - [Bitz Service](./docs/bitz-service.md)
> - [Posts Service](./docs/posts-service.md)
> - [Interaction Service](./docs/interaction-service.md)
> - [Compression Service](./docs/compression-service.md)

---

This file provides a complete API reference in a single document for offline reading or searching. For better organization and navigation, use the individual service documentation files in the `docs/` directory.

Complete API reference for Lambrk video streaming platform backend services.

## Table of Contents

- [Base URLs](#base-urls)
- [Authentication](#authentication)
- [Error Handling](#error-handling)
- [Auth Service API](#auth-service-api)
- [Video Service API](#video-service-api)
- [Bitz Service API](#bitz-service-api)
- [Posts Service API](#posts-service-api)
- [Interaction Service API](#interaction-service-api)
  - [Likes/Dislikes](#likesdislikes-api)
  - [Comments](#comments-api)
  - [Playlists](#playlists-api)
  - [Subscriptions](#subscriptions-api)
  - [Downloads](#downloads-api)
  - [Trending](#trending-api)
  - [Recommendations](#recommendations-api)
- [Compression Service API](#compression-service-api)

## Base URLs

- **API Gateway**: `http://localhost:4400`
- **Auth Service (Direct)**: `http://localhost:4401`
- **Video Service (Direct)**: `http://localhost:4402`
- **Bitz Service (Direct)**: `http://localhost:4403`
- **Posts Service (Direct)**: `http://localhost:4404`
- **Interaction Service (Direct)**: `http://localhost:4405`
- **Compression Service (Direct)**: `http://localhost:4500`

> **Note**: All requests should go through the API Gateway unless accessing services directly for development.

## Authentication

Most endpoints require authentication using JWT tokens. Include the token in the Authorization header:

```
Authorization: Bearer <access_token>
```

### Token Types

- **Access Token**: Short-lived token (default: 7 days) used for API requests
- **Refresh Token**: Long-lived token (default: 30 days) used to obtain new access tokens

## Error Handling

All error responses follow this format:

```json
{
  "success": false,
  "error": {
    "message": "Error description",
    "statusCode": 400
  }
}
```

---

## Bitz Service API

Bitz are short vertical videos similar to TikTok/Instagram Reels.

### 1. Create Bitz

**Endpoint:** `POST /api/bitz`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "My Awesome Bitz",
  "description": "Short video description",
  "url": "https://example.com/bitz/video.mp4",
  "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
  "duration": 30,
  "status": "published"
}
```

### 2. Get All Bitz

**Endpoint:** `GET /api/bitz`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)
- `status`: Filter by status (draft/published/processing)

### 3. Get User's Bitz

**Endpoint:** `GET /api/bitz/my-bitz`

**Authentication:** Required

### 4. Get Bitz by ID

**Endpoint:** `GET /api/bitz/:id`

### 5. Update Bitz

**Endpoint:** `PUT /api/bitz/:id`

**Authentication:** Required

### 6. Delete Bitz

**Endpoint:** `DELETE /api/bitz/:id`

**Authentication:** Required

### 7. Increment Bitz Views

**Endpoint:** `POST /api/bitz/:id/views`

---

## Posts Service API

### 1. Create Post

**Endpoint:** `POST /api/posts`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "My Post Title",
  "content": "Post content here...",
  "imageUrl": "https://example.com/images/post.jpg",
  "status": "published"
}
```

### 2. Get All Posts

**Endpoint:** `GET /api/posts`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)
- `status`: Filter by status (draft/published)

### 3. Get User's Posts

**Endpoint:** `GET /api/posts/my-posts`

**Authentication:** Required

### 4. Get Post by ID

**Endpoint:** `GET /api/posts/:id`

### 5. Update Post

**Endpoint:** `PUT /api/posts/:id`

**Authentication:** Required

### 6. Delete Post

**Endpoint:** `DELETE /api/posts/:id`

**Authentication:** Required

### 7. Increment Post Views

**Endpoint:** `POST /api/posts/:id/views`

---

## Interaction Service API

### Likes/Dislikes API

#### 1. Toggle Like/Dislike

**Endpoint:** `POST /api/likes`

**Authentication:** Required

**Request Body:**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "likeType": "like"
}
```

**Content Types:** `video`, `bitz`, `post`

**Like Types:** `like`, `dislike`

**Response:**
```json
{
  "success": true,
  "data": {
    "action": "added",
    "stats": {
      "likes": 10,
      "dislikes": 2,
      "userLikeType": "like"
    }
  }
}
```

**Actions:** `added`, `updated`, `removed`

#### 2. Get Like Stats

**Endpoint:** `GET /api/likes/:contentType/:contentId`

**Response:**
```json
{
  "success": true,
  "data": {
    "stats": {
      "likes": 10,
      "dislikes": 2,
      "userLikeType": "like"
    }
  }
}
```

#### 3. Get User's Liked Content

**Endpoint:** `GET /api/likes/user/:contentType`

**Authentication:** Required

**Query Parameters:**
- `likeType`: Filter by like/dislike (default: like)
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

---

### Comments API

#### 1. Create Comment

**Endpoint:** `POST /api/comments`

**Authentication:** Required

**Request Body:**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "commentText": "Great video!",
  "parentCommentId": null
}
```

#### 2. Get Content Comments

**Endpoint:** `GET /api/comments/:contentType/:contentId`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

#### 3. Get Comment Replies

**Endpoint:** `GET /api/comments/:commentId/replies`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

#### 4. Update Comment

**Endpoint:** `PUT /api/comments/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "commentText": "Updated comment text"
}
```

#### 5. Delete Comment

**Endpoint:** `DELETE /api/comments/:id`

**Authentication:** Required

#### 6. Get Comment Count

**Endpoint:** `GET /api/comments/:contentType/:contentId/count`

---

### Playlists API

#### 1. Create Playlist

**Endpoint:** `POST /api/playlists`

**Authentication:** Required

**Request Body:**
```json
{
  "name": "My Playlist",
  "description": "My favorite videos",
  "isPublic": true
}
```

#### 2. Get User's Playlists

**Endpoint:** `GET /api/playlists/my-playlists`

**Authentication:** Required

#### 3. Get Watch Later Playlist

**Endpoint:** `GET /api/playlists/watch-later`

**Authentication:** Required

**Note:** Auto-creates if doesn't exist

#### 4. Get Playlist by ID

**Endpoint:** `GET /api/playlists/:id`

#### 5. Update Playlist

**Endpoint:** `PUT /api/playlists/:id`

**Authentication:** Required

**Note:** Cannot update Watch Later playlist

#### 6. Delete Playlist

**Endpoint:** `DELETE /api/playlists/:id`

**Authentication:** Required

**Note:** Cannot delete Watch Later playlist

#### 7. Add Item to Playlist

**Endpoint:** `POST /api/playlists/:id/items`

**Authentication:** Required

**Request Body:**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 8. Remove Item from Playlist

**Endpoint:** `DELETE /api/playlists/:id/items/:contentType/:contentId`

**Authentication:** Required

#### 9. Get Playlist Items

**Endpoint:** `GET /api/playlists/:id/items`

**Query Parameters:**
- `limit`: Number of items (default: 50)
- `offset`: Pagination offset (default: 0)

#### 10. Check Item in Playlist

**Endpoint:** `GET /api/playlists/:id/items/:contentType/:contentId/check`

**Response:**
```json
{
  "success": true,
  "data": {
    "exists": true
  }
}
```

---

### Subscriptions API

#### 1. Subscribe to Channel

**Endpoint:** `POST /api/subscriptions`

**Authentication:** Required

**Request Body:**
```json
{
  "channelId": "660e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "subscribed": true,
    "subscriberCount": 1250
  }
}
```

#### 2. Unsubscribe from Channel

**Endpoint:** `DELETE /api/subscriptions/:channelId`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "data": {
    "unsubscribed": true,
    "subscriberCount": 1249
  }
}
```

#### 3. Check Subscription Status

**Endpoint:** `GET /api/subscriptions/check/:channelId`

**Authentication:** Required

**Response:**
```json
{
  "success": true,
  "data": {
    "subscribed": true
  }
}
```

#### 4. Get User's Subscriptions

**Endpoint:** `GET /api/subscriptions/my-subscriptions`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of items (default: 50)
- `offset`: Pagination offset (default: 0)

**Response:**
```json
{
  "success": true,
  "data": {
    "channelIds": ["uuid1", "uuid2", "uuid3"]
  }
}
```

#### 5. Get Channel Subscribers

**Endpoint:** `GET /api/subscriptions/channel/:channelId/subscribers`

**Query Parameters:**
- `limit`: Number of items (default: 50)
- `offset`: Pagination offset (default: 0)

#### 6. Get Subscriber Count

**Endpoint:** `GET /api/subscriptions/channel/:channelId/count`

**Response:**
```json
{
  "success": true,
  "data": {
    "count": 1250
  }
}
```

---

### Downloads API

#### 1. Create Download

**Endpoint:** `POST /api/downloads`

**Authentication:** Required

**Request Body:**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "downloadUrl": "https://example.com/downloads/video.mp4",
  "fileSize": 104857600,
  "status": "completed"
}
```

**Status:** `pending`, `completed`, `failed`

#### 2. Get User's Downloads

**Endpoint:** `GET /api/downloads/my-downloads`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

#### 3. Get Download by ID

**Endpoint:** `GET /api/downloads/:id`

#### 4. Update Download

**Endpoint:** `PUT /api/downloads/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "status": "completed",
  "fileSize": 104857600
}
```

#### 5. Delete Download

**Endpoint:** `DELETE /api/downloads/:id`

**Authentication:** Required

---

### Trending API

#### 1. Get Trending Videos

**Endpoint:** `GET /api/trending/videos`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

**Response:**
```json
{
  "success": true,
  "data": {
    "videos": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "Trending Video",
        "views": 50000,
        "likes": 2500,
        "dislikes": 100,
        "trendingScore": 125000.5,
        "isTrending": true,
        ...
      }
    ]
  }
}
```

#### 2. Get Trending Bitz

**Endpoint:** `GET /api/trending/bitz`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

#### 3. Get Trending Posts

**Endpoint:** `GET /api/trending/posts`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

#### 4. Refresh Trending Data

**Endpoint:** `POST /api/trending/refresh`

---

### Recommendations API

Personalized content recommendation system using advanced algorithmic scoring.

The recommendation system uses a weighted scoring algorithm:
- **Engagement Score (40%)**: Views, likes, comments, shares
- **Recency Score (25%)**: Exponential decay based on publication time
- **Relevance Score (20%)**: Personalization based on user behavior
- **Quality Score (15%)**: Like/dislike ratios and engagement metrics

#### 1. Get Recommended Videos

**Endpoint:** `GET /api/recommendations/videos`

**Authentication:** Required

**Query Parameters:**
- `currentVideoId` (optional): UUID of currently watching video
- `limit` (optional): Number of recommendations (default: 20)

**Response:**
```json
{
  "success": true,
  "data": {
    "videos": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "title": "Recommended Video",
        "views": 50000,
        "likes": 2500,
        "dislikes": 50,
        "score": 0.875,
        ...
      }
    ],
    "count": 20
  }
}
```

#### 2. Get Recommended Posts

**Endpoint:** `GET /api/recommendations/posts`

**Authentication:** Required

**Query Parameters:**
- `currentPostId` (optional): UUID of currently viewing post
- `limit` (optional): Number of recommendations (default: 15)

#### 3. Get Recommended Bitz

**Endpoint:** `GET /api/recommendations/bitz`

**Authentication:** Required

**Query Parameters:**
- `currentBitzId` (optional): UUID of currently viewing bitz
- `limit` (optional): Number of recommendations (default: 10)

#### 4. Get Trending Recommendations

**Endpoint:** `GET /api/recommendations/trending/:contentType`

**Authentication:** Not required

**Path Parameters:**
- `contentType`: `video`, `bitz`, or `post`

**Query Parameters:**
- `timeWindow` (optional): `24h`, `7d`, or `30d` (default: `7d`)
- `limit` (optional): Number of recommendations (default: 10)

**Examples:**
- `GET /api/recommendations/trending/video?timeWindow=24h&limit=10`
- `GET /api/recommendations/trending/post?timeWindow=7d&limit=20`

**Note:** For detailed algorithm explanation and examples, see [Interaction Service Documentation](./docs/interaction-service.md#recommendations-api).

---

## Compression Service API

Video compression service that creates multiple quality versions and uploads to S3.

**Base URL**: `http://localhost:4400/api/compression` (Gateway) or `http://localhost:4500/api/compression` (Direct)

### 1. Compress Single Video

**Endpoint:** `POST /api/compression/compress`

**Authentication:** Not required (can be added for production)

**Request Body:**
```json
{
  "video_id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "my_video.mp4",
  "video_url_base": "https://example.com/videos"
}
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `video_id` | string (UUID) | Yes | UUID of the video record in the database |
| `filename` | string | Yes | Name of the video file in the pending directory |
| `video_url_base` | string | No | Base URL for fallback (default: "https://example.com/videos") |

**Success Response (200):**
```json
{
  "success": true,
  "message": "Compression job started",
  "video_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 2. Batch Compress Videos

**Endpoint:** `POST /api/compression/compress/batch`

**Authentication:** Not required

**Request Body:**
```json
{
  "videos": [
    {
      "video_id": "550e8400-e29b-41d4-a716-446655440000",
      "filename": "video1.mp4",
      "video_url_base": "https://example.com/videos"
    },
    {
      "video_id": "660e8400-e29b-41d4-a716-446655440001",
      "filename": "video2.mp4"
    }
  ],
  "max_workers": 4
}
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `videos` | array | Yes | Array of video compression requests |
| `videos[].video_id` | string (UUID) | Yes | UUID of the video record |
| `videos[].filename` | string | Yes | Name of the video file |
| `videos[].video_url_base` | string | No | Base URL for fallback |
| `max_workers` | integer | No | Maximum parallel workers (default: 4) |

**Success Response (200):**
```json
{
  "success": true,
  "total": 2,
  "success_count": 0,
  "failed_count": 0,
  "results": []
}
```

---

### 3. Get Video Qualities

**Endpoint:** `GET /api/compression/video/{video_id}/qualities`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "qualities": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "video_id": "550e8400-e29b-41d4-a716-446655440000",
      "quality": "720p",
      "url": "https://lam-brk.s3.ap-south-1.amazonaws.com/videos/550e8400-e29b-41d4-a716-446655440000/my_video_720p.mp4",
      "file_size": 104857600,
      "bitrate": 5000,
      "resolution_width": 1280,
      "resolution_height": 720,
      "codec": "h264",
      "container": "mp4",
      "duration": 3600,
      "is_default": true,
      "status": "ready",
      "created_at": "2024-01-01T00:00:00.000Z",
      "updated_at": "2024-01-01T00:00:00.000Z"
    }
  ]
}
```

---

### 4. Get Video Status

**Endpoint:** `GET /api/compression/video/{video_id}/status`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "video_id": "550e8400-e29b-41d4-a716-446655440000",
  "video_status": "published",
  "qualities": {
    "144p": "ready",
    "240p": "ready",
    "360p": "ready",
    "480p": "ready",
    "720p": "ready",
    "1080p": "ready",
    "original": "ready"
  }
}
```

---

### 5. Health Check

**Endpoint:** `GET /api/compression/health`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "status": "healthy",
  "database": "connected",
  "pending_dir": "/Volumes/Expansion/Lambrk/pending",
  "completed_dir": "/Volumes/Expansion/Lambrk/completed"
}
```

**Note:** All compression jobs run asynchronously in the background. Video files must be placed in the `PENDING_DIR` before starting compression.

For detailed API documentation, request/response examples, error codes, and complete data models, see [Compression Service Documentation](./docs/compression-service.md).

---

## Data Models

### ContentType Enum

```typescript
type ContentType = 'video' | 'bitz' | 'post';
```

### LikeType Enum

```typescript
type LikeType = 'like' | 'dislike';
```

### Video Model

```typescript
{
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
  status: 'draft' | 'published' | 'processing';
  createdAt: Date;
  updatedAt: Date;
}
```

### Bitz Model

```typescript
{
  id: string;
  title: string;
  description?: string;
  url: string;
  thumbnailUrl?: string;
  duration?: number;
  userId: string;
  views: number;
  status: 'draft' | 'published' | 'processing';
  createdAt: Date;
  updatedAt: Date;
}
```

### Post Model

```typescript
{
  id: string;
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  views: number;
  status: 'draft' | 'published';
  createdAt: Date;
  updatedAt: Date;
}
```

### Comment Model

```typescript
{
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  parentCommentId?: string;
  commentText: string;
  createdAt: Date;
  updatedAt: Date;
}
```

### Playlist Model

```typescript
{
  id: string;
  userId: string;
  name: string;
  description?: string;
  isPublic: boolean;
  isWatchLater: boolean;
  createdAt: Date;
  updatedAt: Date;
}
```

### PlaylistItem Model

```typescript
{
  id: string;
  playlistId: string;
  contentType: ContentType;
  contentId: string;
  position: number;
  createdAt: Date;
}
```

### Subscription Model

```typescript
{
  id: string;
  subscriberId: string;
  channelId: string;
  createdAt: Date;
}
```

### Download Model

```typescript
{
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  downloadUrl: string;
  fileSize?: number;
  status: 'pending' | 'completed' | 'failed';
  createdAt: Date;
  updatedAt: Date;
}
```

---

## Rate Limiting

Rate limiting is implemented to protect the API from abuse:

- **Gateway**: 300 requests per 15 minutes per IP
- **Auth Service**: 100 requests per 15 minutes per IP
- **Auth Endpoints** (signup/signin): 5 requests per 15 minutes per IP
- **All Services** (read operations): 100 requests per 15 minutes
- **All Services** (write operations): 50 requests per 15 minutes
- **Video/Bitz Operations** (create/update/delete): 10-20 operations per hour

When rate limits are exceeded, you'll receive a `429 Too Many Requests` response.

---

## Integration Examples

### Like/Dislike a Video

```javascript
// Toggle like
const response = await fetch('http://localhost:4400/api/likes', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  },
  body: JSON.stringify({
    contentType: 'video',
    contentId: 'video-uuid',
    likeType: 'like'
  })
});

const data = await response.json();
// data.data.action will be 'added', 'updated', or 'removed'
// data.data.stats will contain current like/dislike counts
```

### Add to Playlist

```javascript
// Get or create Watch Later playlist
const watchLaterResponse = await fetch('http://localhost:4400/api/playlists/watch-later', {
  headers: {
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  }
});

const { playlist } = (await watchLaterResponse.json()).data;

// Add video to Watch Later
const addResponse = await fetch(`http://localhost:4400/api/playlists/${playlist.id}/items`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  },
  body: JSON.stringify({
    contentType: 'video',
    contentId: 'video-uuid'
  })
});
```

### Subscribe to Channel

```javascript
const response = await fetch('http://localhost:4400/api/subscriptions', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  },
  body: JSON.stringify({
    channelId: 'channel-user-uuid'
  })
});

const data = await response.json();
// data.data.subscribed will be true
// data.data.subscriberCount will contain current subscriber count
```

### Add Comment

```javascript
const response = await fetch('http://localhost:4400/api/comments', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer YOUR_ACCESS_TOKEN'
  },
  body: JSON.stringify({
    contentType: 'video',
    contentId: 'video-uuid',
    commentText: 'Great video!',
    parentCommentId: null // or comment-uuid for replies
  })
});
```

---

## Database Schema

### Key Features

- **Universal Likes System**: Single `likes` table handles likes/dislikes for all content types
- **Comments System**: Supports nested comments (replies) with `parent_comment_id`
- **Flexible Playlists**: Can contain any content type (videos, bitz, posts)
- **Materialized Views**: Trending content is pre-computed for performance
- **Automatic Timestamps**: All tables have `created_at` and `updated_at` with triggers

### Trending Algorithm

Trending score is calculated as:
```
score = (views * 0.5) + (likes * 2) - (dislikes * 1.5)
```

Content is considered trending if:
- Created within the last 7 days
- Status is 'published'
- Has significant engagement

Trending views should be refreshed periodically (hourly recommended) using:
```sql
SELECT refresh_trending_views();
```

---

## Support

For issues or questions, please refer to the main README.md file or contact the development team.
