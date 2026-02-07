# Votes API

Base URL: `/api/votes`

All endpoints require **JWT authentication**.

---

## POST `/api/votes/post`

Upvote or downvote a post. Voting is toggle-based:

- **First vote** → creates the vote
- **Same vote again** → removes the vote (toggle off)
- **Opposite vote** → flips the vote direction

### Headers

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "voteType": "UPVOTE",
  "postId": 1,
  "commentId": null
}
```

### Validation Rules

| Field    | Rule                          |
|----------|-------------------------------|
| voteType | Required: `UPVOTE`, `DOWNVOTE`|
| postId   | Required for post votes       |

### Response `200 OK`

Empty body. Side effects:

- Post `score`, `upvoteCount`, `downvoteCount` updated
- Author `karma` adjusted accordingly
- Kafka `vote.cast` event published

### Score Calculation

| Action         | Score Delta | Upvote Delta | Downvote Delta | Karma Delta |
|----------------|-------------|--------------|----------------|-------------|
| New upvote     | +1          | +1           | 0              | +1          |
| New downvote   | -1          | 0            | +1             | -1          |
| Remove upvote  | -1          | -1           | 0              | -1          |
| Remove downvote| +1          | 0            | -1             | +1          |
| Flip to upvote | +2          | +1           | -1             | +2          |
| Flip to down   | -2          | -1           | +1             | -2          |

### Error Responses

| Status | Condition                    |
|--------|------------------------------|
| 400    | Validation failed            |
| 401    | Not authenticated            |
| 404    | Post not found               |
| 429    | Rate limit exceeded (1000/min)|

---

## POST `/api/votes/comment`

Upvote or downvote a comment. Same toggle behaviour as post votes.

### Request Body

```json
{
  "voteType": "UPVOTE",
  "postId": null,
  "commentId": 10
}
```

### Validation Rules

| Field     | Rule                            |
|-----------|---------------------------------|
| voteType  | Required: `UPVOTE`, `DOWNVOTE`  |
| commentId | Required for comment votes      |

### Response `200 OK`

Empty body. Same side effects as post voting but applied to the comment.

### Error Responses

| Status | Condition                    |
|--------|------------------------------|
| 400    | Validation failed            |
| 401    | Not authenticated            |
| 404    | Comment not found            |
| 429    | Rate limit exceeded (1000/min)|

---

## Database Constraints

- A user can only have **one vote per post** (`UNIQUE(user_id, post_id)`)
- A user can only have **one vote per comment** (`UNIQUE(user_id, comment_id)`)
- A vote must target **either** a post or a comment, never both (`CHECK` constraint)

## Caching

Voting evicts `posts`, `comments`, `hotPosts`, `topPosts`, and `commentTrees` caches.
