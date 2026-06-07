# Bookmarks API

Base path: `/api/bookmarks`

All endpoints **require authentication**. Send a valid JWT in the `Authorization` header for every request.

```http
Authorization: Bearer <accessToken>
```

Auth tokens are obtained from [`POST /api/auth/login`](AUTH_API.md).

---

## Authentication

| Header | Required | Format |
|--------|----------|--------|
| `Authorization` | **Yes** | `Bearer <jwt>` |

No request body is required for any bookmark endpoint. Only path parameters and (for listing) query parameters are used.

---

## Endpoints

### POST `/api/bookmarks/{postId}`

Bookmark a post. Calling this twice for the same post is safe (idempotent).

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `postId` | Path | UUID | **Yes** | UUID of the post to bookmark |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | Bookmark created or already exists |
| `401` | error | JWT missing or invalid |
| `404` | error | Post not found |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/bookmarks/019e9e75-d929-7233-97a0-e16c90026ff3' \
  -H 'Authorization: Bearer <token>'
```

---

### DELETE `/api/bookmarks/{postId}`

Remove a bookmark.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `postId` | Path | UUID | **Yes** | UUID of the post to unbookmark |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | Bookmark removed or did not exist |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/bookmarks/019e9e75-d929-7233-97a0-e16c90026ff3' \
  -H 'Authorization: Bearer <token>'
```

---

### GET `/api/bookmarks`

Get the current user's bookmarked posts. Returns a paginated list of `PostResponse` where every item has `isBookmarked: true`.

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `page` | Query | int | No | `0` | Page number (0-indexed) |
| `size` | Query | int | No | `20` | Items per page |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<PostResponse>` | Paginated list of bookmarked posts |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl 'http://localhost:9500/api/bookmarks?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

---

### GET `/api/bookmarks/{postId}/status`

Check whether a specific post is currently bookmarked by the logged-in user.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `postId` | Path | UUID | **Yes** | UUID of the post to check |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `{ "bookmarked": true\|false }` | Current bookmark status |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl 'http://localhost:9500/api/bookmarks/019e9e75-d929-7233-97a0-e16c90026ff3/status' \
  -H 'Authorization: Bearer <token>'
```

---

### GET `/api/bookmarks/count`

Get the total number of bookmarks saved by the current user.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |

No request body. No path or query parameters.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `{ "count": 42 }` | Total bookmark count |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl 'http://localhost:9500/api/bookmarks/count' \
  -H 'Authorization: Bearer <token>'
```

---

## `isBookmarked` in PostResponse

All post-listing endpoints now include an `isBookmarked` boolean in `PostResponse`. This lets the frontend show a filled/empty bookmark icon without making extra API calls.

```json
{
  "id": "019e9e75-d929-7233-97a0-e16c90026ff3",
  "title": "...",
  "userVote": null,
  "isBookmarked": false,
  "media": [...]
}
```

**Affected endpoints**
- `GET /api/feed`, `/api/feed/hot`, `/api/feed/new`, `/api/feed/top`, `/api/feed/discover`
- `GET /api/posts`, `/api/posts/hot`, `/api/posts/new`, `/api/posts/top`
- `GET /api/posts/media` (LoopMix)
- `GET /api/posts/{postId}`
- `GET /api/posts/community/{communityId}`
- `GET /api/posts/user/{userId}`
- `GET /api/posts/search`
- `GET /api/posts/{postId}/related`
- `GET /api/bookmarks`

---

## Database Schema

**Table:** `bookmarks`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | UUID | PK |
| `user_id` | UUID | NOT NULL, FK → `users(id)` ON DELETE CASCADE |
| `post_id` | UUID | NOT NULL, FK → `posts(id)` ON DELETE CASCADE |
| `created_at` | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT now() |

**Indexes**
- `idx_bookmark_user` — `(user_id)`
- `idx_bookmark_post` — `(post_id)`
- `idx_bookmark_user_post` — UNIQUE `(user_id, post_id)`
- `idx_bookmark_created_at` — `(created_at)`

**Migration:** `V17__Create_bookmarks_table.sql`
