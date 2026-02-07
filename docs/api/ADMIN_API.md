# Admin API

Base URL: `/api/admin`

All endpoints require **ADMIN role** (`@PreAuthorize("hasRole('ADMIN')")`).

---

## POST `/api/admin/actions`

Perform any administrative action on the platform.

### Headers

```
Authorization: Bearer <admin_access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "action": "DELETE_POST",
  "targetId": 1,
  "reason": "Violation of community guidelines",
  "notes": "Spam content with multiple violations",
  "durationDays": null,
  "permanent": false,
  "notifyUser": true
}
```

### Action Types

| Action            | Target Type | Description                              |
|--------------------|-------------|------------------------------------------|
| BAN_USER          | User        | Permanently or temporarily ban user       |
| SUSPEND_USER       | User        | Temporary suspension                      |
| DELETE_POST       | Post        | Remove post (soft delete)                |
| DELETE_COMMENT     | Comment     | Remove comment (soft delete)             |
| LOCK_POST          | Post        | Prevent new comments on post             |
| LOCK_COMMENT       | Comment     | Prevent replies to comment              |
| REMOVE_MODERATOR   | User        | Remove moderator privileges             |
| ADD_MODERATOR      | User        | Grant moderator privileges              |
| BAN_SUBREDDIT      | Sublambrk   | Ban entire sublambrk                     |
| QUARANTINE_POST    | Post        | Mark post as quarantined (18+)           |
| QUARANTINE_COMMENT | Comment     | Mark comment as quarantined (18+)        |

### Validation Rules

| Field       | Rule                                                    |
|-------------|---------------------------------------------------------|
| action      | Required, valid action type                            |
| targetId    | Required, must exist                                    |
| reason      | Required, max 1000 chars                                |
| notes       | Optional, max 1000 chars                               |
| durationDays| Optional for temporary actions                      |
| permanent   | Boolean, default false                                 |
| notifyUser  | Boolean, default true                                  |

### Response `200 OK`

```json
{
  "actionId": 1001,
  "action": "DELETE_POST",
  "targetId": 1,
  "targetType": "Post",
  "reason": "Violation of community guidelines",
  "notes": "Spam content with multiple violations",
  "performedBy": 1,
  "performedAt": "2026-02-07T15:00:00Z",
  "expiresAt": null,
  "isActive": true,
  "result": "Post deleted successfully"
}
```

---

## POST `/api/admin/ban-user/{userId}`

Ban a user permanently or temporarily.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID to ban |

### Query Parameters

| Param       | Type    | Default | Description |
|-------------|---------|---------|-------------|
| reason      | String  | —       | Ban reason (req) |
| durationDays| Integer | —       | Days for temp ban |
| permanent   | boolean | false   | Permanent ban |
| notifyUser  | boolean | true    | Notify user |

### Response `200 OK`

```json
{
  "actionId": 1002,
  "action": "BAN_USER",
  "targetId": 1,
  "targetType": "User",
  "reason": "Repeated policy violations",
  "performedBy": 1,
  "performedAt": "2026-02-07T15:05:00Z",
  "expiresAt": "2026-03-09T15:05:00Z",
  "isActive": true,
  "result": "User banned successfully"
}
```

---

## POST `/api/admin/suspend-user/{userId}`

Suspend a user temporarily.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID to suspend |

### Query Parameters

| Param       | Type   | Default | Description |
|-------------|--------|---------|-------------|
| reason      | String | —       | Suspension reason (req) |
| durationDays| int    | —       | Suspension duration (req) |
| notifyUser  | boolean| true    | Notify user |

---

## POST `/api/admin/delete-post/{postId}`

Delete a post (soft delete).

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| postId | Long | Post ID to delete |

### Query Parameters

| Param     | Type   | Default | Description |
|-----------|--------|---------|-------------|
| reason    | String | —       | Deletion reason (req) |
| notifyUser| boolean| true    | Notify post author |

---

## POST `/api/admin/delete-comment/{commentId}`

Delete a comment (soft delete).

### Path Parameters

| Param     | Type | Description |
|-----------|------|-------------|
| commentId | Long | Comment ID to delete |

### Query Parameters

| Param     | Type   | Default | Description |
|-----------|--------|---------|-------------|
| reason    | String | —       | Deletion reason (req) |
| notifyUser| boolean| true    | Notify comment author |

---

## POST `/api/admin/lock-post/{postId}`

Lock a post to prevent new comments.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| postId | Long | Post ID to lock |

### Query Parameters

| Param       | Type    | Default | Description |
|-------------|---------|---------|-------------|
| reason      | String  | —       | Lock reason (req) |
| durationDays| Integer | —       | Lock duration |
| permanent   | boolean | false   | Permanent lock |
| notifyUser  | boolean | true    | Notify post author |

---

## POST `/api/admin/quarantine-post/{postId}`

Quarantine a post (mark as 18+ content).

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| postId | Long | Post ID to quarantine |

### Query Parameters

| Param     | Type   | Default | Description |
|-----------|--------|---------|-------------|
| reason    | String | —       | Quarantine reason (req) |
| notifyUser| boolean| true    | Notify post author |

---

## POST `/api/admin/remove-moderator/{userId}`

Remove moderator privileges from a user.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID to remove moderator status |

### Query Parameters

| Param     | Type   | Default | Description |
|-----------|--------|---------|-------------|
| reason    | String | —       | Removal reason (req) |
| notifyUser| boolean| true    | Notify user |

---

## GET `/api/admin/actions`

Get all admin actions (paginated).

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
      "actionId": 1001,
      "action": "DELETE_POST",
      "targetId": 1,
      "targetType": "Post",
      "reason": "Violation of community guidelines",
      "performedBy": 1,
      "performedAt": "2026-02-07T15:00:00Z",
      "isActive": true
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

## GET `/api/admin/actions/user/{userId}`

Get admin actions performed by a specific admin.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | Admin user ID |

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## GET `/api/admin/actions/active`

Get currently active admin actions (not expired).

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## Admin Action Effects

### User Actions

| Action        | Effect on User                                  |
|---------------|------------------------------------------------|
| BAN_USER      | `isActive = false`, cannot login               |
| SUSPEND_USER  | Temporary suspension, can login but limited    |
| REMOVE_MODERATOR| Cleared moderatedSublambrks set              |

### Post Actions

| Action        | Effect on Post                                   |
|---------------|------------------------------------------------|
| DELETE_POST   | `isRemoved = true`, content hidden              |
| LOCK_POST     | `isLocked = true`, new comments prevented     |
| QUARANTINE_POST| `isOver18 = true`, age-restricted content      |

### Comment Actions

| Action            | Effect on Comment                                |
|-------------------|--------------------------------------------------|
| DELETE_COMMENT     | `isRemoved = true`, content hidden                |
| LOCK_COMMENT       | `isLocked = true`, replies prevented            |
| QUARANTINE_COMMENT | `isOver18 = true`, age-restricted content       |

---

## Audit Trail

All admin actions are automatically:

1. **Logged** to the `admin_actions` table
2. **Audited** via Kafka topic `admin-actions`
3. **Notified** to affected users (if enabled)
4. **Tracked** with metrics for monitoring

### Kafka Event Schema

```json
{
  "actionId": 1001,
  "action": "DELETE_POST",
  "targetId": 1,
  "targetType": "Post",
  "reason": "Violation of community guidelines",
  "performedBy": 1,
  "performedAt": "2026-02-07T15:00:00Z",
  "expiresAt": null,
  "isActive": true,
  "result": "Post deleted successfully"
}
```

---

## Performance & Limits

- **Rate Limiting**: 100 admin actions per minute
- **Caching**: Action lists cached for 5 minutes
- **Bulk Operations**: Batch processing for multiple actions
- **Audit Retention**: Actions retained for 1 year

---

## Error Handling

| Status | Condition                      |
|--------|--------------------------------|
| 400    | Invalid action parameters      |
| 401    | Not authenticated              |
| 403    | Not an admin                   |
| 404    | Target not found              |
| 429    | Rate limit exceeded            |
| 503    | Admin service unavailable     |

---

## Metrics

All admin endpoints emit these metrics:

- `admin.actions.performed` - Actions performed by type
- `admin.ban.user` - User bans
- `admin.delete.post` - Post deletions
- `admin.delete.comment` - Comment deletions
- `admin.lock.post` - Post locks
- `admin.quarantine.post` - Post quarantines
- `admin.remove.moderator` - Moderator removals
