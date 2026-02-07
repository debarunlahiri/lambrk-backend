# Posts API

Base URL: `/api/posts`

All endpoints require **JWT authentication** unless noted otherwise.

---

## POST `/api/posts`

Create a new post.

### Headers

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "title": "My first post about Spring Boot 3.5",
  "content": "Virtual threads are amazing for high-concurrency workloads...",
  "url": null,
  "postType": "TEXT",
  "flairText": "Discussion",
  "flairCssClass": "discussion",
  "isSpoiler": false,
  "isOver18": false,
  "subredditId": 1
}
```

### Validation Rules

| Field       | Rule                                |
|-------------|-------------------------------------|
| title       | Required, max 300 chars             |
| content     | Optional (TEXT type), max 40000     |
| url         | Optional (LINK type), max 2000     |
| postType    | TEXT, LINK, IMAGE, VIDEO, POLL      |
| subredditId | Required, must exist                |

### Response `200 OK`

```json
{
  "id": 1,
  "title": "My first post about Spring Boot 3.5",
  "content": "Virtual threads are amazing...",
  "url": null,
  "postType": "TEXT",
  "thumbnailUrl": null,
  "flairText": "Discussion",
  "flairCssClass": "discussion",
  "isSpoiler": false,
  "isStickied": false,
  "isLocked": false,
  "isArchived": false,
  "isOver18": false,
  "score": 1,
  "upvoteCount": 1,
  "downvoteCount": 0,
  "commentCount": 0,
  "viewCount": 0,
  "awardCount": 0,
  "author": {
    "id": 1,
    "username": "john_doe",
    "displayName": "John Doe",
    "karma": 10
  },
  "subreddit": {
    "id": 1,
    "name": "programming",
    "title": "Programming"
  },
  "createdAt": "2026-02-07T13:30:00Z",
  "updatedAt": "2026-02-07T13:30:00Z",
  "archivedAt": null,
  "userVote": null
}
```

### Error Responses

| Status | Condition                          |
|--------|------------------------------------|
| 400    | Validation failed                  |
| 401    | Not authenticated                  |
| 404    | Subreddit or user not found        |
| 422    | Content moderation violation       |
| 429    | Rate limit exceeded (100/min)      |
| 503    | Circuit breaker open               |

### Resilience

- **Circuit Breaker**: `postService` — opens at 50% failure rate over 10 calls
- **Rate Limiter**: `postCreation` — 100 requests per minute
- **Retry**: 3 attempts with 1s wait on DataAccessException
- **Bulkhead**: max 10 concurrent calls

---

## GET `/api/posts/{postId}`

Get a single post by ID. Increments view count for authenticated users.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| postId | Long | Post ID     |

### Response `200 OK`

Same shape as create response. `userVote` will be `"UPVOTE"`, `"DOWNVOTE"`, or `null`.

---

## GET `/api/posts/hot`

Get hot posts sorted by score (descending).

### Query Parameters

| Param | Type | Default | Description       |
|-------|------|---------|-------------------|
| page  | int  | 0       | Page number       |
| size  | int  | 20      | Page size         |

### Response `200 OK`

Paginated `PostResponse` list.

```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

---

## GET `/api/posts/new`

Get newest posts sorted by creation date (descending).

### Query Parameters

Same as `/hot`.

---

## GET `/api/posts/top`

Get top posts sorted by combined score + comments (descending).

### Query Parameters

Same as `/hot`.

---

## GET `/api/posts/subreddit/{subredditId}`

Get posts for a specific subreddit.

### Path Parameters

| Param       | Type | Description   |
|-------------|------|---------------|
| subredditId | Long | Subreddit ID  |

### Query Parameters

Same as `/hot`.

---

## GET `/api/posts/user/{userId}`

Get posts by a specific user.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| userId | Long | User ID     |

### Query Parameters

Same as `/hot`.

---

## GET `/api/posts/search`

Full-text search across post titles and content.

### Query Parameters

| Param | Type   | Default | Description       |
|-------|--------|---------|-------------------|
| query | String | —       | Search term (req) |
| page  | int    | 0       | Page number       |
| size  | int    | 20      | Page size         |

---

## PUT `/api/posts/{postId}`

Update an existing post. Only the author can edit.

### Request Body

Same shape as create request.

### Error Responses

| Status | Condition                    |
|--------|------------------------------|
| 403    | Not the post author          |
| 404    | Post not found               |

---

## DELETE `/api/posts/{postId}`

Delete a post. Only the author can delete.

### Response `204 No Content`

### Error Responses

| Status | Condition                    |
|--------|------------------------------|
| 403    | Not the post author          |
| 404    | Post not found               |

---

## GET `/api/posts/stickied`

Get stickied (pinned) posts, optionally filtered by subreddit.

### Query Parameters

| Param       | Type | Required | Description          |
|-------------|------|----------|----------------------|
| subredditId | Long | No       | Filter by subreddit  |

### Response `200 OK`

Array of `PostResponse`.

---

## Caching Behaviour

| Endpoint          | Cache Name    | TTL   |
|-------------------|---------------|-------|
| GET /{postId}     | posts         | 5 min |
| GET /hot          | hotPosts      | 5 min |
| GET /new          | newPosts      | 5 min |
| GET /top          | topPosts      | 5 min |
| GET /search       | searchPosts   | 5 min |

Creating, updating, or deleting a post evicts all post caches.
