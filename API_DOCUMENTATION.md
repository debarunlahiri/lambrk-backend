# Lambrk Backend API Documentation

Complete API reference for Lambrk video platform backend services.

## Table of Contents

- [Base URLs](#base-urls)
- [Authentication](#authentication)
- [Error Handling](#error-handling)
- [Auth Service API](#auth-service-api)
- [Video Service API](#video-service-api)

## Base URLs

- **API Gateway**: `http://localhost:3100`
- **Auth Service (Direct)**: `http://localhost:3101`
- **Video Service (Direct)**: `http://localhost:3102`

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

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Validation error |
| 401 | Unauthorized - Missing or invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error |
| 503 | Service Unavailable |

---

## Auth Service API

### 1. User Registration

Register a new user account with username and password.

**Endpoint:** `POST /api/auth/signup`

**Authentication:** Not required

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Validation Rules:**
- `username`: 
  - Required
  - 3-30 characters
  - Alphanumeric and underscores only
  - Must be unique
- `email`: 
  - Required
  - Valid email format
  - Must be unique
- `password`: 
  - Required
  - Minimum 8 characters
  - Must contain at least one uppercase letter
  - Must contain at least one lowercase letter
  - Must contain at least one number

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": null,
      "lastName": null,
      "avatar": null,
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Responses:**

**400 - Validation Error:**
```json
{
  "success": false,
  "error": {
    "message": "Validation failed",
    "errors": [
      {
        "msg": "Username must be between 3 and 30 characters",
        "param": "username",
        "location": "body"
      }
    ]
  }
}
```

**409 - Conflict:**
```json
{
  "success": false,
  "error": {
    "message": "Email already registered",
    "statusCode": 409
  }
}
```

---

### 2. User Login

Authenticate user with email/username and password.

**Endpoint:** `POST /api/auth/signin`

**Authentication:** Not required

**Request Body (Email):**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Request Body (Username):**
```json
{
  "username": "johndoe",
  "password": "SecurePass123"
}
```

**cURL Example (Email):**
```bash
curl -X POST http://localhost:3100/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**cURL Example (Username):**
```bash
curl -X POST http://localhost:3100/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

**Validation Rules:**
- Either `email` OR `username` is required
- `password`: Required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "avatar": "https://example.com/avatar.jpg",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Responses:**

**400 - Validation Error:**
```json
{
  "success": false,
  "error": {
    "message": "Validation failed",
    "errors": [
      {
        "msg": "Either email or username is required",
        "param": "body",
        "location": "body"
      }
    ]
  }
}
```

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Invalid credentials",
    "statusCode": 401
  }
}
```

---

### 3. Refresh Access Token

Obtain a new access token using a valid refresh token.

**Endpoint:** `POST /api/auth/refresh-token`

**Authentication:** Not required

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

**Error Responses:**

**400 - Bad Request:**
```json
{
  "success": false,
  "error": {
    "message": "Refresh token is required",
    "statusCode": 400
  }
}
```

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Invalid or expired token",
    "statusCode": 401
  }
}
```

---

### 4. Get User Profile

Get the authenticated user's profile information.

**Endpoint:** `GET /api/auth/profile`

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <access_token>
```

**cURL Example:**
```bash
curl -X GET http://localhost:3100/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "johndoe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "avatar": "https://example.com/avatar.jpg",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error Responses:**

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Unauthorized",
    "statusCode": 401
  }
}
```

---

### 5. Google OAuth Login - Initiate

Start the Google OAuth authentication flow.

**Endpoint:** `GET /api/auth/google`

**Authentication:** Not required

**Description:** Redirects user to Google OAuth consent screen.

**cURL Example:**
```bash
curl -X GET http://localhost:3100/api/auth/google \
  -L -v
```
> Note: Use `-L` to follow redirects and `-v` for verbose output

**Success Response:** HTTP 302 Redirect to Google OAuth

**Query Parameters:** None

---

### 6. Google OAuth Callback

Handle Google OAuth callback and redirect to frontend with tokens.

**Endpoint:** `GET /api/auth/google/callback`

**Authentication:** Not required (handled by Google OAuth)

**Description:** This endpoint is called by Google after user authentication. It processes the OAuth response and redirects to the frontend with tokens.

**Success Response:** HTTP 302 Redirect to frontend

**Redirect URL Format:**
```
http://localhost:3100/auth/callback?accessToken=<token>&refreshToken=<token>
```

**Error Responses:**

**401 - Authentication Failed:**
```json
{
  "success": false,
  "error": {
    "message": "Google authentication failed",
    "statusCode": 401
  }
}
```

---

### 7. Google OAuth Failure

Endpoint for handling Google OAuth failures.

**Endpoint:** `GET /api/auth/google/failure`

**Authentication:** Not required

**Error Response (401):**
```json
{
  "success": false,
  "error": {
    "message": "Google authentication failed",
    "statusCode": 401
  }
}
```

---

## Video Service API

### 1. Get All Videos

Retrieve a paginated list of all videos.

**Endpoint:** `GET /api/videos`

**Authentication:** Not required

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | integer | No | 20 | Number of videos per page |
| `offset` | integer | No | 0 | Pagination offset |
| `status` | string | No | - | Filter by status: 'draft', 'published', 'processing' |

**Example Request:**
```
GET /api/videos?limit=10&offset=0&status=published
```

**cURL Example:**
```bash
curl -X GET "http://localhost:3100/api/videos?limit=10&offset=0&status=published"
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
        "description": "This is a great video about technology",
        "url": "https://example.com/videos/video.mp4",
        "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
        "duration": 3600,
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 1250,
        "likes": 89,
        "status": "published",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 2. Get User's Videos

Get all videos created by the authenticated user.

**Endpoint:** `GET /api/videos/my-videos`

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <access_token>
```

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `limit` | integer | No | 20 | Number of videos per page |
| `offset` | integer | No | 0 | Pagination offset |

**Example Request:**
```
GET /api/videos/my-videos?limit=10&offset=0
```

**cURL Example:**
```bash
curl -X GET "http://localhost:3100/api/videos/my-videos?limit=10&offset=0" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

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
        "status": "draft",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

**Error Responses:**

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Unauthorized",
    "statusCode": 401
  }
}
```

---

### 3. Get Video by ID

Retrieve a specific video by its unique identifier.

**Endpoint:** `GET /api/videos/:id`

**Authentication:** Not required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Video unique identifier |

**Example Request:**
```
GET /api/videos/550e8400-e29b-41d4-a716-446655440000
```

**cURL Example:**
```bash
curl -X GET http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000
```

**cURL Example (with quality):**
```bash
# Get video with specific quality
curl -X GET "http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000?quality=720p"

# Get video with all available qualities
curl -X GET "http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000?includeQualities=true"
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "video": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My Awesome Video",
      "description": "This is a great video about technology",
      "url": "https://example.com/videos/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
      "duration": 3600,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 1250,
      "likes": 89,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error Responses:**

**404 - Not Found:**
```json
{
  "success": false,
  "error": {
    "message": "Video not found",
    "statusCode": 404
  }
}
```

---

### 4. Create Video

Create a new video entry.

**Endpoint:** `POST /api/videos`

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <access_token>
```

**Request Body:**
```json
{
  "title": "My New Video",
  "description": "This is a description of my video",
  "url": "https://example.com/videos/video.mp4",
  "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
  "duration": 3600,
  "status": "draft"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/videos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "title": "My New Video",
    "description": "This is a description of my video",
    "url": "https://example.com/videos/video.mp4",
    "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
    "duration": 3600,
    "status": "draft"
  }'
```

**Validation Rules:**
- `title`: 
  - Required
  - Non-empty string
- `url`: 
  - Required
  - Valid URL format
- `description`: Optional string
- `thumbnailUrl`: Optional, valid URL format
- `duration`: Optional integer (in seconds)
- `status`: Optional, must be one of: 'draft', 'published', 'processing' (default: 'draft')

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "video": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My New Video",
      "description": "This is a description of my video",
      "url": "https://example.com/videos/video.mp4",
      "thumbnailUrl": "https://example.com/thumbnails/thumb.jpg",
      "duration": 3600,
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 0,
      "likes": 0,
      "status": "draft",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error Responses:**

**400 - Validation Error:**
```json
{
  "success": false,
  "error": {
    "message": "Validation failed",
    "errors": [
      {
        "msg": "Title is required",
        "param": "title",
        "location": "body"
      }
    ]
  }
}
```

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Unauthorized",
    "statusCode": 401
  }
}
```

---

### 5. Update Video

Update an existing video. Users can only update their own videos.

**Endpoint:** `PUT /api/videos/:id`

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Video unique identifier |

**Request Body:**
```json
{
  "title": "Updated Video Title",
  "description": "Updated description",
  "thumbnailUrl": "https://example.com/thumbnails/new-thumb.jpg",
  "status": "published"
}
```

**cURL Example:**
```bash
curl -X PUT http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "title": "Updated Video Title",
    "description": "Updated description",
    "thumbnailUrl": "https://example.com/thumbnails/new-thumb.jpg",
    "status": "published"
  }'
```

**Validation Rules:**
- All fields are optional
- `title`: If provided, must be non-empty string
- `status`: If provided, must be one of: 'draft', 'published', 'processing'

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
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-02T00:00:00.000Z"
    }
  }
}
```

**Error Responses:**

**400 - Validation Error:**
```json
{
  "success": false,
  "error": {
    "message": "Validation failed",
    "errors": [
      {
        "msg": "Title cannot be empty",
        "param": "title",
        "location": "body"
      }
    ]
  }
}
```

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Unauthorized",
    "statusCode": 401
  }
}
```

**403 - Forbidden:**
```json
{
  "success": false,
  "error": {
    "message": "You can only update your own videos",
    "statusCode": 403
  }
}
```

**404 - Not Found:**
```json
{
  "success": false,
  "error": {
    "message": "Video not found",
    "statusCode": 404
  }
}
```

---

### 6. Delete Video

Delete a video. Users can only delete their own videos.

**Endpoint:** `DELETE /api/videos/:id`

**Authentication:** Required

**Headers:**
```
Authorization: Bearer <access_token>
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Video unique identifier |

**Example Request:**
```
DELETE /api/videos/550e8400-e29b-41d4-a716-446655440000
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Video deleted successfully"
}
```

**Error Responses:**

**401 - Unauthorized:**
```json
{
  "success": false,
  "error": {
    "message": "Unauthorized",
    "statusCode": 401
  }
}
```

**403 - Forbidden:**
```json
{
  "success": false,
  "error": {
    "message": "You can only delete your own videos",
    "statusCode": 403
  }
}
```

**404 - Not Found:**
```json
{
  "success": false,
  "error": {
    "message": "Video not found",
    "statusCode": 404
  }
}
```

---

### 7. Increment Views

Increment the view count for a video.

**Endpoint:** `POST /api/videos/:id/views`

**Authentication:** Not required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Video unique identifier |

**Example Request:**
```
POST /api/videos/550e8400-e29b-41d4-a716-446655440000/views
```

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/views
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Views incremented"
}
```

**Error Responses:**

**404 - Not Found:**
```json
{
  "success": false,
  "error": {
    "message": "Video not found",
    "statusCode": 404
  }
}
```

---

### 8. Increment Likes

Increment the like count for a video.

**Endpoint:** `POST /api/videos/:id/likes`

**Authentication:** Not required

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | UUID | Yes | Video unique identifier |

**Example Request:**
```
POST /api/videos/550e8400-e29b-41d4-a716-446655440000/likes
```

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/likes
```

**Success Response (200):**
```json
{
  "success": true,
  "message": "Likes incremented"
}
```

**Error Responses:**

**404 - Not Found:**
```json
{
  "success": false,
  "error": {
    "message": "Video not found",
    "statusCode": 404
  }
}
```

---

### 9. Video Quality Management

The video service supports multiple quality versions for each video (like YouTube). The following endpoints allow you to manage video qualities.

#### 9.1. Add Video Quality

Add a new quality version to a video.

**Endpoint:** `POST /api/videos/:videoId/qualities`

**Authentication:** Required

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/qualities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
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
  }'
```

#### 9.2. Get All Video Qualities

Get all quality versions for a video.

**Endpoint:** `GET /api/videos/:videoId/qualities`

**Authentication:** Not required

**cURL Example:**
```bash
curl -X GET http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/qualities
```

#### 9.3. Get Specific Video Quality

Get a specific quality version by quality type.

**Endpoint:** `GET /api/videos/:videoId/qualities/:quality`

**Authentication:** Not required

**cURL Example:**
```bash
curl -X GET http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/qualities/720p
```

#### 9.4. Update Video Quality

Update a video quality version.

**Endpoint:** `PUT /api/videos/:videoId/qualities/:qualityId`

**Authentication:** Required

**cURL Example:**
```bash
curl -X PUT http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/qualities/660e8400-e29b-41d4-a716-446655440001 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "status": "ready",
    "bitrate": 6000
  }'
```

#### 9.5. Delete Video Quality

Delete a video quality version.

**Endpoint:** `DELETE /api/videos/:videoId/qualities/:qualityId`

**Authentication:** Required

**cURL Example:**
```bash
curl -X DELETE http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/qualities/660e8400-e29b-41d4-a716-446655440001 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 9.6. Set Default Quality

Set a quality version as the default for a video.

**Endpoint:** `POST /api/videos/:videoId/qualities/:qualityId/set-default`

**Authentication:** Required

**cURL Example:**
```bash
curl -X POST http://localhost:3100/api/videos/550e8400-e29b-41d4-a716-446655440000/qualities/660e8400-e29b-41d4-a716-446655440001/set-default \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**Available Quality Types:**
- `144p`, `240p`, `360p`, `480p`, `720p`, `1080p`, `1440p`, `2160p`, `original`

---

## Data Models

### User Model

```typescript
{
  id: string;              // UUID
  username: string;         // Unique, 3-30 chars
  email: string;            // Unique, valid email
  password?: string;         // Hashed (never returned in responses)
  googleId?: string;        // Google OAuth ID
  firstName?: string;        // Optional
  lastName?: string;         // Optional
  avatar?: string;           // URL to avatar image
  createdAt: Date;          // ISO 8601 timestamp
  updatedAt: Date;          // ISO 8601 timestamp
}
```

### Video Model

```typescript
{
  id: string;               // UUID
  title: string;            // Required
  description?: string;     // Optional
  url: string;              // Required, video URL
  thumbnailUrl?: string;    // Optional, thumbnail URL
  duration?: number;        // Optional, in seconds
  userId: string;           // UUID, foreign key to users
  views: number;            // Integer, default: 0
  likes: number;            // Integer, default: 0
  status: 'draft' | 'published' | 'processing';  // Video status
  createdAt: Date;         // ISO 8601 timestamp
  updatedAt: Date;          // ISO 8601 timestamp
}
```

---

## Rate Limiting

Rate limiting is implemented to protect the API from abuse:

- **Gateway**: 300 requests per 15 minutes per IP
- **Auth Service**: 100 requests per 15 minutes per IP
- **Auth Endpoints** (signup/signin): 5 requests per 15 minutes per IP
- **Video Service**: 200 requests per 15 minutes per IP
- **Video Operations** (create/update/delete): 10 operations per hour per IP

When rate limits are exceeded, you'll receive a `429 Too Many Requests` response.

## Versioning

API versioning is not currently implemented. All endpoints are under `/api/` prefix.

## Support

For issues or questions, please refer to the main README.md file or contact the development team.

