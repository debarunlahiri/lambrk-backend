# Comments API

Base URL: `/api/comments`

All endpoints require **JWT authentication** unless noted otherwise.

---

## POST `/api/comments`

Create a new comment on a post, or reply to an existing comment.

### Headers

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "content": "Great post! Virtual threads really simplify concurrency.",
  "postId": 1,
  "parentCommentId": null
}
```

To reply to a comment, set `parentCommentId`:

```json
{
  "content": "I agree, especially for I/O-bound workloads.",
  "postId": 1,
  "parentCommentId": 5
}
```

### Validation Rules

| Field            | Rule                              |
|------------------|-----------------------------------|
| content          | Required, max 10 000 chars        |
| postId           | Required, must exist              |
| parentCommentId  | Optional, must exist if provided  |

### Response `200 OK`

```json
{
  "id": 10,
  "content": "Great post! Virtual threads really simplify concurrency.",
  "flairText": null,
  "isEdited": false,
  "isDeleted": false,
  "isRemoved": false,
  "isCollapsed": false,
  "isStickied": false,
  "score": 1,
  "upvoteCount": 1,
  "downvoteCount": 0,
  "replyCount": 0,
  "awardCount": 0,
  "depthLevel": 0,
  "author": {
    "id": 2,
    "username": "john_doe",
    "displayName": "John Doe",
    "karma": 15
  },
  "postId": 1,
  "parentId": null,
  "replies": [],
  "createdAt": "2026-02-07T14:00:00Z",
  "updatedAt": "2026-02-07T14:00:00Z",
  "editedAt": null,
  "userVote": null
}
```

### Error Responses

| Status | Condition                           |
|--------|-------------------------------------|
| 400    | Validation failed                   |
| 401    | Not authenticated                   |
| 403    | Post is locked                      |
| 404    | Post or parent comment not found    |
| 422    | Content moderation violation        |
| 429    | Rate limit exceeded (500/min)       |
| 503    | Circuit breaker open                |

---

## GET `/api/comments/{commentId}`

Get a single comment by ID.

### Path Parameters

| Param     | Type | Description |
|-----------|------|-------------|
| commentId | Long | Comment ID  |

### Response `200 OK`

Same shape as create response. `userVote` populated for authenticated users.

---

## GET `/api/comments/post/{postId}`

Get top-level comments for a post (paginated, sorted by score).

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| postId | Long | Post ID     |

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

Paginated `CommentResponse` list. Only top-level comments (no parent).

---

## GET `/api/comments/{commentId}/replies`

Get direct replies to a specific comment.

### Path Parameters

| Param     | Type | Description |
|-----------|------|-------------|
| commentId | Long | Comment ID  |

### Response `200 OK`

Array of `CommentResponse`.

---

## GET `/api/comments/user/{userId}`

Get all comments by a specific user (paginated, newest first).

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| userId | Long | User ID     |

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## PUT `/api/comments/{commentId}`

Edit a comment. Only the author can edit. Sets `isEdited = true` and records `editedAt`.

### Request Body

Raw string — the new comment content.

### Response `200 OK`

Updated `CommentResponse`.

### Error Responses

| Status | Condition              |
|--------|------------------------|
| 403    | Not the comment author |
| 404    | Comment not found      |

---

## DELETE `/api/comments/{commentId}`

Soft-delete a comment. Content is replaced with `[deleted]` in responses.

### Response `204 No Content`

### Error Responses

| Status | Condition              |
|--------|------------------------|
| 403    | Not the comment author |
| 404    | Comment not found      |

---

## GET `/api/comments/search`

Search comments by content.

### Query Parameters

| Param | Type   | Default | Description       |
|-------|--------|---------|-------------------|
| query | String | —       | Search term (req) |
| page  | int    | 0       | Page number       |
| size  | int    | 20      | Page size         |

---

## Comment Threading Model

Comments form a tree structure via `parentId`:

```
Comment A (depthLevel=0, parentId=null)
├── Comment B (depthLevel=1, parentId=A)
│   └── Comment D (depthLevel=2, parentId=B)
└── Comment C (depthLevel=1, parentId=A)
```

- `depthLevel` is auto-calculated as `parent.depthLevel + 1`
- `replyCount` on parent is incremented/decremented automatically
- Post `commentCount` is updated on create/delete

## Caching Behaviour

| Endpoint               | Cache Name    | TTL   |
|------------------------|---------------|-------|
| GET /{commentId}       | comments      | 3 min |
| GET /post/{postId}     | commentTrees  | 3 min |

Creating, updating, or deleting a comment evicts all comment caches.
