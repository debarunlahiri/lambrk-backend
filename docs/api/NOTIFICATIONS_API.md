# Notifications API

Base path: `/api/notifications`. JWT required.

> **Auto-generated notifications:** Some notifications are created automatically by the system and do not need to be sent manually:
> - `COMMENT_REPLY` — sent when someone replies to your comment via `POST /api/comments/{commentId}/reply`.
> - `COMMENT_MENTION` — sent when someone tags you with `@username` in a comment or reply.
> - `USER_FOLLOW` — sent when another user follows you.
> - `FRIEND_REQUEST` — sent when another user sends you a friend request.
> - `FRIEND_REQUEST_ACCEPTED` — sent when another user accepts your friend request.

## Notification Types

| Type | Created by | Recipient | Related user | Action URL |
|------|------------|-----------|--------------|------------|
| `COMMENT_REPLY` | Reply creation | Parent comment author or post author | Reply author | `/posts/{postId}#comment-{commentId}` |
| `POST_LIKE` | Post like | Post author | Voter | `/posts/{postId}` |
| `COMMENT_LIKE` | Comment like | Comment author | Voter | Comment/post deeplink |
| `POST_MENTION` | Post mention | Mentioned user | Mention author | Post deeplink |
| `COMMENT_MENTION` | Comment mention | Mentioned user | Mention author | Comment deeplink |
| `USER_FOLLOW` | `POST /api/users/{userId}/follow` | Followed user | Follower | `/users/{followerUsername}` |
| `FRIEND_REQUEST` | `POST /api/users/{userId}/friend-request` | Request addressee | Requester | `/users/{requesterUsername}` |
| `FRIEND_REQUEST_ACCEPTED` | `POST /api/users/{userId}/friend-request/accept` | Original requester | User who accepted | `/users/{acceptedByUsername}` |
| `COMMUNITY_INVITE` | Community workflows | Invited user | Inviter | Community deeplink |
| `MODERATOR_ACTION` | Moderation workflows | Affected user | Moderator | Moderation target |
| `SYSTEM_ANNOUNCEMENT` | System workflows | Target user | None | Optional |
| `CONTENT_MODERATION` | Moderation workflows | Affected user | Moderator/system | Moderation target |

---

### POST `/api/notifications`

Create notification.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `type` | Body | string | **Yes** | Notification type. See Notification Types above. |
| `recipientId` | Body | UUID | **Yes** | Target user UUID |
| `title` | Body | string | **Yes** | Notification title |
| `message` | Body | string | **Yes** | Notification body |
| `relatedPostId` | Body | UUID | No | Linked post UUID |
| `relatedCommentId` | Body | UUID | No | Linked comment UUID |
| `relatedUserId` | Body | UUID | No | Linked user UUID |
| `actionUrl` | Body | string | No | Deeplink URL |
| `actionText` | Body | string | No | Button label |
| `isRead` | Body | boolean | No | `false` |

**Request body**

```json
{
  "type": "COMMENT_REPLY",
  "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "title": "New reply",
  "message": "Someone replied",
  "relatedPostId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "relatedCommentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "relatedUserId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "actionUrl": "/posts/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "actionText": "View",
  "isRead": false
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `NotificationResponse` | Created notification |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/notifications' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "COMMENT_REPLY",
  "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "title": "New reply",
  "message": "Someone replied",
  "relatedPostId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "relatedCommentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "relatedUserId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "actionUrl": "/posts/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "actionText": "View",
  "isRead": false
}'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb1c",
  "type": "COMMENT_REPLY",
  "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "title": "New reply",
  "message": "Someone replied",
  "relatedPostId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "relatedPostTitle": null,
  "relatedCommentId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb17",
  "relatedCommentPreview": null,
  "relatedUserId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "relatedUsername": null,
  "actionUrl": "/posts/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  "actionText": "View",
  "isRead": false,
  "createdAt": "2026-05-02T10:00:00Z",
  "readAt": null
}
```

---

### GET `/api/notifications`

Get notifications.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<NotificationResponse>` | Paginated notifications |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications?page=0&size=20' \
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

---

### GET `/api/notifications/unread`

Get unread notifications.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<NotificationResponse>` | Unread notifications |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications/unread?page=0&size=20' \
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

---

### PUT `/api/notifications/{notificationId}/read`

Mark one notification read.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `notificationId` | Path | UUID | **Yes** | Notification UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | empty | Marked as read |
| `401` | error | JWT missing or invalid |
| `404` | error | Notification not found |

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/notifications/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/read' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`200 OK` with an empty body

---

### PUT `/api/notifications/read-all`

Mark all notifications read.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | empty | All marked as read |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/notifications/read-all' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`200 OK` with an empty body

---

### DELETE `/api/notifications/{notificationId}`

Delete notification.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `notificationId` | Path | UUID | **Yes** | Notification UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | Notification deleted |
| `401` | error | JWT missing or invalid |
| `404` | error | Notification not found |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/notifications/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`

---

### DELETE `/api/notifications`

Delete all notifications.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | All notifications deleted |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/notifications' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`

---

### GET `/api/notifications/count/unread`

Get unread count.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | integer | Unread notification count |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications/count/unread' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
0
```

---

### GET `/api/notifications/type/{type}`

Get notifications by type. Current implementation returns an empty page.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `type` | Path | string | **Yes** | — | Notification type |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<NotificationResponse>` | Notifications of the given type |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications/type/COMMENT_REPLY?page=0&size=20' \
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
