# Lambrk Backend - Quick Start Guide

## Fast Setup (5 minutes)

### Step 1: Run with Docker (Easiest)

```bash
# Clone and navigate to project
cd lambrk-backend

# Start all services
docker-compose up -d

# Wait for services to be healthy (about 30 seconds)
docker-compose ps

# Run migrations
docker cp migrations/003_create_comprehensive_platform_schema.sql lambrk-postgres:/tmp/
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /tmp/003_create_comprehensive_platform_schema.sql
```

### Step 2: Test the API

```bash
# Register a user
curl -X POST http://localhost:4400/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123"
  }'

# Response will include accessToken - save it!
```

### Step 3: Create Content

```bash
# Create a video (replace YOUR_ACCESS_TOKEN)
curl -X POST http://localhost:4400/api/videos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "title": "My First Video",
    "url": "https://example.com/video.mp4",
    "description": "This is my first video",
    "status": "published"
  }'

# Response will include video ID - save it!
```

### Step 4: Interact with Content

```bash
# Like the video (replace VIDEO_ID and YOUR_ACCESS_TOKEN)
curl -X POST http://localhost:4400/api/likes \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "contentType": "video",
    "contentId": "VIDEO_ID",
    "likeType": "like"
  }'

# Add a comment
curl -X POST http://localhost:4400/api/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "contentType": "video",
    "contentId": "VIDEO_ID",
    "commentText": "Great video!"
  }'

# Subscribe to the channel (use user ID from video response)
curl -X POST http://localhost:4400/api/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "channelId": "USER_ID"
  }'
```

## Service URLs

- **API Gateway**: http://localhost:4400 (use this for all requests)
- **Auth Service**: http://localhost:4401 (direct access)
- **Video Service**: http://localhost:4402 (direct access)
- **Bitz Service**: http://localhost:4403 (direct access)
- **Posts Service**: http://localhost:4404 (direct access)
- **Interaction Service**: http://localhost:4405 (direct access)

## Common Operations

### Authentication

```bash
# Sign up
POST /api/auth/signup
{
  "username": "user123",
  "email": "user@example.com",
  "password": "SecurePass123"
}

# Sign in
POST /api/auth/signin
{
  "email": "user@example.com",
  "password": "SecurePass123"
}

# Get profile
GET /api/auth/profile
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
```

### Content Management

```bash
# Create video
POST /api/videos
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "title": "Video Title",
  "url": "https://example.com/video.mp4",
  "status": "published"
}

# Create bitz (short video)
POST /api/bitz
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "title": "Bitz Title",
  "url": "https://example.com/bitz.mp4",
  "duration": 30
}

# Create post
POST /api/posts
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "title": "Post Title",
  "content": "Post content here...",
  "imageUrl": "https://example.com/image.jpg"
}

# Get all videos
GET /api/videos?limit=20&offset=0

# Get all bitz
GET /api/bitz?limit=20&offset=0

# Get all posts
GET /api/posts?limit=20&offset=0
```

### Interactions

```bash
# Like content
POST /api/likes
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "contentType": "video",
  "contentId": "uuid",
  "likeType": "like"
}

# Add comment
POST /api/comments
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "contentType": "video",
  "contentId": "uuid",
  "commentText": "Great content!"
}

# Get comments
GET /api/comments/video/VIDEO_ID

# Subscribe to channel
POST /api/subscriptions
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "channelId": "uuid"
}

# Add to playlist
POST /api/playlists/PLAYLIST_ID/items
Headers: Authorization: Bearer YOUR_ACCESS_TOKEN
{
  "contentType": "video",
  "contentId": "uuid"
}
```

### Trending & Discovery

```bash
# Get trending videos
GET /api/trending/videos?limit=20

# Get trending bitz
GET /api/trending/bitz?limit=20

# Get trending posts
GET /api/trending/posts?limit=20

# Refresh trending data (admin)
POST /api/trending/refresh
```

## Development Tips

### Check Service Health

```bash
# Check all services
curl http://localhost:4400/health  # Gateway
curl http://localhost:4401/health  # Auth
curl http://localhost:4402/health  # Video
curl http://localhost:4403/health  # Bitz
curl http://localhost:4404/health  # Posts
curl http://localhost:4405/health  # Interaction
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f auth-service
docker-compose logs -f video-service
docker-compose logs -f bitz-service
docker-compose logs -f posts-service
docker-compose logs -f interaction-service
docker-compose logs -f gateway
```

### Database Access

```bash
# Connect to database
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk

# List tables
\dt

# View table structure
\d videos
\d bitz
\d posts
\d likes
\d comments
\d playlists

# Exit
\q
```

### Stop Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

## Troubleshooting

### Port Already in Use

```bash
# Check what's using the port
lsof -i :4400  # or other port numbers

# Kill the process
kill -9 PID
```

### Database Connection Failed

```bash
# Restart database
docker-compose restart postgres

# Check database logs
docker-compose logs postgres
```

### Service Won't Start

```bash
# Rebuild the service
docker-compose build SERVICE_NAME
docker-compose up -d SERVICE_NAME

# Example
docker-compose build auth-service
docker-compose up -d auth-service
```

## Next Steps

1. Read the organized [API Documentation](./docs/README.md) by service
   - [Auth Service](./docs/auth-service.md)
   - [Video Service](./docs/video-service.md)
   - [Bitz Service](./docs/bitz-service.md)
   - [Posts Service](./docs/posts-service.md)
   - [Interaction Service](./docs/interaction-service.md)
2. Read `UPDATE_SUMMARY.md` for architecture details
3. Set up frontend to consume these APIs
4. Configure Google OAuth and Firebase (optional)
5. Set up CDN for video delivery (production)
6. Configure automated trending refresh (cron job)

## Production Checklist

- [ ] Change JWT_SECRET to a strong random key
- [ ] Set up proper environment variables
- [ ] Configure Google OAuth credentials
- [ ] Configure Firebase credentials
- [ ] Set up SSL/TLS certificates
- [ ] Configure CDN for video delivery
- [ ] Set up database backups
- [ ] Configure monitoring and alerting
- [ ] Set up log aggregation
- [ ] Configure automated trending refresh
- [ ] Set up rate limiting rules
- [ ] Configure CORS for production domains

## Need Help?

- Check [API Documentation](./docs/README.md) for complete API reference (organized by service)
- Check `UPDATE_SUMMARY.md` for architecture and setup details
- Check `API_DOCUMENTATION.md` for consolidated single-file reference
- Check service logs: `docker-compose logs -f SERVICE_NAME`
- Check database: `docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk`
