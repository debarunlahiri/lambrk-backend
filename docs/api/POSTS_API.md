# Posts API

Base path: `/api/posts`. JWT required.

---

### POST `/api/posts`

Create a post.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Description                      |
| --------------- | -------- | ------- | -------- | -------------------------------- |
| `Authorization` | Header   | string  | **Yes**  | `Bearer <jwt>`                   |
| `title`         | Body     | string  | **Yes**  | Post title                       |
| `content`       | Body     | string  | No       | Post body text                   |
| `url`           | Body     | string  | No       | External URL (for LINK posts)    |
| `postType`      | Body     | string  | **Yes**  | `TEXT`, `LINK`, `IMAGE`, `VIDEO` |
| `flairText`     | Body     | string  | No       | Flair label                      |
| `flairCssClass` | Body     | string  | No       | Flair CSS class                  |
| `isSpoiler`     | Body     | boolean | No       | `false`                          |
| `isOver18`      | Body     | boolean | No       | `false`                          |
| `communityId`   | Body     | UUID    | **Yes**  | Target community UUID            |
| `mediaIds`      | Body     | array   | No       | Uploaded file UUIDs              |

**Request body**

```json
{
  "title": "My first post",
  "content": "Post body",
  "url": null,
  "postType": "TEXT",
  "flairText": null,
  "flairCssClass": null,
  "isSpoiler": false,
  "isOver18": false,
  "communityId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "mediaIds": []
}
```

**Response**

| Status | Body           | Description            |
| ------ | -------------- | ---------------------- |
| `200`  | `PostResponse` | Created post           |
| `401`  | error          | JWT missing or invalid |
| `404`  | error          | Community not found    |

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
  "communityId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "mediaIds": []
}'
```

**Create a post with images**

Upload files first via `POST /api/files/upload`, then include the returned `fileId`s in `mediaIds`:

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/posts' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "title": "My photo post",
  "content": "Look at these",
  "postType": "IMAGE",
  "mediaIds": ["019e7a12-3456-7baa-9f6d-b9b9b82afb14", "019e8b23-4567-7baa-9f6d-b9b9b82afb14"]
}'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "title": "My first post",
  "content": "Post body",
  "url": null,
  "postType": "TEXT",
  "thumbnailUrl": null,
  "flairText": null,
  "flairCssClass": null,
  "isSpoiler": false,
  "isStickied": false,
  "isLocked": false,
  "isArchived": false,
  "isOver18": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "commentCount": 0,
  "viewCount": 0,
  "awardCount": 0,
  "media": [],
  "author": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "username": "johndoe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z"
  },
  "community": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
    "name": "programming",
    "title": "Programming",
    "description": "Software",
    "sidebarText": null,
    "headerImageUrl": null,
    "iconImageUrl": null,
    "isPublic": true,
    "isRestricted": false,
    "isOver18": false,
    "memberCount": 1,
    "subscriberCount": 1,
    "activeUserCount": 0,
    "createdBy": {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
      "username": "johndoe",
      "displayName": "John Doe",
      "bio": null,
      "avatarUrl": null,
      "isActive": true,
      "isVerified": false,
      "karma": 0,
      "createdAt": "2026-05-02T10:00:00Z",
      "updatedAt": "2026-05-02T10:00:00Z"
    },
    "categories": [
      {
        "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb18",
        "name": "Technology",
        "description": "All things tech, software, and hardware",
        "iconUrl": null,
        "imageUrl": null,
        "color": "#2563EB",
        "slug": "technology",
        "sortOrder": 1,
        "communityCount": 0,
        "createdAt": "2026-05-23T10:00:00Z",
        "updatedAt": "2026-05-23T10:00:00Z"
      }
    ],
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z",
    "isUserSubscribed": false,
    "isUserModerator": false
  },
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "archivedAt": null,
  "userVote": null,
  "isBookmarked": false
}
```

---

### GET `/api/posts/{postId}`

Get one post.

**Auth:** User

**What to send**

| Parameter       | Location | Type   | Required | Description      |
| --------------- | -------- | ------ | -------- | ---------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>`   |
| `postId`        | Path     | UUID   | **Yes**  | UUID of the post |

No request body.

**Response**

| Status | Body           | Description            |
| ------ | -------------- | ---------------------- |
| `200`  | `PostResponse` | Post details           |
| `401`  | error          | JWT missing or invalid |
| `404`  | error          | Post not found         |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "title": "My first post",
  "content": "Post body",
  "url": null,
  "postType": "TEXT",
  "thumbnailUrl": null,
  "flairText": null,
  "flairCssClass": null,
  "isSpoiler": false,
  "isStickied": false,
  "isLocked": false,
  "isArchived": false,
  "isOver18": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "commentCount": 0,
  "viewCount": 0,
  "awardCount": 0,
  "media": [],
  "author": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "username": "johndoe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z"
  },
  "community": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
    "name": "programming",
    "title": "Programming",
    "description": "Software",
    "sidebarText": null,
    "headerImageUrl": null,
    "iconImageUrl": null,
    "isPublic": true,
    "isRestricted": false,
    "isOver18": false,
    "memberCount": 1,
    "subscriberCount": 1,
    "activeUserCount": 0,
    "createdBy": {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
      "username": "johndoe",
      "displayName": "John Doe",
      "bio": null,
      "avatarUrl": null,
      "isActive": true,
      "isVerified": false,
      "karma": 0,
      "createdAt": "2026-05-02T10:00:00Z",
      "updatedAt": "2026-05-02T10:00:00Z"
    },
    "categories": [
      {
        "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb18",
        "name": "Technology",
        "description": "All things tech, software, and hardware",
        "iconUrl": null,
        "imageUrl": null,
        "color": "#2563EB",
        "slug": "technology",
        "sortOrder": 1,
        "communityCount": 0,
        "createdAt": "2026-05-23T10:00:00Z",
        "updatedAt": "2026-05-23T10:00:00Z"
      }
    ],
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z",
    "isUserSubscribed": false,
    "isUserModerator": false
  },
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "archivedAt": null,
  "userVote": null,
  "isBookmarked": false
}
```

---

### GET `/api/posts/hot`

Get hot posts.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                 | Description            |
| ------ | -------------------- | ---------------------- |
| `200`  | `Page<PostResponse>` | Paginated hot posts    |
| `401`  | error                | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/hot?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "title": "My first post"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### GET `/api/posts/new`

Get newest posts.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                 | Description            |
| ------ | -------------------- | ---------------------- |
| `200`  | `Page<PostResponse>` | Paginated newest posts |
| `401`  | error                | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/new?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "title": "My first post"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### GET `/api/posts/top`

Get top posts.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                 | Description            |
| ------ | -------------------- | ---------------------- |
| `200`  | `Page<PostResponse>` | Paginated top posts    |
| `401`  | error                | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/top?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "title": "My first post"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### GET `/api/posts/community/{communityId}`

Get posts in a community.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `communityId`   | Path     | UUID    | **Yes**  | —       | Community UUID        |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                 | Description               |
| ------ | -------------------- | ------------------------- |
| `200`  | `Page<PostResponse>` | Paginated community posts |
| `401`  | error                | JWT missing or invalid    |
| `404`  | error                | Community not found       |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/community/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "title": "My first post"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### GET `/api/posts/user/{userId}`

Get posts by user.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `userId`        | Path     | UUID    | **Yes**  | —       | Author UUID           |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                 | Description            |
| ------ | -------------------- | ---------------------- |
| `200`  | `Page<PostResponse>` | Paginated user posts   |
| `401`  | error                | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "title": "My first post"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### GET `/api/posts/search`

Search posts.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `query`         | Query    | string  | **Yes**  | —       | Search text           |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                 | Description              |
| ------ | -------------------- | ------------------------ |
| `200`  | `Page<PostResponse>` | Paginated search results |
| `401`  | error                | JWT missing or invalid   |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/search?query=spring&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "title": "My first post"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### PUT `/api/posts/{postId}`

Update a post.

**Auth:** User

**What to send**

| Parameter       | Location | Type   | Required | Description      |
| --------------- | -------- | ------ | -------- | ---------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>`   |
| `postId`        | Path     | UUID   | **Yes**  | UUID of the post |
| `title`         | Body     | string | No       | Updated title    |
| `content`       | Body     | string | No       | Updated body     |
| `url`           | Body     | string | No       | Updated URL      |
| `postType`      | Body     | string | No       | Post type        |
| `communityId`   | Body     | UUID   | No       | Target community |

**Request body**

```json
{
  "title": "Updated title",
  "content": "Updated body",
  "url": null,
  "postType": "TEXT",
  "communityId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15"
}
```

**Response**

| Status | Body           | Description                 |
| ------ | -------------- | --------------------------- |
| `200`  | `PostResponse` | Updated post                |
| `401`  | error          | JWT missing or invalid      |
| `403`  | error          | Not the author              |
| `404`  | error          | Post or community not found |

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
  "communityId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15"
}'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "title": "My first post",
  "content": "Post body",
  "url": null,
  "postType": "TEXT",
  "thumbnailUrl": null,
  "flairText": null,
  "flairCssClass": null,
  "isSpoiler": false,
  "isStickied": false,
  "isLocked": false,
  "isArchived": false,
  "isOver18": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "commentCount": 0,
  "viewCount": 0,
  "awardCount": 0,
  "media": [],
  "author": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "username": "johndoe",
    "displayName": "John Doe",
    "bio": null,
    "avatarUrl": null,
    "isActive": true,
    "isVerified": false,
    "karma": 0,
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z"
  },
  "community": {
    "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
    "name": "programming",
    "title": "Programming",
    "description": "Software",
    "sidebarText": null,
    "headerImageUrl": null,
    "iconImageUrl": null,
    "isPublic": true,
    "isRestricted": false,
    "isOver18": false,
    "memberCount": 1,
    "subscriberCount": 1,
    "activeUserCount": 0,
    "createdBy": {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
      "username": "johndoe",
      "displayName": "John Doe",
      "bio": null,
      "avatarUrl": null,
      "isActive": true,
      "isVerified": false,
      "karma": 0,
      "createdAt": "2026-05-02T10:00:00Z",
      "updatedAt": "2026-05-02T10:00:00Z"
    },
    "categories": [
      {
        "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb18",
        "name": "Technology",
        "description": "All things tech, software, and hardware",
        "iconUrl": null,
        "imageUrl": null,
        "color": "#2563EB",
        "slug": "technology",
        "sortOrder": 1,
        "communityCount": 0,
        "createdAt": "2026-05-23T10:00:00Z",
        "updatedAt": "2026-05-23T10:00:00Z"
      }
    ],
    "createdAt": "2026-05-02T10:00:00Z",
    "updatedAt": "2026-05-02T10:00:00Z",
    "isUserSubscribed": false,
    "isUserModerator": false
  },
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "archivedAt": null,
  "userVote": null,
  "isBookmarked": false
}
```

---

### DELETE `/api/posts/{postId}`

Delete a post.

**Auth:** User

**What to send**

| Parameter       | Location | Type   | Required | Description      |
| --------------- | -------- | ------ | -------- | ---------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>`   |
| `postId`        | Path     | UUID   | **Yes**  | UUID of the post |

No request body.

**Response**

| Status | Body  | Description            |
| ------ | ----- | ---------------------- |
| `204`  | empty | Post deleted           |
| `401`  | error | JWT missing or invalid |
| `403`  | error | Not the author         |
| `404`  | error | Post not found         |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/posts/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`

---

### GET `/api/posts/stickied`

List stickied posts.

**Auth:** User

**What to send**

| Parameter       | Location | Type   | Required | Default | Description         |
| --------------- | -------- | ------ | -------- | ------- | ------------------- |
| `Authorization` | Header   | string | **Yes**  | —       | `Bearer <jwt>`      |
| `communityId`   | Query    | UUID   | No       | —       | Filter by community |

No request body.

**Response**

| Status | Body                 | Description            |
| ------ | -------------------- | ---------------------- |
| `200`  | `List<PostResponse>` | Stickied posts         |
| `401`  | error                | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/posts/stickied?communityId=b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
