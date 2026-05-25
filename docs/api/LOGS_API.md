# Logs API

Base path: `/api/logs`. JWT required with `ADMIN` role. All responses are `Page<LogEntry>`. Pageable params: `page`, `size`, `sort`; default size is 50 sorted by `timestamp,DESC`.

### GET `/api/logs`

Get all logs.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/user/{userId}`

Get logs by user.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/endpoint`

Get logs by endpoint path.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/endpoint?path=/api/posts&page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/method/{method}`

Get logs by method.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/method/GET?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/status/{statusCode}`

Get logs by status code.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/status/500?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/errors`

Get error logs.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/errors?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/exceptions`

Get exception logs.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/exceptions?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/anonymous`

Get anonymous logs.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/anonymous?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/logs/authenticated`

Get authenticated logs.

**Auth:** Admin

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/logs/authenticated?page=0&size=50' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
