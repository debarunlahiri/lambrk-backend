# Lambrk Backend API Documentation

Welcome to the Lambrk Backend API documentation. This directory contains comprehensive API documentation organized by service.

## Quick Links

- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [Service Documentation](#service-documentation)
- [Common Patterns](#common-patterns)

## Getting Started

### Base URLs

- **API Gateway**: `http://localhost:4400` (recommended for all requests)
- **Auth Service**: `http://localhost:4401`
- **Video Service**: `http://localhost:4402`
- **Bitz Service**: `http://localhost:4403`
- **Posts Service**: `http://localhost:4404`
- **Interaction Service**: `http://localhost:4405`
- **Compression Service**: `http://localhost:4500`

### Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <access_token>
```

For detailed authentication documentation, see [Auth Service Documentation](./auth-service.md).

## Service Documentation

### [Auth Service](./auth-service.md)
User authentication, registration, and profile management.
- User signup/signin
- Google OAuth integration
- Firebase authentication
- JWT token management
- User profiles

### [Video Service](./video-service.md)
Full-length video management.
- Video CRUD operations
- Multiple quality support (4K, 2K, HD, 720p, 480p, 360p)
- View tracking
- Like/dislike integration

### [Bitz Service](./bitz-service.md)
Short vertical videos (TikTok/Reels style).
- Bitz CRUD operations
- View tracking
- Optimized for mobile vertical content

### [Posts Service](./posts-service.md)
Social media-style posts with images.
- Post CRUD operations
- Image support
- View tracking

### [Interaction Service](./interaction-service.md)
User interactions across all content types.
- **Likes/Dislikes**: Universal like system
- **Comments**: Nested comments with replies
- **Playlists**: User playlists and Watch Later
- **Subscriptions**: Channel subscriptions
- **Downloads**: Download tracking
- **Trending**: Algorithmic trending content
- **Recommendations**: Personalized content recommendations

### [Compression Service](./compression-service.md)
Video compression and quality management.
- Single and batch video compression
- Multiple quality versions (144p to 4K)
- S3 upload integration
- Compression status tracking
- Quality version management

## Common Patterns

### Error Handling

All API responses follow a consistent format:

**Success Response:**
```json
{
  "success": true,
  "data": {
    // Response data here
  }
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "message": "Error description",
    "statusCode": 400
  }
}
```

### Pagination

Most list endpoints support pagination:

```
GET /api/endpoint?limit=20&offset=0
```

Parameters:
- `limit`: Number of items per page (default: 20)
- `offset`: Number of items to skip (default: 0)

### Content Types

The platform supports multiple content types:
- `video`: Full-length videos
- `bitz`: Short vertical videos
- `post`: Social media posts

These are used in polymorphic endpoints (likes, comments, playlists).

### Rate Limiting

Rate limits are applied to prevent abuse:
- **Gateway**: 300 requests per 15 minutes
- **Auth Endpoints**: 5 requests per 15 minutes
- **Read Operations**: 100 requests per 15 minutes
- **Write Operations**: 50 requests per 15 minutes
- **Video/Bitz Upload**: 10-20 operations per hour

When rate limits are exceeded, you'll receive a `429 Too Many Requests` response.

## HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Missing or invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Resource already exists |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

## Quick Examples

### Authentication Flow

```bash
# 1. Register
curl -X POST http://localhost:4400/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "email": "user@example.com", "password": "Pass123"}'

# 2. Login
curl -X POST http://localhost:4400/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "Pass123"}'
```

### Creating Content

```bash
# Create a video
curl -X POST http://localhost:4400/api/videos \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "My Video", "url": "https://example.com/video.mp4"}'
```

### Interactions

```bash
# Like a video
curl -X POST http://localhost:4400/api/likes \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contentType": "video", "contentId": "UUID", "likeType": "like"}'

# Add a comment
curl -X POST http://localhost:4400/api/comments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"contentType": "video", "contentId": "UUID", "commentText": "Great!"}'
```

## Data Models

### Common Types

```typescript
type ContentType = 'video' | 'bitz' | 'post';
type LikeType = 'like' | 'dislike';
type Status = 'draft' | 'published' | 'processing';
```

For detailed model definitions, refer to individual service documentation.

## Support

- **Setup Guide**: [QUICK_START.md](../QUICK_START.md)
- **Architecture**: [UPDATE_SUMMARY.md](../UPDATE_SUMMARY.md)
- **Main README**: [README.md](../README.md)

For issues or questions, contact the development team.
