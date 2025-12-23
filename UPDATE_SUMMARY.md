# Lambrk Backend - Update Summary

## Overview

The Lambrk backend has been completely updated to support the full-featured video streaming platform as described in the frontend README. The backend now provides comprehensive API support for videos, bitz (short vertical videos), posts, likes/dislikes, comments, playlists, subscriptions, downloads, and trending content.

## Architecture

The backend follows a microservices architecture with the following services:

### Services

1. **API Gateway** (Port 3100)
   - Routes requests to appropriate microservices
   - Handles CORS, rate limiting, and request proxying

2. **Auth Service** (Port 3101)
   - User authentication (email/password, Google OAuth, Firebase)
   - JWT token generation and validation
   - User profile management

3. **Video Service** (Port 3102)
   - Video CRUD operations
   - Video quality management (4K, 2K, HD, 720p, 480p, 360p, Auto)
   - View counting
   - Dislikes support integrated

4. **Bitz Service** (Port 3103)
   - Short vertical video CRUD operations
   - Similar to TikTok/Instagram Reels
   - View counting

5. **Posts Service** (Port 3104)
   - Social media-style post CRUD operations
   - Image support
   - View counting

6. **Interaction Service** (Port 3105)
   - **Likes/Dislikes**: Universal like/dislike system for all content types
   - **Comments**: Nested comments with replies for all content
   - **Playlists**: User playlists with Watch Later functionality
   - **Subscriptions**: Channel subscription system
   - **Downloads**: User download tracking
   - **Trending**: Trending content across all types

## New Features

### 1. Bitz (Short Vertical Videos)
- Full CRUD operations
- Status management (draft, published, processing)
- View tracking
- Integration with likes, comments, and playlists

### 2. Posts
- Full CRUD operations
- Image support
- View tracking
- Integration with likes, comments, and shares

### 3. Universal Likes/Dislikes System
- Single API for liking/disliking any content type
- Toggle functionality (like → dislike → remove)
- Real-time statistics with user's current state
- Supports videos, bitz, and posts

### 4. Comments System
- Create, read, update, delete comments
- Nested comments (replies)
- Works on videos, bitz, and posts
- Comment count tracking
- Integration with likes/dislikes

### 5. Playlists
- Create custom playlists (public/private)
- Automatic "Watch Later" playlist
- Add any content type to playlists
- Remove items from playlists
- Check if item exists in playlist
- Ordered items with position tracking

### 6. Subscriptions
- Subscribe/unsubscribe to channels
- Check subscription status
- Get user's subscriptions
- Get channel's subscribers
- Subscriber count tracking

### 7. Downloads
- Track user downloads
- Status management (pending, completed, failed)
- File size tracking
- Download URL management

### 8. Trending Content
- Automated trending algorithm
- Separate trending for videos, bitz, and posts
- Based on views, likes, dislikes, and recency
- Materialized views for performance
- Refresh endpoint for periodic updates

## Database Schema

### New Tables

- `bitz` - Short vertical videos
- `posts` - Social media posts
- `likes` - Universal likes/dislikes (polymorphic)
- `comments` - Nested comments (polymorphic)
- `playlists` - User playlists
- `playlist_items` - Playlist contents (polymorphic)
- `subscriptions` - Channel subscriptions
- `downloads` - User download tracking
- `view_history` - Watch history tracking

### Materialized Views

- `trending_videos` - Top trending videos
- `trending_bitz` - Top trending bitz
- `trending_posts` - Top trending posts

### Enhanced Tables

- `videos` - Added `dislikes` and `is_trending` columns

## API Endpoints Summary

### Bitz Service
- `POST /api/bitz` - Create bitz
- `GET /api/bitz` - Get all bitz
- `GET /api/bitz/my-bitz` - Get user's bitz
- `GET /api/bitz/:id` - Get bitz by ID
- `PUT /api/bitz/:id` - Update bitz
- `DELETE /api/bitz/:id` - Delete bitz
- `POST /api/bitz/:id/views` - Increment views

### Posts Service
- `POST /api/posts` - Create post
- `GET /api/posts` - Get all posts
- `GET /api/posts/my-posts` - Get user's posts
- `GET /api/posts/:id` - Get post by ID
- `PUT /api/posts/:id` - Update post
- `DELETE /api/posts/:id` - Delete post
- `POST /api/posts/:id/views` - Increment views

### Interaction Service - Likes
- `POST /api/likes` - Toggle like/dislike
- `GET /api/likes/:contentType/:contentId` - Get like stats
- `GET /api/likes/user/:contentType` - Get user's liked content

### Interaction Service - Comments
- `POST /api/comments` - Create comment
- `GET /api/comments/:contentType/:contentId` - Get content comments
- `GET /api/comments/:commentId/replies` - Get comment replies
- `PUT /api/comments/:id` - Update comment
- `DELETE /api/comments/:id` - Delete comment
- `GET /api/comments/:contentType/:contentId/count` - Get comment count

### Interaction Service - Playlists
- `POST /api/playlists` - Create playlist
- `GET /api/playlists/my-playlists` - Get user's playlists
- `GET /api/playlists/watch-later` - Get Watch Later playlist
- `GET /api/playlists/:id` - Get playlist by ID
- `PUT /api/playlists/:id` - Update playlist
- `DELETE /api/playlists/:id` - Delete playlist
- `POST /api/playlists/:id/items` - Add item to playlist
- `DELETE /api/playlists/:id/items/:contentType/:contentId` - Remove item
- `GET /api/playlists/:id/items` - Get playlist items
- `GET /api/playlists/:id/items/:contentType/:contentId/check` - Check item

### Interaction Service - Subscriptions
- `POST /api/subscriptions` - Subscribe to channel
- `DELETE /api/subscriptions/:channelId` - Unsubscribe
- `GET /api/subscriptions/check/:channelId` - Check subscription
- `GET /api/subscriptions/my-subscriptions` - Get user's subscriptions
- `GET /api/subscriptions/channel/:channelId/subscribers` - Get subscribers
- `GET /api/subscriptions/channel/:channelId/count` - Get subscriber count

### Interaction Service - Downloads
- `POST /api/downloads` - Create download
- `GET /api/downloads/my-downloads` - Get user's downloads
- `GET /api/downloads/:id` - Get download by ID
- `PUT /api/downloads/:id` - Update download
- `DELETE /api/downloads/:id` - Delete download

### Interaction Service - Trending
- `GET /api/trending/videos` - Get trending videos
- `GET /api/trending/bitz` - Get trending bitz
- `GET /api/trending/posts` - Get trending posts
- `POST /api/trending/refresh` - Refresh trending data

## Setup Instructions

### Prerequisites

- Docker and Docker Compose
- Node.js 18+ (for local development)
- PostgreSQL 15 (handled by Docker)

### Running with Docker

1. **Build and start all services:**
```bash
docker-compose up --build
```

2. **Run database migrations:**
```bash
# The migrations need to be run in order
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /migrations/001_initial_schema.sql
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /migrations/002_create_video_qualities_table.sql
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /migrations/003_create_comprehensive_platform_schema.sql
```

Or copy the migration files to the container and run:
```bash
docker cp migrations/003_create_comprehensive_platform_schema.sql lambrk-postgres:/tmp/
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /tmp/003_create_comprehensive_platform_schema.sql
```

3. **Access the services:**
- API Gateway: http://localhost:3100
- Auth Service: http://localhost:3101
- Video Service: http://localhost:3102
- Bitz Service: http://localhost:3103
- Posts Service: http://localhost:3104
- Interaction Service: http://localhost:3105

### Running Locally (Development)

1. **Install dependencies:**
```bash
# Install shared package
cd shared
npm install
npm run build

# Install gateway
cd ../gateway
npm install

# Install all services
cd ../services/auth-service && npm install
cd ../video-service && npm install
cd ../bitz-service && npm install
cd ../posts-service && npm install
cd ../interaction-service && npm install
```

2. **Set up environment variables:**
Create a `.env` file in the root directory:
```env
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=lambrk_user
POSTGRES_PASSWORD=lambrk_password
POSTGRES_DB=lambrk

# JWT
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRES_IN=7d

# Services
AUTH_SERVICE_URL=http://localhost:3101
VIDEO_SERVICE_URL=http://localhost:3102
BITZ_SERVICE_URL=http://localhost:3103
POSTS_SERVICE_URL=http://localhost:3104
INTERACTION_SERVICE_URL=http://localhost:3105

# Google OAuth (optional)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Firebase (optional)
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_PRIVATE_KEY=your-firebase-private-key
FIREBASE_CLIENT_EMAIL=your-firebase-client-email

# CORS
CORS_ORIGIN=http://localhost:3100
```

3. **Run database migrations:**
```bash
psql -U lambrk_user -d lambrk -f migrations/001_initial_schema.sql
psql -U lambrk_user -d lambrk -f migrations/002_create_video_qualities_table.sql
psql -U lambrk_user -d lambrk -f migrations/003_create_comprehensive_platform_schema.sql
```

4. **Start services:**
```bash
# Terminal 1 - Gateway
cd gateway && npm run dev

# Terminal 2 - Auth Service
cd services/auth-service && npm run dev

# Terminal 3 - Video Service
cd services/video-service && npm run dev

# Terminal 4 - Bitz Service
cd services/bitz-service && npm run dev

# Terminal 5 - Posts Service
cd services/posts-service && npm run dev

# Terminal 6 - Interaction Service
cd services/interaction-service && npm run dev
```

## Periodic Maintenance

### Refresh Trending Data

Trending data should be refreshed periodically (recommended: hourly) to keep it up-to-date:

```bash
curl -X POST http://localhost:3100/api/trending/refresh \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

You can set up a cron job for this:
```bash
# Add to crontab
0 * * * * curl -X POST http://localhost:3100/api/trending/refresh
```

## Testing

### Test Authentication
```bash
# Register a user
curl -X POST http://localhost:3100/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123"
  }'

# Login
curl -X POST http://localhost:3100/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123"
  }'
```

### Test Video Creation
```bash
curl -X POST http://localhost:3100/api/videos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "title": "Test Video",
    "url": "https://example.com/video.mp4",
    "status": "published"
  }'
```

### Test Like System
```bash
# Like a video
curl -X POST http://localhost:3100/api/likes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "contentType": "video",
    "contentId": "VIDEO_UUID",
    "likeType": "like"
  }'

# Get like stats
curl http://localhost:3100/api/likes/video/VIDEO_UUID
```

### Test Comments
```bash
# Add a comment
curl -X POST http://localhost:3100/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "contentType": "video",
    "contentId": "VIDEO_UUID",
    "commentText": "Great video!"
  }'

# Get comments
curl http://localhost:3100/api/comments/video/VIDEO_UUID
```

## Documentation

- **Organized API Documentation** (Primary): See [docs/README.md](./docs/README.md) for service-specific documentation
  - [Auth Service API](./docs/auth-service.md)
  - [Video Service API](./docs/video-service.md)
  - [Bitz Service API](./docs/bitz-service.md)
  - [Posts Service API](./docs/posts-service.md)
  - [Interaction Service API](./docs/interaction-service.md)
- **Consolidated Reference**: See `API_DOCUMENTATION.md` (single-file version for offline use)
- **Frontend README**: Provided by user (describes UI requirements)

## Technology Stack

- **Language**: TypeScript
- **Runtime**: Node.js 18
- **Framework**: Express.js
- **Database**: PostgreSQL 15
- **Authentication**: JWT, Google OAuth, Firebase
- **Architecture**: Microservices
- **Gateway**: http-proxy-middleware
- **Validation**: express-validator
- **Security**: Rate limiting, CORS, bcrypt
- **Containerization**: Docker & Docker Compose

## Key Design Decisions

1. **Microservices Architecture**: Each major feature is a separate service for scalability
2. **Polymorphic Relationships**: Likes, comments, and playlists work with any content type
3. **Materialized Views**: Trending data is pre-computed for performance
4. **Auto-created Playlists**: Watch Later is automatically created for each user
5. **Flexible Content Types**: Easy to add new content types (e.g., podcasts, shorts)
6. **Universal Interaction Service**: All user interactions in one service for consistency

## Future Enhancements

- File upload service for videos, images, thumbnails
- Video transcoding service
- Real-time notifications with WebSockets
- Search service with Elasticsearch
- Analytics service
- CDN integration for video delivery
- Admin dashboard
- Content moderation service
- Recommendation engine
- Live streaming support

## Troubleshooting

### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs lambrk-postgres

# Connect to database
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk
```

### Service Not Responding
```bash
# Check service logs
docker logs lambrk-auth-service
docker logs lambrk-video-service
docker logs lambrk-bitz-service
docker logs lambrk-posts-service
docker logs lambrk-interaction-service
docker logs lambrk-gateway

# Restart a service
docker-compose restart auth-service
```

### Migration Issues
```bash
# Check which migrations have run
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -c "\dt"

# Manually run a migration
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /path/to/migration.sql
```

## Contact

For questions or issues, please refer to the project documentation or contact the development team.
