# Bitz Service API Documentation

Short vertical video (TikTok/Reels style) management service for the Lambrk platform.

**Base URL**: `http://localhost:4403` (Direct) or `http://localhost:4400/api/bitz` (Gateway)

## Table of Contents

- [Overview](#overview)
- [Endpoints](#endpoints)
- [Data Models](#data-models)
- [Examples](#examples)

## Overview

The Bitz Service handles:
- Short vertical video CRUD operations
- Optimized for mobile vertical content (9:16 aspect ratio)
- Similar to TikTok/Instagram Reels
- View counting
- Integration with likes, comments, and playlists

### Bitz Characteristics

- **Duration**: Typically 15-60 seconds (though not enforced)
- **Format**: Vertical videos (portrait mode)
- **Use Case**: Quick, engaging content for mobile users
- **Discovery**: Featured in trending and recommendations

## Endpoints

### 1. Create Bitz

Upload/create a new short video.

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

**Validation Rules:**
- `title`: Required, non-empty string
- `url`: Required, valid URL
- `description`: Optional string
- `thumbnailUrl`: Optional, valid URL
- `duration`: Optional integer (seconds)
- `status`: Optional, one of: 'draft', 'published', 'processing'

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "bitz": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My Awesome Bitz",
      "description": "Short video description",
      "url": "https://example.com/bitz/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
      "duration": 30,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 0,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get All Bitz

Retrieve a paginated list of bitz.

**Endpoint:** `GET /api/bitz`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of bitz per page (default: 20)
- `offset`: Pagination offset (default: 0)
- `status`: Filter by status: 'draft', 'published', 'processing'

**Example:**
```
GET /api/bitz?limit=20&offset=0&status=published
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "bitz": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "Amazing Bitz",
        "description": "Check this out!",
        "url": "https://example.com/bitz/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 45,
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 5000,
        "status": "published",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 3. Get User's Bitz

Get all bitz created by the authenticated user.

**Endpoint:** `GET /api/bitz/my-bitz`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of bitz per page (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "bitz": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "My Bitz",
        "description": "My short video",
        "url": "https://example.com/bitz/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 20,
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 100,
        "status": "published",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 4. Get Bitz by ID

Retrieve a specific bitz by its ID.

**Endpoint:** `GET /api/bitz/:id`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "bitz": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Amazing Bitz",
      "description": "Check this out!",
      "url": "https://example.com/bitz/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
      "duration": 45,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 5000,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "error": {
    "message": "Bitz not found",
    "statusCode": 404
  }
}
```

---

### 5. Update Bitz

Update an existing bitz. Users can only update their own bitz.

**Endpoint:** `PUT /api/bitz/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "Updated Bitz Title",
  "description": "Updated description",
  "thumbnailUrl": "https://example.com/thumbnails/new-thumb.jpg",
  "status": "published"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "bitz": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Updated Bitz Title",
      "description": "Updated description",
      "url": "https://example.com/bitz/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/new-thumb.jpg",
      "duration": 45,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 5000,
      "status": "published",
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
    "message": "You can only update your own bitz",
    "statusCode": 403
  }
}
```

---

### 6. Delete Bitz

Delete a bitz. Users can only delete their own bitz.

**Endpoint:** `DELETE /api/bitz/:id`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Bitz deleted successfully"
}
```

---

### 7. Increment Views

Increment the view count for a bitz.

**Endpoint:** `POST /api/bitz/:id/views`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Views incremented"
}
```

---

## Data Models

### Bitz Model

```typescript
interface Bitz {
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

---

## Examples

### Complete Bitz Upload Flow

```javascript
// 1. Create bitz
const createResponse = await fetch('http://localhost:4400/api/bitz', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    title: 'My Bitz',
    description: 'Check this out!',
    url: 'https://example.com/bitz.mp4',
    thumbnailUrl: 'https://example.com/thumb.jpg',
    duration: 30,
    status: 'published'
  })
});

const { data: { bitz } } = await createResponse.json();

// 2. Get bitz feed for users
const feedResponse = await fetch('http://localhost:4400/api/bitz?limit=20&status=published');
const { data: { bitz: bitzFeed } } = await feedResponse.json();

// 3. Track view when user watches
await fetch(`http://localhost:4400/api/bitz/${bitz.id}/views`, {
  method: 'POST'
});
```

### Bitz Feed Implementation

```javascript
// Infinite scroll implementation
let offset = 0;
const limit = 20;

async function loadMoreBitz() {
  const response = await fetch(
    `http://localhost:4400/api/bitz?limit=${limit}&offset=${offset}&status=published`
  );
  const { data: { bitz } } = await response.json();
  
  // Render bitz in vertical scroll feed
  bitz.forEach(renderBitz);
  
  offset += limit;
}

// Load initial bitz
loadMoreBitz();

// Load more on scroll
window.addEventListener('scroll', () => {
  if (isNearBottom()) {
    loadMoreBitz();
  }
});
```

### Bitz with Interactions

```javascript
// Like a bitz (using Interaction Service)
await fetch('http://localhost:4400/api/likes', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    contentType: 'bitz',
    contentId: bitzId,
    likeType: 'like'
  })
});

// Add comment (using Interaction Service)
await fetch('http://localhost:4400/api/comments', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    contentType: 'bitz',
    contentId: bitzId,
    commentText: 'Amazing!'
  })
});

// Save to favorites (using Interaction Service)
const watchLaterResponse = await fetch('http://localhost:4400/api/playlists/watch-later', {
  headers: { 'Authorization': `Bearer ${accessToken}` }
});
const { data: { playlist } } = await watchLaterResponse.json();

await fetch(`http://localhost:4400/api/playlists/${playlist.id}/items`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    contentType: 'bitz',
    contentId: bitzId
  })
});
```

---

## Best Practices

### Video Specifications

Recommended specifications for bitz:
- **Aspect Ratio**: 9:16 (portrait)
- **Resolution**: 1080x1920 or 720x1280
- **Duration**: 15-60 seconds
- **Format**: MP4 (H.264)
- **File Size**: < 50MB for optimal performance

### Mobile Optimization

- Use vertical format for mobile viewing
- Keep videos short and engaging
- Optimize for cellular data (compress appropriately)
- Generate thumbnails at same aspect ratio

### User Experience

- Auto-play when in viewport
- Pause when scrolling away
- Snap scrolling (one bitz per screen)
- Keyboard navigation support (arrow keys)
- Preload next bitz for smooth playback

---

## Rate Limiting

- **Create/Update/Delete**: 20 operations per hour per user (higher than videos)
- **Read Operations**: 100 requests per 15 minutes per IP

---

## Integration with Other Services

### Likes/Dislikes
See [Interaction Service - Likes API](./interaction-service.md#likesdislikes-api)

### Comments
See [Interaction Service - Comments API](./interaction-service.md#comments-api)

### Trending
See [Interaction Service - Trending API](./interaction-service.md#trending-api)

---

[Back to Documentation Index](./README.md)
