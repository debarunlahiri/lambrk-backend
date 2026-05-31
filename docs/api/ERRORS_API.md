# Error Responses

Most API failures return Spring `ProblemDetail` JSON. Feed-specific exceptions may return a map-shaped error, and feed generation failures return an empty successful feed response.

## Standard Error Response

**Example request**

```bash
curl -X GET 'http://localhost:9500/api/users/f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a99' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "https://api.lambrk-backend.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "User not found with id: 999999",
  "timestamp": "2026-05-02T10:00:00Z"
}
```

## Validation Error Response

**Example request**

```bash
curl -X POST 'http://localhost:9500/api/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{"username":"ab","email":"bad","password":"short"}'
```

**Response**

```json
{
  "type": "https://api.lambrk-backend.com/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed",
  "timestamp": "2026-05-02T10:00:00Z",
  "fieldErrors": {
    "username": "Username must be between 3 and 50 characters",
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

## Error Types

| HTTP | Title | Type suffix | Trigger |
| --- | --- | --- | --- |
| 400 | Validation Error | `/errors/validation` | Bean validation failure. |
| 401 | Authentication Failed | `/errors/bad-credentials` | Bad credentials. |
| 403 | Access Denied | `/errors/access-denied` | Missing role or denied access. |
| 403 | Unauthorized Action | `/errors/unauthorized-action` | Domain permission failure. |
| 404 | Resource Not Found | `/errors/not-found` | Missing entity. |
| 409 | Duplicate Resource | `/errors/duplicate` | Unique/conflict condition. |
| 422 | Content Moderation Violation | `/errors/content-moderation` | Moderation rejection. |
| 429 | Rate Limit Exceeded | `/errors/rate-limit` | Resilience4j rate limiter. |
| 429 | Bulkhead Full | `/errors/bulkhead` | Too many concurrent requests. |
| 503 | Service Unavailable | `/errors/circuit-breaker` | Circuit breaker open. |
| 500 | Internal Server Error | `/errors/internal` | Unhandled exception. |

## Feed Map Error

```json
{
  "timestamp": "2026-05-02T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request"
}
```
