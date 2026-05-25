# Posts API

Base path: `/api/posts`. JWT required.

### POST `/api/posts`

Create a post.

**Auth:** User

**Request body**

```json
{"title":"My first post","content":"Post body","url":null,"postType":"TEXT","flairText":null,"flairCssClass":null,"isSpoiler":false,"isOver18":false,"communityId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/posts' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "title": "My first post",
  "content": "Post body",
  "url": null,
  "postType": "TEXT",
  "flairText": null,
  "flairCssClass": null,
  "isSpoiler": false,
  "isOver18": false,
  "communityId": 1
}'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post","content":"Post body","url":null,"postType":"TEXT","thumbnailUrl":null,"flairText":null,"flairCssClass":null,"isSpoiler":false,"isStickied":false,"isLocked":false,"isArchived":false,"isOver18":false,"score":1,"likeCount":1,"dislikeCount":0,"commentCount":0,"viewCount":0,"awardCount":0,"author":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"community":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software","sidebarText":null,"headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"categories":[],"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","archivedAt":null,"userVote":null}
```
### GET `/api/posts/{postId}`

Get one post.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post","content":"Post body","url":null,"postType":"TEXT","thumbnailUrl":null,"flairText":null,"flairCssClass":null,"isSpoiler":false,"isStickied":false,"isLocked":false,"isArchived":false,"isOver18":false,"score":1,"likeCount":1,"dislikeCount":0,"commentCount":0,"viewCount":0,"awardCount":0,"author":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"community":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software","sidebarText":null,"headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"categories":[],"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","archivedAt":null,"userVote":null}
```
### GET `/api/posts/hot`

Get hot posts.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/hot?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post"}],"totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true,"numberOfElements":1,"empty":false}
```
### GET `/api/posts/new`

Get newest posts.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/new?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post"}],"totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true,"numberOfElements":1,"empty":false}
```
### GET `/api/posts/top`

Get top posts.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/top?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post"}],"totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true,"numberOfElements":1,"empty":false}
```
### GET `/api/posts/community/{communityId}`

Get posts in a community.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/community/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post"}],"totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true,"numberOfElements":1,"empty":false}
```
### GET `/api/posts/user/{userId}`

Get posts by user.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post"}],"totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true,"numberOfElements":1,"empty":false}
```
### GET `/api/posts/search`

Search posts.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |
| `query` | string | yes | - | Search text. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/search?query=spring&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post"}],"totalElements":1,"totalPages":1,"size":20,"number":0,"first":true,"last":true,"numberOfElements":1,"empty":false}
```
### PUT `/api/posts/{postId}`

Update a post.

**Auth:** User

**Request body**

```json
{"title":"Updated title","content":"Updated body","url":null,"postType":"TEXT","communityId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}
```

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/posts/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "title": "Updated title",
  "content": "Updated body",
  "url": null,
  "postType": "TEXT",
  "communityId": 1
}'
```

**Response**

```json
{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","title":"My first post","content":"Post body","url":null,"postType":"TEXT","thumbnailUrl":null,"flairText":null,"flairCssClass":null,"isSpoiler":false,"isStickied":false,"isLocked":false,"isArchived":false,"isOver18":false,"score":1,"likeCount":1,"dislikeCount":0,"commentCount":0,"viewCount":0,"awardCount":0,"author":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"community":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","name":"programming","title":"Programming","description":"Software","sidebarText":null,"headerImageUrl":null,"iconImageUrl":null,"isPublic":true,"isRestricted":false,"isOver18":false,"memberCount":1,"subscriberCount":1,"activeUserCount":0,"createdBy":{"id":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"categories":[],"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","isUserSubscribed":false,"isUserModerator":false},"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","archivedAt":null,"userVote":null}
```
### DELETE `/api/posts/{postId}`

Delete a post.

**Auth:** User

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/posts/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`
### GET `/api/posts/stickied`

List stickied posts.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `communityId` | UUID | no | - | Filter by community. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/stickied?communityId=b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
