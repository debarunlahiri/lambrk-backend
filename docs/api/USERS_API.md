# Users API

Base path: `/api/users`. JWT required unless noted.

### GET `/api/users/{userId}`

Get a user by id.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":"Builder","avatarUrl":"https://example.com/avatar.png","isActive":true,"isVerified":false,"karma":42,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"}
```
### GET `/api/users/username/{username}`

Get a user by username.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/username/johndoe' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":"Builder","avatarUrl":"https://example.com/avatar.png","isActive":true,"isVerified":false,"karma":42,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"}
```
### GET `/api/users/me`

Get the authenticated user.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/me' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":"Builder","avatarUrl":"https://example.com/avatar.png","isActive":true,"isVerified":false,"karma":42,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"}
```
### GET `/api/users/top`

List top users by karma.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/top?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/users/search`

Search active users.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |
| `query` | string | yes | - | Search text. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/search?query=john&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### DELETE `/api/users/{userId}`

Delete a user.

**Auth:** Admin

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/users/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`
