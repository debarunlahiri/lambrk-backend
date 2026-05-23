# Auth API

Base path: `/api/auth`. These endpoints are public.

### POST `/api/auth/register`

Register a new account and return JWT tokens.

**Auth:** Public

**Request body**

```json
{"username":"johndoe","email":"john@example.com","password":"securePassword123","displayName":"John Doe"}
```

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

**Response**

```json
{"accessToken":"<jwt>","refreshToken":"<jwt>","tokenType":"Bearer","expiresIn":86400,"user":{"id":1,"username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":0,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"}}
```
### POST `/api/auth/login`

Authenticate with username and password.

**Auth:** Public

**Request body**

```json
{"username":"johndoe","password":"securePassword123"}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "johndoe",
  "password": "securePassword123"
}'
```

**Response**

```json
{"accessToken":"<jwt>","refreshToken":"<jwt>","tokenType":"Bearer","expiresIn":86400,"user":{"id":1,"username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":0,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"}}
```
### POST `/api/auth/refresh`

Refresh tokens using the raw refresh-token string.

**Auth:** Public

**Request body**

```text
eyJhbGciOi...
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/auth/refresh' \
  -H 'Content-Type: text/plain' \
  -d 'eyJhbGciOi...'
```

**Response**

```json
{"accessToken":"<jwt>","refreshToken":"<jwt>","tokenType":"Bearer","expiresIn":86400,"user":{"id":1,"username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":0,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"}}
```
