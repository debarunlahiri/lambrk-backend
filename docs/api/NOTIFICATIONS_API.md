# Notifications API

Base URL: `/api/notifications`

All endpoints require **JWT authentication**.

---

## POST `/api/notifications`

Create a new notification for a user.

### Headers

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "type": "COMMENT_REPLY",
  "recipientId": 1,
  "title": "New reply to your comment",
  "message": "john_doe replied to your comment: \"Great post!\"",
  "relatedPostId": 1,
  "relatedCommentId": 10,
  "relatedUserId": 2,
  "actionUrl": "/posts/1#comment-10",
  "actionText": "View reply",
  "isRead": false
}
```

### Validation Rules

| Field          | Rule                                                    |
|----------------|---------------------------------------------------------|
| type           | Required: `COMMENT_REPLY`, `POST_UPVOTE`, `COMMENT_UPVOTE`, `POST_MENTION`, `COMMENT_MENTION`, `SUBREDDIT_INVITE`, `MODERATOR_ACTION`, `SYSTEM_ANNOUNCEMENT`, `CONTENT_MODERATION` |
| recipientId    | Required, must exist                                    |
| title          | Required, max 500 chars                                |
| message        | Required, max 2000 chars                               |
| relatedPostId  | Optional                                                |
| relatedCommentId| Optional                                                |
| relatedUserId  | Optional                                                |
| actionUrl      | Optional, max 500 chars                                |
| actionText     | Optional, max 100 chars                                |
| isRead         | Boolean, default false                                  |

### Response `200 OK`

```json
{
  "id": 100,
  "type": "COMMENT_REPLY",
  "recipientId": 1,
  "title": "New reply to your comment",
  "message": "john_doe replied to your comment: \"Great post!\"",
  "relatedPostId": 1,
  "relatedPostTitle": "Spring Boot 3.5 Features",
  "relatedCommentId": 10,
  "relatedCommentPreview": "Great post!",
  "relatedUserId": 2,
  "relatedUsername": "john_doe",
  "actionUrl": "/posts/1#comment-10",
  "actionText": "View reply",
  "isRead": false,
  "createdAt": "2026-02-07T14:30:00Z",
  "readAt": null
}
```

---

## GET `/api/notifications`

Get all notifications for the authenticated user (paginated).

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

```json
{
  "content": [
    {
      "id": 100,
      "type": "COMMENT_REPLY",
      "title": "New reply to your comment",
      "message": "john_doe replied to your comment",
      "isRead": false,
      "createdAt": "2026-02-07T14:30:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## GET `/api/notifications/unread`

Get only unread notifications for the authenticated user.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

Same shape as GET `/api/notifications` but only contains unread items.

---

## PUT `/api/notifications/{notificationId}/read`

Mark a specific notification as read.

### Path Parameters

| Param          | Type | Description |
|----------------|------|-------------|
| notificationId | Long | Notification ID |

### Response `200 OK`

Empty response body.

### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| 404    | Notification not found         |
| 403    | Notification belongs to other user |

---

## PUT `/api/notifications/read-all`

Mark all notifications for the authenticated user as read.

### Response `200 OK`

Empty response body.

---

## DELETE `/api/notifications/{notificationId}`

Delete a specific notification.

### Path Parameters

| Param          | Type | Description |
|----------------|------|-------------|
| notificationId | Long | Notification ID |

### Response `204 No Content`

### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| 404    | Notification not found         |
| 403    | Notification belongs to other user |

---

## DELETE `/api/notifications`

Delete all notifications for the authenticated user.

### Response `204 No Content`

---

## GET `/api/notifications/count/unread`

Get the count of unread notifications for the authenticated user.

### Response `200 OK`

```json
5
```

---

## GET `/api/notifications/type/{type}`

Get notifications of a specific type.

### Path Parameters

| Param | Type   | Description |
|-------|--------|-------------|
| type  | String | Notification type |

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

Paginated notifications of the specified type.

---

## Notification Types

| Type              | Description                              | Triggered By                     |
|--------------------|------------------------------------------|----------------------------------|
| COMMENT_REPLY     | Reply to user's comment                 | CommentService                    |
| POST_UPVOTE       | User's post received upvote             | VoteService                       |
| COMMENT_UPVOTE    | User's comment received upvote          | VoteService                       |
| POST_MENTION      | User mentioned in post                  | ContentModerationAspect           |
| COMMENT_MENTION   | User mentioned in comment               | ContentModerationAspect           |
| SUBREDDIT_INVITE  | Invited to moderate subreddit          | SubredditService                  |
| MODERATOR_ACTION  | Moderator action on user's content      | AdminService                      |
| SYSTEM_ANNOUNCEMENT| System-wide announcement              | Admin/Manual                      |
| CONTENT_MODERATION| Content moderation result              | ContentModerationAspect           |

---

## Real-Time Updates

Notifications are also sent via **WebSocket**:

- **Topic**: `/user/{username}/queue/notifications`
- **Event**: Real-time notification delivery
- **Connection**: WebSocket endpoint `/ws`

Example WebSocket message:
```json
{
  "id": 101,
  "type": "POST_UPVOTE",
  "title": "Your post received an upvote",
  "message": "jane_smith upvoted your post",
  "isRead": false,
  "createdAt": "2026-02-07T14:35:00Z"
}
```

---

## Performance

- **Caching**: Notification lists cached for 3 minutes
- **Rate Limiting**: 100 requests per minute
- **Real-time**: WebSocket updates for instant delivery
- **Batching**: Bulk operations for read/delete all

---

## Error Handling

| Status | Condition                      |
|--------|--------------------------------|
| 400    | Invalid notification data      |
| 401    | Not authenticated              |
| 403    | Access to other user's data   |
| 404    | Notification not found         |
| 429    | Rate limit exceeded            |

---

## Metrics

All notification endpoints emit these metrics:

- `notifications.created` - New notifications created
- `notifications.viewed` - Notification lists viewed
- `notifications.unread.viewed` - Unread notifications viewed
- `notifications.read` - Notifications marked as read
- `notifications.read.all` - All notifications marked as read
- `notifications.deleted` - Individual notifications deleted
- `notifications.deleted.all` - All notifications deleted
- `notifications.count.unread` - Unread count requests
