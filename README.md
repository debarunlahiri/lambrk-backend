# Lambrk Backend - Video Streaming Platform

A modern microservices-based backend for Lambrk video streaming platform built with TypeScript, Express, and PostgreSQL.

## 📚 Documentation

- **[Quick Start Guide](./QUICK_START.md)** - Get up and running in 5 minutes
- **[API Documentation](./docs/README.md)** - Complete API reference organized by service
  - [Auth Service](./docs/auth-service.md)
  - [Video Service](./docs/video-service.md)
  - [Bitz Service](./docs/bitz-service.md)
  - [Posts Service](./docs/posts-service.md)
  - [Interaction Service](./docs/interaction-service.md)
- **[Architecture & Setup](./UPDATE_SUMMARY.md)** - Detailed architecture and deployment guide
- **[Consolidated API Reference](./API_DOCUMENTATION.md)** - Single-file documentation

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Quick Setup](#quick-setup)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [Running the Services](#running-the-services)
- [Docker Setup](#docker-setup)
- [Project Structure](#project-structure)

## Architecture

The project follows a microservices architecture with 6 specialized services:

- **API Gateway** (Port 3100): Routes requests and provides rate limiting
- **Auth Service** (Port 3101): Authentication, user management, OAuth
- **Video Service** (Port 3102): Full-length videos with quality management
- **Bitz Service** (Port 3103): Short vertical videos (TikTok/Reels style)
- **Posts Service** (Port 3104): Social media posts with images
- **Interaction Service** (Port 3105): Likes, comments, playlists, subscriptions, downloads, trending
- **Shared Package**: Common utilities and middleware
- **PostgreSQL Database**: Shared database with materialized views

## Features

### Content Types
- **Videos**: Full-length videos with multiple quality support (4K, 2K, HD, 720p, 480p, 360p)
- **Bitz**: Short vertical videos for mobile (TikTok/Reels style)
- **Posts**: Social media posts with text and images

### User Interactions
- **Universal Likes/Dislikes**: Toggle-based system for all content types
- **Nested Comments**: Comments with replies on all content
- **Playlists**: Custom playlists + auto Watch Later
- **Subscriptions**: Channel subscriptions with subscriber counts
- **Downloads**: Download tracking and management
- **Trending**: Algorithmic trending content

### Authentication
- Email/password authentication
- Google OAuth integration
- Firebase authentication support
- JWT with access and refresh tokens

### Technical Features
- Microservices architecture for scalability
- TypeScript for type safety
- PostgreSQL with materialized views
- Rate limiting and DDoS protection
- Docker support for easy deployment
- Comprehensive API documentation

## Services

1. **API Gateway** (Port 3100) - Request routing and rate limiting
2. **Auth Service** (Port 3101) - User authentication and management
3. **Video Service** (Port 3102) - Full-length video operations
4. **Bitz Service** (Port 3103) - Short vertical videos
5. **Posts Service** (Port 3104) - Social media posts
6. **Interaction Service** (Port 3105) - All user interactions

## Prerequisites

- Node.js 18+ and npm
- PostgreSQL 15+
- Docker and Docker Compose (recommended)
- Google OAuth credentials (optional, for Google login)
- Firebase credentials (optional, for mobile auth)

## Quick Setup

For detailed setup instructions, see [QUICK_START.md](./QUICK_START.md).

```bash
# 1. Start all services with Docker
docker-compose up -d

# 2. Run migrations
docker cp migrations/003_create_comprehensive_platform_schema.sql lambrk-postgres:/tmp/
docker exec -it lambrk-postgres psql -U lambrk_user -d lambrk -f /tmp/003_create_comprehensive_platform_schema.sql

# 3. Access API Gateway
curl http://localhost:3100/health
```

## Detailed Setup

### 1. Clone and Install Dependencies

```bash
# Install all dependencies (root and all services)
npm run install:all
```

### 2. Environment Configuration

Copy the example environment files and configure them:

```bash
# Root level
cp .env.example .env

# Auth service
cp services/auth-service/.env.example services/auth-service/.env

# Video service
cp services/video-service/.env.example services/video-service/.env

# Gateway
cp gateway/.env.example gateway/.env
```

### 3. Database Setup

Start PostgreSQL database:

```bash
# Using Docker Compose
docker-compose up postgres -d

# Or use your local PostgreSQL instance
```

### 4. Run Migrations

```bash
# Build shared package first
cd shared
npm run build

# Run auth service migrations
cd ../services/auth-service
npm run build
npm run migrate

# Run video service migrations
cd ../video-service
npm run build
npm run migrate
```

### 5. Start Services

```bash
# From root directory - start all services concurrently
npm run dev:all

# Or start individually:
npm run dev:auth      # Auth service on port 3101
npm run dev:video     # Video service on port 3102
npm run dev:gateway   # API Gateway on port 3100
```

## Environment Variables

### Root `.env`

```env
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=lambrk_user
POSTGRES_PASSWORD=lambrk_password
POSTGRES_DB=lambrk

JWT_SECRET=your-super-secret-jwt-key-change-in-production
JWT_EXPIRES_IN=7d
JWT_REFRESH_SECRET=your-super-secret-refresh-key-change-in-production
JWT_REFRESH_EXPIRES_IN=30d

GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_CALLBACK_URL=http://localhost:3101/api/auth/google/callback

AUTH_SERVICE_PORT=3101
VIDEO_SERVICE_PORT=3102
GATEWAY_PORT=3100

AUTH_SERVICE_URL=http://localhost:3101
VIDEO_SERVICE_URL=http://localhost:3102

CORS_ORIGIN=http://localhost:3100
```

## Database Setup

The database uses PostgreSQL with the following schema:

### Users Table
- `id` (UUID, Primary Key)
- `username` (VARCHAR, Unique)
- `email` (VARCHAR, Unique)
- `password` (VARCHAR, Hashed)
- `google_id` (VARCHAR, Unique, Nullable)
- `first_name`, `last_name`, `avatar` (VARCHAR/TEXT)
- `created_at`, `updated_at` (TIMESTAMP)

### Videos Table
- `id` (UUID, Primary Key)
- `title` (VARCHAR)
- `description` (TEXT)
- `url` (TEXT)
- `thumbnail_url` (TEXT)
- `duration` (INTEGER)
- `user_id` (UUID, Foreign Key to users)
- `views`, `likes` (INTEGER)
- `status` (VARCHAR: 'draft', 'published', 'processing')
- `created_at`, `updated_at` (TIMESTAMP)

## Running the Services

### Development Mode

```bash
# Start all services
npm run dev:all

# Individual services
cd services/auth-service && npm run dev
cd services/video-service && npm run dev
cd gateway && npm run dev
```

### Production Mode

```bash
# Build all services
npm run build

# Start services
cd services/auth-service && npm start
cd services/video-service && npm start
cd gateway && npm start
```

## API Documentation

For complete API documentation with detailed request/response examples, error codes, and data models, see **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)**.

### Quick Reference

**Base URLs:**
- API Gateway: `http://localhost:3100`
- Auth Service: `http://localhost:3101`
- Video Service: `http://localhost:3102`

**Authentication:**
Include JWT token in Authorization header:
```
Authorization: Bearer <access_token>
```

**Main Endpoints:**

**Auth Service:**
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signin` - User login
- `POST /api/auth/refresh-token` - Refresh access token
- `GET /api/auth/profile` - Get user profile (requires auth)
- `GET /api/auth/google` - Google OAuth login

**Video Service:**
- `GET /api/videos` - Get all videos
- `GET /api/videos/my-videos` - Get user's videos (requires auth)
- `GET /api/videos/:id` - Get video by ID
- `POST /api/videos` - Create video (requires auth)
- `PUT /api/videos/:id` - Update video (requires auth)
- `DELETE /api/videos/:id` - Delete video (requires auth)
- `POST /api/videos/:id/views` - Increment views
- `POST /api/videos/:id/likes` - Increment likes

For detailed API documentation including request/response examples, validation rules, and error codes, please refer to [API_DOCUMENTATION.md](./API_DOCUMENTATION.md).


## Docker Setup

### Using Docker Compose

Start all services with Docker:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL database
- Auth service
- Video service
- API Gateway

### Individual Docker Containers

Build and run individual services:

```bash
# Auth service
cd services/auth-service
docker build -t lambrk-auth-service .
docker run -p 3101:3101 lambrk-auth-service

# Video service
cd services/video-service
docker build -t lambrk-video-service .
docker run -p 3102:3102 lambrk-video-service

# Gateway
cd gateway
docker build -t lambrk-gateway .
docker run -p 3100:3100 lambrk-gateway
```

## Project Structure

```
lambrk-backend/
├── shared/                    # Shared utilities and types
│   ├── src/
│   │   ├── database/         # PostgreSQL connection pool
│   │   ├── utils/           # JWT, password hashing, errors
│   │   └── middleware/      # Auth & error handling middleware
│   └── package.json
├── services/
│   ├── auth-service/        # Authentication microservice
│   │   ├── src/
│   │   │   ├── models/      # User model
│   │   │   ├── queries/     # Database queries
│   │   │   ├── services/    # Auth business logic
│   │   │   ├── controllers/ # Request handlers
│   │   │   ├── routes/      # API routes
│   │   │   ├── config/      # Database & Passport config
│   │   │   └── migrations/  # Database migrations
│   │   └── package.json
│   └── video-service/       # Video microservice
│       ├── src/
│       │   ├── models/      # Video model
│       │   ├── queries/     # Database queries
│       │   ├── services/    # Video business logic
│       │   ├── controllers/ # Request handlers
│       │   ├── routes/      # API routes
│       │   ├── config/      # Database config
│       │   └── migrations/  # Database migrations
│       └── package.json
├── gateway/                  # API Gateway
│   ├── src/
│   │   └── index.ts         # Proxy middleware
│   └── package.json
├── docker-compose.yml        # Docker orchestration
├── package.json              # Root workspace config
└── README.md                 # This file
```

## Development

### Building the Project

```bash
# Build all services
npm run build

# Build individual service
cd services/auth-service && npm run build
```

### Running Migrations

```bash
# Auth service
cd services/auth-service
npm run build
npm run migrate

# Video service
cd services/video-service
npm run build
npm run migrate
```

### Testing

```bash
# Run tests for all services
npm test
```

## License

ISC

