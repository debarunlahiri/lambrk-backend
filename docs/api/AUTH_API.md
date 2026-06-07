# Auth API

Base path: `/api/auth`. These endpoints are public.

---

### POST `/api/auth/register`

Register a new account and return JWT tokens.

**Auth:** Public

**What to send**

| Parameter     | Location | Type   | Required | Description                  |
| ------------- | -------- | ------ | -------- | ---------------------------- |
| `username`    | Body     | string | **Yes**  | Unique username (3–50 chars) |
| `email`       | Body     | string | **Yes**  | Valid email address          |
| `password`    | Body     | string | **Yes**  | Minimum 8 characters         |
| `displayName` | Body     | string | No       | Public display name          |

**Request body**

```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123",
  "displayName": "John Doe"
}
```

**Response**

| Status | Body           | Description              |
| ------ | -------------- | ------------------------ |
| `200`  | `AuthResponse` | Tokens and user info     |
| `400`  | error          | Validation failure       |
| `409`  | error          | Duplicate username/email |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123",
  "displayName": "John Doe"
}'
```

**Response body**

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "username": "johndoe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z"
  }
}
```

---

### POST `/api/auth/login`

Authenticate with username and password.

**Auth:** Public

**What to send**

| Parameter  | Location | Type   | Required | Description         |
| ---------- | -------- | ------ | -------- | ------------------- |
| `username` | Body     | string | **Yes**  | Registered username |
| `password` | Body     | string | **Yes**  | Account password    |

**Request body**

```json
{
  "username": "johndoe",
  "password": "securePassword123"
}
```

**Response**

| Status | Body           | Description          |
| ------ | -------------- | -------------------- |
| `200`  | `AuthResponse` | Tokens and user info |
| `401`  | error          | Bad credentials      |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "johndoe",
  "password": "securePassword123"
}'
```

**Response body**

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "username": "johndoe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z"
  }
}
```

---

### POST `/api/auth/refresh`

Refresh tokens using the raw refresh-token string.

**Auth:** Public

**What to send**

| Parameter      | Location | Type   | Required | Description           |
| -------------- | -------- | ------ | -------- | --------------------- |
| `refreshToken` | Body     | string | **Yes**  | Raw JWT refresh token |

**Request body**

```text
eyJhbGciOi...
```

**Response**

| Status | Body           | Description                      |
| ------ | -------------- | -------------------------------- |
| `200`  | `AuthResponse` | New tokens and user info         |
| `401`  | error          | Invalid or expired refresh token |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/auth/refresh' \
  -H 'Content-Type: text/plain' \
  -d 'eyJhbGciOi...'
```

**Response body**

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "username": "johndoe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z"
  }
}
```
