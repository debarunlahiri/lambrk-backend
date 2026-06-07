# Logs API

Base path: `/api/logs`. JWT required with `ADMIN` role. All responses are `Page<LogEntry>`. Pageable params: `page`, `size`, `sort`; default size is 50 sorted by `timestamp,DESC`.

---

### GET `/api/logs`

Get all logs.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | All logs |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/user/{userId}`

Get logs by user.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `userId` | Path | UUID | **Yes** | — | User UUID |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Logs for the user |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/endpoint`

Get logs by endpoint path.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `path` | Query | string | **Yes** | — | Endpoint path pattern |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Logs for the endpoint |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/endpoint?path=/api/posts&page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/method/{method}`

Get logs by method.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `method` | Path | string | **Yes** | — | HTTP method (`GET`, `POST`, etc.) |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Logs for the method |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/method/GET?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/status/{statusCode}`

Get logs by status code.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `statusCode` | Path | integer | **Yes** | — | HTTP status code |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Logs for the status code |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/status/500?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/errors`

Get error logs.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Error logs |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/errors?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/exceptions`

Get exception logs.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Exception logs |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/exceptions?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/anonymous`

Get anonymous logs.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Anonymous request logs |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/anonymous?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/logs/authenticated`

Get authenticated logs.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` (Admin) |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `50` | Page size |
| `sort` | Query | string | No | `timestamp,DESC` | Sort criteria |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<LogEntry>` | Authenticated request logs |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/authenticated?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```
