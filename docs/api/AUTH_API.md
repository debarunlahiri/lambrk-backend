# Authentication API

Base URL: `/api/auth`

All authentication endpoints are **public** (no JWT required).

---

## POST `/api/auth/register`

Register a new user account.

### Request Body

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "displayName": "John Doe"
}
```

### Validation Rules

| Field       | Rule                                      |
|-------------|-------------------------------------------|
| username    | Required, 3–50 chars                      |
| email       | Required, valid email format               |
| password    | Required, min 8 chars                      |
| displayName | Optional                                   |

### Response `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 4,
    "username": "john_doe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-02-07T13:30:00Z",
    "updatedAt": "2026-02-07T13:30:00Z"
  }
}
```

### Error Responses

| Status | Condition                        |
|--------|----------------------------------|
| 400    | Validation failed                |
| 409    | Username or email already exists |
| 429    | Rate limit exceeded (10/min)     |

---

## POST `/api/auth/login`

Authenticate an existing user.

### Request Body

```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

### Response `200 OK`

Same shape as register response.

### Error Responses

| Status | Condition           |
|--------|---------------------|
| 400    | Validation failed   |
| 401    | Invalid credentials |

---

## POST `/api/auth/refresh`

Exchange a refresh token for a new access + refresh token pair.

### Request Body

Raw string — the refresh token.

```
eyJhbGciOiJIUzUxMiJ9...
```

### Response `200 OK`

Same shape as login response with new tokens.

### Error Responses

| Status | Condition              |
|--------|------------------------|
| 401    | Invalid refresh token  |

---

## Token Details

| Property        | Value                |
|-----------------|----------------------|
| Algorithm       | HS512                |
| Access TTL      | 24 hours (86 400 s)  |
| Refresh TTL     | 7 days (604 800 s)   |
| Header          | `Authorization: Bearer <token>` |

### JWT Claims

```json
{
  "sub": "john_doe",
  "iat": 1738934400,
  "exp": 1739020800,
  "roles": ["ROLE_USER"],
  "tokenType": "access"
}
```
