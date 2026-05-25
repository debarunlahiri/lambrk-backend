# Votes API

Base path: `/api/votes`. JWT required.

### POST `/api/votes/post`

Vote on a post.

**Auth:** User

**Request body**

```json
{"voteType":"LIKE","postId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11","commentId":null}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/votes/post' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "voteType": "LIKE",
  "postId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
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
{"voteType":"DISLIKE","postId":null,"commentId":"b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/votes/comment' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "voteType": "DISLIKE",
  "postId": null,
  "commentId": "b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
}'
```

**Response**

`200 OK` with an empty body
