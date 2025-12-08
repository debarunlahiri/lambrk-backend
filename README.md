# Lambrk Backend - Video Platform

A microservices-based Node.js backend for Lambrk video platform built with TypeScript, Express, and PostgreSQL.

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [Running the Services](#running-the-services)
- [API Documentation](#api-documentation)
- [Docker Setup](#docker-setup)
- [Project Structure](#project-structure)

> **📖 For complete API reference, see [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)**

## Architecture

The project follows a microservices architecture with the following components:

- **API Gateway** (Port 3100): Single entry point that routes requests to appropriate services
- **Auth Service** (Port 3101): Handles user authentication and authorization
- **Video Service** (Port 3102): Manages video operations and metadata
- **Shared Package**: Common utilities, types, and middleware used across services
- **PostgreSQL Database**: Shared database for all services

## Features

- User authentication with username/password
- Google OAuth integration
- JWT-based authentication with access and refresh tokens
- Video CRUD operations
- User-specific video management
- Video views and likes tracking
- Microservices architecture
- TypeScript for type safety
- PostgreSQL database
- Docker support

## Prerequisites

- Node.js 18+ and npm
- PostgreSQL 15+
- Docker and Docker Compose (optional, for containerized setup)
- Google OAuth credentials (for Google login)

## Setup

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

