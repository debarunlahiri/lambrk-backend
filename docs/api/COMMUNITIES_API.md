# Communities API

Base path: `/api/communities`. JWT required. Update requires `MODERATOR` or `ADMIN`.

### POST `/api/communities`

Create a community.

**Auth:** User

**Request body**

```json
{"name":"programming","title":"Programming","description":"Software development","sidebarText":"Rules","isPublic":true,"isRestricted":false,"isOver18":false,"categoryIds":["f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"]}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/communities' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "programming",
  "title": "Programming",
  "description": "Software development",
  "sidebarText": "Rules",
  "isPublic": true,
  "isRestricted": false,
  "isOver18": false,
  "categoryIds": ["f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"]
}'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software development","sidebarText":"Rules","headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false,"categories":[{"id":"f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"Technology","description":"All things tech, software, and hardware","iconUrl":null,"imageUrl":null,"color":"#2563EB","slug":"technology","sortOrder":1,"communityCount":0,"createdAt":"2026-05-23T10:00:00Z","updatedAt":"2026-05-23T10:00:00Z"}]}
```
### GET `/api/communities`

List public communities.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/communities?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/communities/{communityId}`

Get community by id.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/communities/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software development","sidebarText":"Rules","headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false,"categories":[{"id":"f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"Technology","description":"All things tech, software, and hardware","iconUrl":null,"imageUrl":null,"color":"#2563EB","slug":"technology","sortOrder":1,"communityCount":0,"createdAt":"2026-05-23T10:00:00Z","updatedAt":"2026-05-23T10:00:00Z"}]}
```
### GET `/api/communities/r/{name}`

Get community by name.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/communities/r/programming' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software development","sidebarText":"Rules","headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false,"categories":[{"id":"f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"Technology","description":"All things tech, software, and hardware","iconUrl":null,"imageUrl":null,"color":"#2563EB","slug":"technology","sortOrder":1,"communityCount":0,"createdAt":"2026-05-23T10:00:00Z","updatedAt":"2026-05-23T10:00:00Z"}]}
```
### GET `/api/communities/trending`

List trending communities.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/communities/trending?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/communities/search`

Search communities.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |
| `query` | string | yes | - | Search text. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/communities/search?query=prog&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### PUT `/api/communities/{communityId}`

Update community.

**Auth:** Moderator/Admin

**Request body**

```json
{"name":"programming","title":"Programming","description":"Updated","sidebarText":"Rules","isPublic":true,"isRestricted":false,"isOver18":false}
```

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/communities/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "programming",
  "title": "Programming",
  "description": "Updated",
  "sidebarText": "Rules",
  "isPublic": true,
  "isRestricted": false,
  "isOver18": false,
  "categoryIds": ["f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"]
}'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software development","sidebarText":"Rules","headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false,"categories":[{"id":"f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"Technology","description":"All things tech, software, and hardware","iconUrl":null,"imageUrl":null,"color":"#2563EB","slug":"technology","sortOrder":1,"communityCount":0,"createdAt":"2026-05-23T10:00:00Z","updatedAt":"2026-05-23T10:00:00Z"}]}
```
### POST `/api/communities/{communityId}/subscribe`

Subscribe current user.

**Auth:** User

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/communities/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/subscribe' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`200 OK` with an empty body
### POST `/api/communities/{communityId}/unsubscribe`

Unsubscribe current user.

**Auth:** User

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/communities/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/unsubscribe' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`200 OK` with an empty body
### GET `/api/communities/user/subscriptions`

Get current user subscriptions.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/communities/user/subscriptions' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
