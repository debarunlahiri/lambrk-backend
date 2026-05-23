# Comments API

Base path: `/api/comments`. JWT required.

### POST `/api/comments`

Create a comment or reply.

**Auth:** User

**Request body**

```json
{"content":"Great post","postId":1,"parentCommentId":null}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/comments' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "content": "Great post",
  "postId": 1,
  "parentCommentId": null
}'
```

**Response**

```json
{"id":1,"content":"Great post","flairText":null,"isEdited":false,"isDeleted":false,"isRemoved":false,"isCollapsed":false,"isStickied":false,"score":1,"likeCount":1,"dislikeCount":0,"replyCount":0,"awardCount":0,"depthLevel":0,"author":{"id":1,"username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"postId":1,"parentId":null,"replies":[],"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","editedAt":null,"userVote":null}
```
### GET `/api/comments/{commentId}`

Get one comment.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/1' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"id":1,"content":"Great post","flairText":null,"isEdited":false,"isDeleted":false,"isRemoved":false,"isCollapsed":false,"isStickied":false,"score":1,"likeCount":1,"dislikeCount":0,"replyCount":0,"awardCount":0,"depthLevel":0,"author":{"id":1,"username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"postId":1,"parentId":null,"replies":[],"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","editedAt":null,"userVote":null}
```
### GET `/api/comments/post/{postId}`

Get comments for a post.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/post/1?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/comments/{commentId}/replies`

Get replies for a comment.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/comments/1/replies' \
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
curl -X GET 'http://localhost:9500/api/comments/user/1?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
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
curl -X PUT 'http://localhost:9500/api/comments/1' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: text/plain' \
  -H 'Content-Type: application/json' \
  -d 'Updated comment text'
```

**Response**

```json
{"id":1,"content":"Great post","flairText":null,"isEdited":false,"isDeleted":false,"isRemoved":false,"isCollapsed":false,"isStickied":false,"score":1,"likeCount":1,"dislikeCount":0,"replyCount":0,"awardCount":0,"depthLevel":0,"author":{"id":1,"username":"johndoe","displayName":"John Doe","bio":null,"avatarUrl":null,"isActive":true,"isVerified":false,"karma":1,"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z"},"postId":1,"parentId":null,"replies":[],"createdAt":"2026-05-02T10:00:00Z","updatedAt":"2026-05-02T10:00:00Z","editedAt":null,"userVote":null}
```
### DELETE `/api/comments/{commentId}`

Delete comment.

**Auth:** User

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/comments/1' \
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
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
