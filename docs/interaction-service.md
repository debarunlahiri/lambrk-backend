# Interaction Service API Documentation

User interaction management service for the Lambrk platform.

**Base URL**: `http://localhost:4405` (Direct) or `http://localhost:4400/api` (Gateway)

## Table of Contents

- [Overview](#overview)
- [Likes/Dislikes API](#likesdislikes-api)
- [Comments API](#comments-api)
- [Playlists API](#playlists-api)
- [Subscriptions API](#subscriptions-api)
- [Downloads API](#downloads-api)
- [Trending API](#trending-api)
- [Recommendations API](#recommendations-api)
- [Data Models](#data-models)

## Overview

The Interaction Service is a comprehensive service that handles all user interactions across the platform:

- **Likes/Dislikes**: Universal like/dislike system for all content types
- **Comments**: Nested comments with replies for all content
- **Playlists**: User playlists with Watch Later functionality
- **Subscriptions**: Channel subscription system
- **Downloads**: User download tracking
- **Trending**: Algorithmic trending content
- **Recommendations**: Personalized content recommendation system

### Content Types

All interaction endpoints support these content types:
- `video`: Full-length videos
- `bitz`: Short vertical videos
- `post`: Social media posts

---

## Likes/Dislikes API

Universal like/dislike system for all content types with toggle functionality.

### Features

- Single endpoint for all content types
- Toggle behavior (like → dislike → remove)
- Real-time statistics with user's current state
- Supports videos, bitz, and posts

### 1. Toggle Like/Dislike

Add, update, or remove a like/dislike on any content.

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

**Parameters:**
- `contentType`: `video` | `bitz` | `post`
- `contentId`: UUID of the content
- `likeType`: `like` | `dislike`

**Behavior:**
1. If user hasn't interacted: **Adds** the like/dislike
2. If user has same interaction: **Removes** the like/dislike
3. If user has opposite interaction: **Updates** to new type

**Success Response (200):**
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

---

### 2. Get Like Stats

Get like/dislike statistics for any content.

**Endpoint:** `GET /api/likes/:contentType/:contentId`

**Authentication:** Optional (includes user's like type if authenticated)

**Example:**
```
GET /api/likes/video/550e8400-e29b-41d4-a716-446655440000
```

**Success Response (200):**
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

**Note:** `userLikeType` is `null` if user not authenticated or hasn't interacted.

---

### 3. Get User's Liked Content

Get list of content IDs that user has liked or disliked.

**Endpoint:** `GET /api/likes/user/:contentType`

**Authentication:** Required

**Query Parameters:**
- `likeType`: `like` | `dislike` (default: like)
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

**Example:**
```
GET /api/likes/user/video?likeType=like&limit=20&offset=0
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "contentIds": [
      "550e8400-e29b-41d4-a716-446655440000",
      "660e8400-e29b-41d4-a716-446655440000",
      "770e8400-e29b-41d4-a716-446655440000"
    ]
  }
}
```

---

## Comments API

Nested comments system with replies for all content types.

### Features

- Create, read, update, delete comments
- Nested comments (replies to comments)
- Works on videos, bitz, and posts
- Comment like/dislike support
- Comment count tracking

### 1. Create Comment

Add a comment to any content or reply to a comment.

**Endpoint:** `POST /api/comments`

**Authentication:** Required

**Request Body (Top-level Comment):**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "commentText": "Great video!",
  "parentCommentId": null
}
```

**Request Body (Reply):**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000",
  "commentText": "I agree!",
  "parentCommentId": "880e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "comment": {
      "id": "990e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "contentType": "video",
      "contentId": "550e8400-e29b-41d4-a716-446655440000",
      "parentCommentId": null,
      "commentText": "Great video!",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get Content Comments

Get all top-level comments for content.

**Endpoint:** `GET /api/comments/:contentType/:contentId`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of comments (default: 20)
- `offset`: Pagination offset (default: 0)

**Example:**
```
GET /api/comments/video/550e8400-e29b-41d4-a716-446655440000?limit=20
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "comments": [
      {
        "id": "990e8400-e29b-41d4-a716-446655440000",
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "contentType": "video",
        "contentId": "550e8400-e29b-41d4-a716-446655440000",
        "parentCommentId": null,
        "commentText": "Great video!",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 3. Get Comment Replies

Get all replies to a specific comment.

**Endpoint:** `GET /api/comments/:commentId/replies`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of replies (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "replies": [
      {
        "id": "aa0e8400-e29b-41d4-a716-446655440000",
        "userId": "770e8400-e29b-41d4-a716-446655440000",
        "contentType": "video",
        "contentId": "550e8400-e29b-41d4-a716-446655440000",
        "parentCommentId": "990e8400-e29b-41d4-a716-446655440000",
        "commentText": "I agree!",
        "createdAt": "2024-01-01T01:00:00.000Z",
        "updatedAt": "2024-01-01T01:00:00.000Z"
      }
    ]
  }
}
```

---

### 4. Update Comment

Update your own comment.

**Endpoint:** `PUT /api/comments/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "commentText": "Updated comment text"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "comment": {
      "id": "990e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "contentType": "video",
      "contentId": "550e8400-e29b-41d4-a716-446655440000",
      "parentCommentId": null,
      "commentText": "Updated comment text",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T02:00:00.000Z"
    }
  }
}
```

---

### 5. Delete Comment

Delete your own comment.

**Endpoint:** `DELETE /api/comments/:id`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Comment deleted successfully"
}
```

**Note:** Deleting a comment with replies will also delete all replies (CASCADE).

---

### 6. Get Comment Count

Get total number of comments for content.

**Endpoint:** `GET /api/comments/:contentType/:contentId/count`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "count": 42
  }
}
```

---

## Playlists API

User playlists with support for all content types and auto-created Watch Later.

### Features

- Create custom playlists (public/private)
- Auto-created "Watch Later" playlist for each user
- Add any content type to playlists
- Ordered items with position tracking
- Cannot delete Watch Later playlist

### 1. Create Playlist

Create a new custom playlist.

**Endpoint:** `POST /api/playlists`

**Authentication:** Required

**Request Body:**
```json
{
  "name": "My Playlist",
  "description": "My favorite content",
  "isPublic": true
}
```

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "playlist": {
      "id": "aa0e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "name": "My Playlist",
      "description": "My favorite content",
      "isPublic": true,
      "isWatchLater": false,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get User's Playlists

Get all playlists for authenticated user.

**Endpoint:** `GET /api/playlists/my-playlists`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of playlists (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "playlists": [
      {
        "id": "aa0e8400-e29b-41d4-a716-446655440000",
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Watch Later",
        "description": null,
        "isPublic": false,
        "isWatchLater": true,
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      },
      {
        "id": "bb0e8400-e29b-41d4-a716-446655440000",
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "name": "My Favorites",
        "description": "My favorite videos",
        "isPublic": true,
        "isWatchLater": false,
        "createdAt": "2024-01-02T00:00:00.000Z",
        "updatedAt": "2024-01-02T00:00:00.000Z"
      }
    ]
  }
}
```

**Note:** Watch Later is always returned first.

---

### 3. Get Watch Later Playlist

Get or auto-create Watch Later playlist.

**Endpoint:** `GET /api/playlists/watch-later`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "playlist": {
      "id": "aa0e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "name": "Watch Later",
      "description": null,
      "isPublic": false,
      "isWatchLater": true,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Note:** Auto-creates if doesn't exist.

---

### 4. Get Playlist by ID

Get playlist details.

**Endpoint:** `GET /api/playlists/:id`

**Authentication:** Not required (but only public playlists visible to non-owners)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "playlist": {
      "id": "aa0e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "name": "My Playlist",
      "description": "My favorite content",
      "isPublic": true,
      "isWatchLater": false,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 5. Update Playlist

Update playlist details.

**Endpoint:** `PUT /api/playlists/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "name": "Updated Name",
  "description": "Updated description",
  "isPublic": false
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "playlist": {
      "id": "aa0e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "name": "Updated Name",
      "description": "Updated description",
      "isPublic": false,
      "isWatchLater": false,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-02T00:00:00.000Z"
    }
  }
}
```

**Error (403) - Watch Later:**
```json
{
  "success": false,
  "error": {
    "message": "Cannot update Watch Later playlist",
    "statusCode": 403
  }
}
```

---

### 6. Delete Playlist

Delete a playlist.

**Endpoint:** `DELETE /api/playlists/:id`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Playlist deleted successfully"
}
```

**Error (403) - Watch Later:**
```json
{
  "success": false,
  "error": {
    "message": "Cannot delete Watch Later playlist",
    "statusCode": 403
  }
}
```

---

### 7. Add Item to Playlist

Add content to a playlist.

**Endpoint:** `POST /api/playlists/:id/items`

**Authentication:** Required

**Request Body:**
```json
{
  "contentType": "video",
  "contentId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "item": {
      "id": "cc0e8400-e29b-41d4-a716-446655440000",
      "playlistId": "aa0e8400-e29b-41d4-a716-446655440000",
      "contentType": "video",
      "contentId": "550e8400-e29b-41d4-a716-446655440000",
      "position": 0,
      "createdAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error (409) - Duplicate:**
```json
{
  "success": false,
  "error": {
    "message": "Item already exists in playlist",
    "statusCode": 409
  }
}
```

---

### 8. Remove Item from Playlist

Remove content from a playlist.

**Endpoint:** `DELETE /api/playlists/:id/items/:contentType/:contentId`

**Authentication:** Required

**Example:**
```
DELETE /api/playlists/aa0e8400-e29b-41d4-a716-446655440000/items/video/550e8400-e29b-41d4-a716-446655440000
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Item removed from playlist"
}
```

---

### 9. Get Playlist Items

Get all items in a playlist.

**Endpoint:** `GET /api/playlists/:id/items`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of items (default: 50)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": "cc0e8400-e29b-41d4-a716-446655440000",
        "playlistId": "aa0e8400-e29b-41d4-a716-446655440000",
        "contentType": "video",
        "contentId": "550e8400-e29b-41d4-a716-446655440000",
        "position": 0,
        "createdAt": "2024-01-01T00:00:00.000Z"
      },
      {
        "id": "dd0e8400-e29b-41d4-a716-446655440000",
        "playlistId": "aa0e8400-e29b-41d4-a716-446655440000",
        "contentType": "bitz",
        "contentId": "660e8400-e29b-41d4-a716-446655440000",
        "position": 1,
        "createdAt": "2024-01-01T01:00:00.000Z"
      }
    ]
  }
}
```

---

### 10. Check Item in Playlist

Check if content exists in playlist.

**Endpoint:** `GET /api/playlists/:id/items/:contentType/:contentId/check`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "exists": true
  }
}
```

---

## Subscriptions API

Channel subscription system with subscriber counts.

### Features

- Subscribe/unsubscribe to channels
- Check subscription status
- Get user's subscriptions
- Get channel's subscribers
- Real-time subscriber counts
- Cannot subscribe to yourself

### 1. Subscribe to Channel

Subscribe to a user's channel.

**Endpoint:** `POST /api/subscriptions`

**Authentication:** Required

**Request Body:**
```json
{
  "channelId": "770e8400-e29b-41d4-a716-446655440000"
}
```

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "subscribed": true,
    "subscriberCount": 1250
  }
}
```

**Error (409) - Self Subscribe:**
```json
{
  "success": false,
  "error": {
    "message": "Cannot subscribe to yourself",
    "statusCode": 409
  }
}
```

**Error (409) - Already Subscribed:**
```json
{
  "success": false,
  "error": {
    "message": "Already subscribed to this channel",
    "statusCode": 409
  }
}
```

---

### 2. Unsubscribe from Channel

Unsubscribe from a channel.

**Endpoint:** `DELETE /api/subscriptions/:channelId`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "unsubscribed": true,
    "subscriberCount": 1249
  }
}
```

---

### 3. Check Subscription Status

Check if user is subscribed to a channel.

**Endpoint:** `GET /api/subscriptions/check/:channelId`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "subscribed": true
  }
}
```

---

### 4. Get User's Subscriptions

Get all channels user is subscribed to.

**Endpoint:** `GET /api/subscriptions/my-subscriptions`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of items (default: 50)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "channelIds": [
      "770e8400-e29b-41d4-a716-446655440000",
      "880e8400-e29b-41d4-a716-446655440000",
      "990e8400-e29b-41d4-a716-446655440000"
    ]
  }
}
```

---

### 5. Get Channel Subscribers

Get all subscribers of a channel.

**Endpoint:** `GET /api/subscriptions/channel/:channelId/subscribers`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of items (default: 50)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "subscriberIds": [
      "660e8400-e29b-41d4-a716-446655440000",
      "aa0e8400-e29b-41d4-a716-446655440000",
      "bb0e8400-e29b-41d4-a716-446655440000"
    ]
  }
}
```

---

### 6. Get Subscriber Count

Get total subscriber count for a channel.

**Endpoint:** `GET /api/subscriptions/channel/:channelId/count`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "count": 1250
  }
}
```

---

## Downloads API

User download tracking and management.

### 1. Create Download

Track a new download.

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

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "download": {
      "id": "ee0e8400-e29b-41d4-a716-446655440000",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "contentType": "video",
      "contentId": "550e8400-e29b-41d4-a716-446655440000",
      "downloadUrl": "https://example.com/downloads/video.mp4",
      "fileSize": 104857600,
      "status": "completed",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get User's Downloads

Get all downloads for authenticated user.

**Endpoint:** `GET /api/downloads/my-downloads`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "downloads": [
      {
        "id": "ee0e8400-e29b-41d4-a716-446655440000",
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "contentType": "video",
        "contentId": "550e8400-e29b-41d4-a716-446655440000",
        "downloadUrl": "https://example.com/downloads/video.mp4",
        "fileSize": 104857600,
        "status": "completed",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 3. Update Download

Update download status.

**Endpoint:** `PUT /api/downloads/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "status": "failed"
}
```

---

### 4. Delete Download

Delete a download record.

**Endpoint:** `DELETE /api/downloads/:id`

**Authentication:** Required

---

## Trending API

Algorithmic trending content across all types.

### Features

- Separate trending for videos, bitz, and posts
- Algorithm based on views, likes, dislikes, and recency
- Materialized views for performance
- Periodic refresh recommended

### Trending Algorithm

```
score = (views * 0.5) + (likes * 2) - (dislikes * 1.5)
```

Only includes:
- Content from last 7 days
- Status is 'published'
- Content with significant engagement

### 1. Get Trending Videos

**Endpoint:** `GET /api/trending/videos`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
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
        "createdAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 2. Get Trending Bitz

**Endpoint:** `GET /api/trending/bitz`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

---

### 3. Get Trending Posts

**Endpoint:** `GET /api/trending/posts`

**Query Parameters:**
- `limit`: Number of items (default: 20)
- `offset`: Pagination offset (default: 0)

---

### 4. Refresh Trending Data

Manually refresh trending materialized views.

**Endpoint:** `POST /api/trending/refresh`

**Authentication:** Not required (but should be protected in production)

**Success Response (200):**
```json
{
  "success": true,
  "message": "Trending data refreshed successfully"
}
```

**Note:** Set up a cron job to call this endpoint hourly:
```bash
0 * * * * curl -X POST http://localhost:4400/api/trending/refresh
```

---

## Recommendations API

Personalized content recommendation system using advanced algorithmic scoring.

### Features

- **Personalized Recommendations**: Content tailored to each user based on their behavior
- **Multi-Factor Scoring**: Combines engagement, recency, relevance, and quality metrics
- **Content Type Support**: Separate recommendation algorithms for Videos, Posts, and Bitz
- **Context Awareness**: Considers current viewing context and user history
- **Trending Recommendations**: Velocity-based trending content discovery

### Recommendation Algorithm

The recommendation system uses a weighted scoring algorithm with four components:

**Final Score = (Engagement × 0.40) + (Recency × 0.25) + (Relevance × 0.20) + (Quality × 0.15)**

#### Score Components

1. **Engagement Score (40%)**: Measures user interaction with content
   - Views count
   - Likes and dislikes ratio
   - Comments count
   - Shares count
   - Normalized across content types

2. **Recency Score (25%)**: Measures content freshness
   - Exponential decay based on publication time
   - Maximum score for content < 1 hour old
   - Gradually decreases as content ages
   - Minimum score for content > 30 days old

3. **Relevance Score (20%)**: Personalization factor
   - User's subscribed channels boost
   - Watch history penalties (avoid duplicates)
   - Same channel/author boost for related content
   - User preference learning

4. **Quality Score (15%)**: Content quality indicator
   - Like/dislike ratio
   - Engagement rate
   - Creator reputation (for videos)

### 1. Get Recommended Videos

Get personalized video recommendations for the authenticated user.

**Endpoint:** `GET /api/recommendations/videos`

**Authentication:** Required

**Query Parameters:**
- `currentVideoId` (optional): UUID of currently watching video (excluded from results)
- `limit` (optional): Number of recommendations (default: 20, max: 100)

**Example:**
```
GET /api/recommendations/videos?currentVideoId=550e8400-e29b-41d4-a716-446655440000&limit=20
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "videos": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "title": "Recommended Video Title",
        "description": "Video description",
        "url": "https://example.com/videos/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 300,
        "userId": "770e8400-e29b-41d4-a716-446655440000",
        "views": 50000,
        "likes": 2500,
        "dislikes": 50,
        "category": "Technology",
        "tags": ["tech", "programming"],
        "publishedAt": "2024-01-15T10:00:00.000Z",
        "createdAt": "2024-01-15T10:00:00.000Z",
        "score": 0.875
      }
    ],
    "count": 20
  }
}
```

**Notes:**
- Recommendations exclude videos already in user's recent watch history (last 20 videos)
- Current video (if specified) is excluded from results
- Results are sorted by recommendation score (descending)
- Score ranges from 0.0 to 1.0 (higher is better)

---

### 2. Get Recommended Posts

Get personalized post recommendations for the authenticated user.

**Endpoint:** `GET /api/recommendations/posts`

**Authentication:** Required

**Query Parameters:**
- `currentPostId` (optional): UUID of currently viewing post (excluded from results)
- `limit` (optional): Number of recommendations (default: 15, max: 100)

**Example:**
```
GET /api/recommendations/posts?currentPostId=550e8400-e29b-41d4-a716-446655440000&limit=15
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "posts": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "title": "Recommended Post Title",
        "content": "Post content text...",
        "imageUrl": "https://example.com/images/post.jpg",
        "userId": "770e8400-e29b-41d4-a716-446655440000",
        "views": 15000,
        "likes": 800,
        "dislikes": 20,
        "createdAt": "2024-01-15T10:00:00.000Z",
        "score": 0.782
      }
    ],
    "count": 15
  }
}
```

**Notes:**
- Recommendations exclude posts user has already liked
- Current post (if specified) is excluded from results
- Results are sorted by recommendation score (descending)

---

### 3. Get Recommended Bitz

Get personalized bitz recommendations for the authenticated user.

**Endpoint:** `GET /api/recommendations/bitz`

**Authentication:** Required

**Query Parameters:**
- `currentBitzId` (optional): UUID of currently viewing bitz (excluded from results)
- `limit` (optional): Number of recommendations (default: 10, max: 100)

**Example:**
```
GET /api/recommendations/bitz?currentBitzId=550e8400-e29b-41d4-a716-446655440000&limit=10
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "bitz": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "title": "Recommended Bitz Title",
        "description": "Bitz description",
        "url": "https://example.com/bitz/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 30,
        "userId": "770e8400-e29b-41d4-a716-446655440000",
        "views": 50000,
        "likes": 3000,
        "dislikes": 100,
        "createdAt": "2024-01-15T10:00:00.000Z",
        "score": 0.856
      }
    ],
    "count": 10
  }
}
```

**Notes:**
- Recommendations exclude bitz user has already watched
- Current bitz (if specified) is excluded from results
- Results are sorted by recommendation score (descending)

---

### 4. Get Trending Recommendations

Get trending content recommendations using velocity-based scoring.

**Endpoint:** `GET /api/recommendations/trending/:contentType`

**Authentication:** Not required

**Path Parameters:**
- `contentType`: Content type - `video`, `bitz`, or `post`

**Query Parameters:**
- `timeWindow` (optional): Time window for trending - `24h`, `7d`, or `30d` (default: `7d`)
- `limit` (optional): Number of recommendations (default: 10, max: 100)

**Examples:**
```
GET /api/recommendations/trending/video?timeWindow=24h&limit=10
GET /api/recommendations/trending/post?timeWindow=7d&limit=20
GET /api/recommendations/trending/bitz?timeWindow=30d&limit=15
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "contentType": "video",
        "title": "Trending Video Title",
        "score": 12500.75,
        "views": 100000,
        "likes": 5000,
        "dislikes": 200,
        "createdAt": "2024-01-15T10:00:00.000Z"
      }
    ],
    "contentType": "video",
    "timeWindow": "24h",
    "count": 10
  }
}
```

**Error Response (400) - Invalid Content Type:**
```json
{
  "success": false,
  "error": "Invalid content type. Must be video, bitz, or post"
}
```

**Error Response (400) - Invalid Time Window:**
```json
{
  "success": false,
  "error": "Invalid time window. Must be 24h, 7d, or 30d"
}
```

**Trending Algorithm:**

The trending score uses velocity-based calculation:

```
Trending Score = (Velocity Score + Engagement Rate Score) × Recency Boost
```

Where:
- **Velocity Score** = Views / (Hours Since Publication + 1) × 0.5
- **Engagement Rate Score** = (Likes / (Views + 1)) × 1000 × 0.3
- **Recency Boost**: 1.5x for 24h window (< 24 hours), 0.5x otherwise

**Notes:**
- Trending recommendations are not personalized (same for all users)
- Focuses on content gaining engagement quickly
- Recency boost only applies to 24-hour window
- Results are sorted by trending score (descending)

---

## Data Models

### ContentType

```typescript
type ContentType = 'video' | 'bitz' | 'post';
```

### LikeType

```typescript
type LikeType = 'like' | 'dislike';
```

### Like

```typescript
interface Like {
  id: string;
  userId: string;
  contentType: ContentType;
  contentId: string;
  likeType: LikeType;
  createdAt: Date;
  updatedAt: Date;
}
```

### Comment

```typescript
interface Comment {
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

### Playlist

```typescript
interface Playlist {
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

### PlaylistItem

```typescript
interface PlaylistItem {
  id: string;
  playlistId: string;
  contentType: ContentType;
  contentId: string;
  position: number;
  createdAt: Date;
}
```

### Subscription

```typescript
interface Subscription {
  id: string;
  subscriberId: string;
  channelId: string;
  createdAt: Date;
}
```

### Download

```typescript
interface Download {
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

### RecommendedVideo

```typescript
interface RecommendedVideo {
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
  category?: string;
  tags?: string[];
  publishedAt?: Date;
  createdAt: Date;
  score: number; // Recommendation score (0.0 to 1.0)
}
```

### RecommendedPost

```typescript
interface RecommendedPost {
  id: string;
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  views: number;
  likes: number;
  dislikes: number;
  createdAt: Date;
  score: number; // Recommendation score (0.0 to 1.0)
}
```

### RecommendedBitz

```typescript
interface RecommendedBitz {
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
  createdAt: Date;
  score: number; // Recommendation score (0.0 to 1.0)
}
```

### TrendingContent

```typescript
interface TrendingContent {
  id: string;
  contentType: ContentType;
  title: string;
  score: number; // Trending score (can exceed 1.0)
  views: number;
  likes: number;
  dislikes: number;
  createdAt: Date;
}
```

---

[Back to Documentation Index](./README.md)
