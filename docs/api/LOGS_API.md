# Logging API

Base URL: `/api/logs`

**ADMIN ONLY** - All endpoints require `ADMIN` role.

Comprehensive request/response logging system that captures:
- All HTTP requests and responses
- Request/response bodies
- Headers (with sensitive data redacted)
- User information (authenticated and anonymous)
- Response times
- Exceptions and stack traces
- Correlation IDs for request tracing

Logs are stored in:
1. **PostgreSQL database** (`api_logs` table)
2. **Log files** (`logs/api-logs.log`)

---

## GET `/api/logs`

Get all API logs ordered by timestamp (most recent first).

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 50 | Page size |
| sort | string | timestamp,desc | Sort field and direction |

### Response `200 OK`

```json
{
  "content": [
    {
      "id": 1001,
      "correlationId": "550e8400-e29b-41d4-a716-446655440000",
      "timestamp": "2026-02-07T20:30:00Z",
      "logLevel": "INFO",
      "method": "POST",
      "endpoint": "/api/posts",
      "fullUrl": "https://api.example.com/api/posts",
      "queryString": null,
      "requestHeaders": "{\"Content-Type\":\"application/json\",\"X-Correlation-Id\":\"550e8400...\"}",
      "requestBody": "{\"title\":\"My Post\",\"content\":\"Hello World\"}",
      "responseHeaders": "{\"Content-Type\":\"application/json\"}",
      "responseBody": "{\"id\":123,\"title\":\"My Post\"...}",
      "statusCode": 201,
      "responseTimeMs": 145,
      "ipAddress": "192.168.1.100",
      "userAgent": "Mozilla/5.0...",
      "userId": 2,
      "username": "john_doe",
      "isAuthenticated": true,
      "exceptionMessage": null,
      "exceptionStackTrace": null,
      "source": "API",
      "serviceName": "lambrk-backend",
      "createdAt": "2026-02-07T20:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 50,
    "totalElements": 1000,
    "totalPages": 20
  }
}
```

---

## GET `/api/logs/user/{userId}`

Get all logs for a specific user.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |

### Response `200 OK`

Paginated logs for the specified user.

---

## GET `/api/logs/endpoint`

Get logs filtered by endpoint path.

### Query Parameters

| Param | Type | Description |
|-------|------|-------------|
| path | String | Endpoint path to search (e.g., `/api/posts`) |
| page | int | Page number |
| size | int | Page size |

### Example

```
GET /api/logs/endpoint?path=/api/posts&size=20
```

---

## GET `/api/logs/method/{method}`

Get logs filtered by HTTP method.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| method | String | HTTP method: GET, POST, PUT, DELETE, etc. |

### Example

```
GET /api/logs/method/POST
```

---

## GET `/api/logs/status/{statusCode}`

Get logs filtered by HTTP status code.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| statusCode | Integer | HTTP status code: 200, 404, 500, etc. |

### Example

```
GET /api/logs/status/500
```

---

## GET `/api/logs/errors`

Get all error logs (4xx and 5xx status codes).

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 50 | Page size |

---

## GET `/api/logs/exceptions`

Get logs that contain exception information.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 50 | Page size |

---

## GET `/api/logs/anonymous`

Get logs from unauthenticated (anonymous) requests.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 50 | Page size |

---

## GET `/api/logs/authenticated`

Get logs from authenticated requests only.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page | int | 0 | Page number |
| size | int | 50 | Page size |

---

## Log Entry Fields

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Unique log entry ID |
| correlationId | String | Request correlation ID for tracing |
| timestamp | Instant | When the request was received |
| logLevel | String | INFO, WARN, ERROR |
| method | String | HTTP method |
| endpoint | String | Request path |
| fullUrl | String | Complete URL |
| queryString | String | URL query parameters |
| requestHeaders | String | Request headers (JSON, sensitive headers redacted) |
| requestBody | String | Request body (truncated if large) |
| responseHeaders | String | Response headers (JSON) |
| responseBody | String | Response body (truncated if large) |
| statusCode | Integer | HTTP status code |
| responseTimeMs | Long | Request processing time in milliseconds |
| ipAddress | String | Client IP address |
| userAgent | String | Client user agent string |
| userId | Long | User ID (if authenticated) |
| username | String | Username (if authenticated) |
| isAuthenticated | Boolean | Whether user was authenticated |
| exceptionMessage | String | Exception message (if error) |
| exceptionStackTrace | String | Full stack trace (if error) |
| source | String | Log source (always "API") |
| serviceName | String | Service name |

---

## Configuration

### Application Properties

```yaml
app:
  logging:
    enabled: true                    # Enable/disable logging
    log-request-body: true           # Log request bodies
    log-response-body: true          # Log response bodies
    max-body-size: 10000             # Max body size to log (characters)
    sensitive-headers: authorization,cookie,x-api-key  # Headers to redact
```

### Sensitive Header Redaction

The following headers are automatically redacted in logs:
- `Authorization`
- `Cookie`
- `X-Api-Key`

Values are replaced with `***REDACTED***`

---

## Log Files

### File Locations

| File | Purpose | Rotation |
|------|---------|----------|
| `logs/lambrk-backend.log` | General application logs | Daily, 10MB max |
| `logs/api-logs.log` | API request/response logs | Daily, 100MB max |

### Log Retention

- **Max history**: 30 days
- **Max file size**: 10MB (general), 100MB (API)
- **Total size cap**: 1GB (general), 2GB (API)

---

## Database Indexes

The `api_logs` table has the following indexes for efficient queries:

| Index | Purpose |
|-------|---------|
| `idx_api_logs_timestamp` | Sort by time |
| `idx_api_logs_user_id` | Filter by user |
| `idx_api_logs_method` | Filter by HTTP method |
| `idx_api_logs_endpoint` | Filter by endpoint |
| `idx_api_logs_status_code` | Filter by status |
| `idx_api_logs_ip_address` | Filter by IP |
| `idx_api_logs_errors` | Error logs only (partial index) |
| `idx_api_logs_authenticated` | Authenticated requests (partial index) |
| `idx_api_logs_anonymous` | Anonymous requests (partial index) |

---

## Skipped Endpoints

The following endpoints are not logged to reduce noise:

- `/actuator/*` - Health checks and metrics
- `/health` - Health endpoint
- `/favicon.ico` - Browser favicon requests
- `/static/*` - Static resources
- `/swagger-ui/*` - API documentation
- `/v3/api-docs` - OpenAPI spec
- `GET /` - Root path

---

## Privacy & Security

- **IP addresses** are logged for all requests
- **User agents** are logged for analytics
- **Authentication tokens** are never logged (redacted)
- **Passwords** in request bodies should be masked at the application level
- Consider implementing **log retention policies** for GDPR compliance

---

## Performance

- Logging is **asynchronous** using `@Async`
- Uses a **separate transaction** to avoid impacting main request
- File logging uses **async appenders** with 1024 queue size
- Large request/response bodies are **truncated** to prevent memory issues

---

## Correlation IDs

Pass `X-Correlation-Id` header to trace requests across services:

```bash
curl -H "X-Correlation-Id: my-trace-id-123" https://api.example.com/api/posts
```

If not provided, a UUID is automatically generated.
