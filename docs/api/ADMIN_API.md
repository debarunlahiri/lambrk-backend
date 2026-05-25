# Admin API

Base path: `/api/admin`. JWT required with `ADMIN` role.

### POST `/api/admin/actions`

Perform generic admin action.

**Auth:** Admin

**Request body**

```json
{"action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","reason":"Spam","notes":"Repeated reports","durationDays":7,"permanent":false,"notifyUser":true}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/actions' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "action": "BAN_USER",
  "targetId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "reason": "Spam",
  "notes": "Repeated reports",
  "durationDays": 7,
  "permanent": false,
  "notifyUser": true
}'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### GET `/api/admin/actions`

List admin actions.

**Auth:** Admin

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/admin/actions?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/admin/actions/user/{userId}`

List actions by user.

**Auth:** Admin

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/admin/actions/user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/admin/actions/active`

List active actions.

**Auth:** Admin

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/admin/actions/active?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### POST `/api/admin/ban-user/{userId}`

Perform ban user action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Spam&durationDays=7&permanent=false&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/ban-user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Spam&durationDays=7&permanent=false&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### POST `/api/admin/suspend-user/{userId}`

Perform suspend user action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Abuse&durationDays=3&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/suspend-user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Abuse&durationDays=3&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### POST `/api/admin/delete-post/{postId}`

Perform delete post action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Rule%20violation&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/delete-post/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Rule%20violation&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### POST `/api/admin/delete-comment/{commentId}`

Perform delete comment action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Rule%20violation&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/delete-comment/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Rule%20violation&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### POST `/api/admin/lock-post/{postId}`

Perform lock post action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Heated&durationDays=1&permanent=false&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/lock-post/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Heated&durationDays=1&permanent=false&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### POST `/api/admin/quarantine-post/{postId}`

Perform quarantine post action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Review&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/quarantine-post/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Review&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
### POST `/api/admin/remove-moderator/{userId}`

Perform remove moderator action.

**Auth:** Admin

**Query/path parameters**

Query string: `reason=Policy&notifyUser=true`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/admin/remove-moderator/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?reason=Policy&notifyUser=true' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"actionId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","action":"BAN_USER","targetId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","targetType":"USER","reason":"Spam","notes":null,"performedBy":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","performedAt":"2026-05-02T10:00:00Z","expiresAt":null,"isActive":true,"result":"Action completed"}
```
