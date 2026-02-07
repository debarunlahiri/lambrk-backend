# Error Handling

All error responses use the **RFC 7807 Problem Detail** format.

---

## Error Response Shape

```json
{
  "type": "https://api.lambrk-backend.com/errors/not-found",
  "title": "Resource Not Found",
  "status": 404,
  "detail": "Post not found with id: '999'",
  "instance": "/api/posts/999",
  "timestamp": "2026-02-07T14:00:00Z"
}
```

Validation errors include an extra `fieldErrors` map:

```json
{
  "type": "https://api.lambrk-backend.com/errors/validation",
  "title": "Validation Error",
  "status": 400,
  "detail": "Validation failed",
  "fieldErrors": {
    "title": "Title is required",
    "sublambrkId": "must not be null"
  },
  "timestamp": "2026-02-07T14:00:00Z"
}
```

Content moderation errors include `violationCategories`:

```json
{
  "type": "https://api.lambrk-backend.com/errors/content-moderation",
  "title": "Content Moderation Violation",
  "status": 422,
  "detail": "Content rejected: hate speech detected",
  "violationCategories": ["hate_speech"],
  "timestamp": "2026-02-07T14:00:00Z"
}
```

---

## Error Catalogue

| Status | Type Slug              | Title                        | When                                      |
|--------|------------------------|------------------------------|-------------------------------------------|
| 400    | `validation`           | Validation Error             | Request body fails bean validation        |
| 401    | `bad-credentials`      | Authentication Failed        | Invalid username/password                 |
| 403    | `access-denied`        | Access Denied                | Missing required role                     |
| 403    | `unauthorized-action`  | Unauthorized Action          | E.g. editing another user's post          |
| 404    | `not-found`            | Resource Not Found           | Entity does not exist                     |
| 409    | `duplicate`            | Duplicate Resource           | Username/email/sublambrk name taken       |
| 422    | `content-moderation`   | Content Moderation Violation | AI moderation rejected the content        |
| 429    | `rate-limit`           | Rate Limit Exceeded          | Resilience4j RateLimiter triggered        |
| 429    | `bulkhead`             | Bulkhead Full                | Too many concurrent requests              |
| 503    | `circuit-breaker`      | Service Unavailable          | Circuit breaker is open                   |
| 500    | `internal`             | Internal Server Error        | Unhandled exception                       |

---

## Rate Limits

| Endpoint Group    | Limit        | Window |
|-------------------|--------------|--------|
| Post creation     | 100 requests | 1 min  |
| Comment creation  | 500 requests | 1 min  |
| Vote casting      | 1000 requests| 1 min  |
| User registration | 10 requests  | 1 min  |
| Search            | 50 requests  | 1 min  |
| File upload       | 20 requests  | 1 min  |

When a rate limit is exceeded the response includes no `Retry-After` header — clients should implement exponential backoff.

---

## Circuit Breaker States

| Service         | Failure Threshold | Window | Open Duration |
|-----------------|-------------------|--------|---------------|
| postService     | 50%               | 10     | 30 s          |
| commentService  | 60%               | 20     | 45 s          |
| userService     | 40%               | 15     | 20 s          |
| kafkaProducer   | 70%               | 5      | 60 s          |

---

## Observability

Every error increments a Micrometer counter:

```
errors_total{type="not_found"}
errors_total{type="validation"}
errors_total{type="rate_limit"}
errors_total{type="circuit_breaker"}
errors_total{type="content_moderation"}
errors_total{type="internal"}
```

These are available at `/actuator/prometheus`.
