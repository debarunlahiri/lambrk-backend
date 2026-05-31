# Comments API

Base path: `/api/comments`. JWT required.

### POST `/api/comments`

Create a top-level comment on a post.

**Auth:** User

**Request body**

```json
{
  "content": "Great post @johndoe!",
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "parentCommentId": null
}
```

> **@mention tagging:** Include `@username` in the content. Every tagged user (who is not the author) receives a `COMMENT_MENTION` notification.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/comments' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "content": "Great post",
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "parentCommentId": null
}'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "content": "Great post",
  "flairText": null,
  "isEdited": false,
  "isDeleted": false,
  "isRemoved": false,
  "isCollapsed": false,
  "isStickied": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "replyCount": 0,
  "awardCount": 0,
  "depthLevel": 0,
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
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "parentId": null,
  "replies": [],
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "editedAt": null,
  "userVote": null
}
```
### POST `/api/comments/{commentId}/reply`

Reply to an existing comment. This sends a `COMMENT_REPLY` notification to the parent comment author (unless replying to yourself).

**Auth:** User

**Request body**

Raw text or JSON string with the reply content. Use `@username` to tag users.

```text
Thanks for the insight @johndoe!
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/comments/019e5a43-e0c2-7baa-9f6d-b9b9b82afb17/reply' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: text/plain' \
  -d 'Thanks for the insight!'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb18",
  "content": "Thanks for the insight!",
  "flairText": null,
  "isEdited": false,
  "isDeleted": false,
  "isRemoved": false,
  "isCollapsed": false,
  "isStickied": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "replyCount": 0,
  "awardCount": 0,
  "depthLevel": 1,
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
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "parentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "replies": [],
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "editedAt": null,
  "userVote": null
}
```
### GET `/api/comments/{commentId}`

Get one comment.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "content": "Great post",
  "flairText": null,
  "isEdited": false,
  "isDeleted": false,
  "isRemoved": false,
  "isCollapsed": false,
  "isStickied": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "replyCount": 0,
  "awardCount": 0,
  "depthLevel": 0,
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
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "parentId": null,
  "replies": [],
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "editedAt": null,
  "userVote": null
}
```
### GET `/api/comments/post/{postId}`

Get top-level comments for a post, sorted by score descending. Each comment includes up to 3 recent replies in the `replies` array.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/post/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
      "content": "Great post",
      "flairText": null,
      "isEdited": false,
      "isDeleted": false,
      "isRemoved": false,
      "isCollapsed": false,
      "isStickied": false,
      "score": 5,
      "likeCount": 5,
      "dislikeCount": 0,
      "replyCount": 2,
      "awardCount": 0,
      "depthLevel": 0,
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
      "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
      "parentId": null,
      "replies": [
        {
          "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb18",
          "content": "Thanks!",
          "flairText": null,
          "isEdited": false,
          "isDeleted": false,
          "isRemoved": false,
          "isCollapsed": false,
          "isStickied": false,
          "score": 2,
          "likeCount": 2,
          "dislikeCount": 0,
          "replyCount": 0,
          "awardCount": 0,
          "depthLevel": 1,
          "author": {
            "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
            "username": "janedoe",
            "displayName": "Jane Doe",
            "bio": null,
            "avatarUrl": null,
            "isActive": true,
            "isVerified": false,
            "karma": 0,
            "createdAt": "2026-05-02T10:00:00Z",
            "updatedAt": "2026-05-02T10:00:00Z"
          },
          "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
          "parentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
          "replies": [],
          "createdAt": "2026-05-02T10:00:00Z",
          "updatedAt": "2026-05-02T10:00:00Z",
          "editedAt": null,
          "userVote": null
        }
      ],
      "createdAt": "2026-05-02T10:00:00Z",
      "updatedAt": "2026-05-02T10:00:00Z",
      "editedAt": null,
      "userVote": null
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
### GET `/api/comments/{commentId}/replies`

Get replies for a comment.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/replies' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
### GET `/api/comments/user/{userId}`

Get comments by user.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/user/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?page=0&size=20' \
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
### PUT `/api/comments/{commentId}`

Update comment content. Body is raw text.

**Auth:** User

**Request body**

```text
Updated comment text
```

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/comments/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: text/plain' \
  -H 'Content-Type: application/json' \
  -d 'Updated comment text'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "content": "Great post",
  "flairText": null,
  "isEdited": false,
  "isDeleted": false,
  "isRemoved": false,
  "isCollapsed": false,
  "isStickied": false,
  "score": 0,
  "likeCount": 0,
  "dislikeCount": 0,
  "replyCount": 0,
  "awardCount": 0,
  "depthLevel": 0,
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
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "parentId": null,
  "replies": [],
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z",
  "editedAt": null,
  "userVote": null
}
```
### DELETE `/api/comments/{commentId}`

Delete comment.

**Auth:** User

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/comments/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`
### GET `/api/comments/search`

Search comments.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |
| `query` | string | yes | - | Search text. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/search?query=great&page=0&size=20' \
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
