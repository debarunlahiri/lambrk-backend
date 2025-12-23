# Auth Service API Documentation

Authentication and user management service for the Lambrk platform.

**Base URL**: `http://localhost:3101` (Direct) or `http://localhost:3100/api/auth` (Gateway)

## Table of Contents

- [Overview](#overview)
- [Authentication Methods](#authentication-methods)
- [Endpoints](#endpoints)
- [Data Models](#data-models)
- [Examples](#examples)

## Overview

The Auth Service handles:
- User registration and login
- JWT token generation and validation
- Google OAuth integration
- Firebase authentication
- User profile management

### Token Types

- **Access Token**: Short-lived (7 days) for API requests
- **Refresh Token**: Long-lived (30 days) for obtaining new access tokens

## Authentication Methods

1. **Email/Password**: Traditional username/email and password authentication
2. **Google OAuth**: Sign in with Google account
3. **Firebase Auth**: Mobile authentication via Firebase

## Endpoints

### 1. User Registration

Create a new user account.

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

**Validation Rules:**
- `username`: 3-30 characters, alphanumeric and underscores only, must be unique
- `email`: Valid email format, must be unique
- `password`: Minimum 8 characters, at least one uppercase, one lowercase, one number

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

*400 - Validation Error:*
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

*409 - Conflict:*
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

*401 - Unauthorized:*
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

*401 - Unauthorized:*
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

---

### 5. Google OAuth - Initiate

Start the Google OAuth authentication flow.

**Endpoint:** `GET /api/auth/google`

**Authentication:** Not required

**Description:** Redirects user to Google OAuth consent screen.

**Success:** HTTP 302 Redirect to Google OAuth

---

### 6. Google OAuth - Callback

Handle Google OAuth callback and redirect to frontend with tokens.

**Endpoint:** `GET /api/auth/google/callback`

**Authentication:** Not required (handled by Google OAuth)

**Success:** HTTP 302 Redirect to frontend

**Redirect URL Format:**
```
http://localhost:3100/auth/callback?accessToken=<token>&refreshToken=<token>
```

---

### 7. Firebase Authentication

Authenticate using Firebase ID token (for mobile apps).

**Endpoint:** `POST /api/auth/firebase`

**Authentication:** Not required

**Request Body:**
```json
{
  "idToken": "firebase-id-token"
}
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
      "avatar": "https://example.com/avatar.jpg"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

---

## Data Models

### User Model

```typescript
interface User {
  id: string;              // UUID
  username: string;         // Unique, 3-30 chars
  email: string;            // Unique, valid email
  password?: string;        // Hashed (never returned)
  googleId?: string;        // Google OAuth ID
  firstName?: string;       // Optional
  lastName?: string;        // Optional
  avatar?: string;          // URL to avatar image
  createdAt: Date;          // ISO 8601 timestamp
  updatedAt: Date;          // ISO 8601 timestamp
}
```

### Token Response

```typescript
interface TokenResponse {
  accessToken: string;      // JWT access token
  refreshToken: string;     // JWT refresh token
}
```

---

## Examples

### Complete Authentication Flow

```javascript
// 1. Register
const signupResponse = await fetch('http://localhost:3100/api/auth/signup', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'johndoe',
    email: 'john@example.com',
    password: 'SecurePass123'
  })
});

const { data: signupData } = await signupResponse.json();
const { accessToken, refreshToken } = signupData;

// 2. Use access token for authenticated requests
const profileResponse = await fetch('http://localhost:3100/api/auth/profile', {
  headers: {
    'Authorization': `Bearer ${accessToken}`
  }
});

// 3. Refresh token when access token expires
const refreshResponse = await fetch('http://localhost:3100/api/auth/refresh-token', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ refreshToken })
});

const { data: refreshData } = await refreshResponse.json();
const newAccessToken = refreshData.accessToken;
```

### Google OAuth Flow

```javascript
// 1. Redirect user to Google OAuth
window.location.href = 'http://localhost:3100/api/auth/google';

// 2. Handle callback (on your callback page)
const urlParams = new URLSearchParams(window.location.search);
const accessToken = urlParams.get('accessToken');
const refreshToken = urlParams.get('refreshToken');

// Store tokens and use for authenticated requests
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
```

---

## Rate Limiting

Auth endpoints have strict rate limiting:
- **signup/signin**: 5 requests per 15 minutes per IP
- **Other endpoints**: 100 requests per 15 minutes per IP

---

## Security Best Practices

1. **Store tokens securely**: Use httpOnly cookies or secure storage
2. **Never expose tokens**: Don't log or display tokens
3. **Refresh tokens proactively**: Refresh before access token expires
4. **Validate on every request**: Backend validates tokens on protected routes
5. **Use HTTPS in production**: Always use SSL/TLS for authentication

---

## Environment Variables

Required environment variables for Auth Service:

```env
# Database
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_USER=lambrk_user
POSTGRES_PASSWORD=lambrk_password
POSTGRES_DB=lambrk

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRES_IN=7d

# Google OAuth (optional)
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# Firebase (optional)
FIREBASE_PROJECT_ID=your-project-id
FIREBASE_PRIVATE_KEY=your-private-key
FIREBASE_CLIENT_EMAIL=your-client-email
```

---

## Error Codes Reference

| Status | Error | Description |
|--------|-------|-------------|
| 400 | Validation failed | Invalid input data |
| 401 | Invalid credentials | Wrong email/password |
| 401 | Invalid or expired token | Token validation failed |
| 409 | Email already registered | Duplicate email |
| 409 | Username already taken | Duplicate username |
| 503 | OAuth not configured | Google OAuth not set up |

---

[Back to Documentation Index](./README.md)
