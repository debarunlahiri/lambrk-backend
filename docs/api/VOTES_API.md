# Votes API

Base path: `/api/votes`. JWT required.

### POST `/api/votes/post`

Vote on a post.

**Auth:** User

**Request body**

```json
{
  "voteType": "LIKE",
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "commentId": null
}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/votes/post' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "voteType": "LIKE",
  "postId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "commentId": null
}'
```

**Response**

`200 OK` with an empty body
### POST `/api/votes/comment`

Vote on a comment.

**Auth:** User

**Request body**

```json
{
  "voteType": "DISLIKE",
  "postId": null,
  "commentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17"
}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/votes/comment' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "voteType": "DISLIKE",
  "postId": null,
  "commentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17"
}'
```

**Response**

`200 OK` with an empty body
