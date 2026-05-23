# Notifications API

Base path: `/api/notifications`. JWT required.

### POST `/api/notifications`

Create notification.

**Auth:** User

**Request body**

```json
{"type":"COMMENT_REPLY","recipientId":1,"title":"New reply","message":"Someone replied","relatedPostId":10,"relatedCommentId":20,"relatedUserId":2,"actionUrl":"/posts/10","actionText":"View","isRead":false}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/notifications' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "COMMENT_REPLY",
  "recipientId": 1,
  "title": "New reply",
  "message": "Someone replied",
  "relatedPostId": 10,
  "relatedCommentId": 20,
  "relatedUserId": 2,
  "actionUrl": "/posts/10",
  "actionText": "View",
  "isRead": false
}'
```

**Response**

```json
{"id":1,"type":"COMMENT_REPLY","recipientId":1,"title":"New reply","message":"Someone replied","relatedPostId":10,"relatedPostTitle":null,"relatedCommentId":20,"relatedCommentPreview":null,"relatedUserId":2,"relatedUsername":null,"actionUrl":"/posts/10","actionText":"View","isRead":false,"createdAt":"2026-05-02T10:00:00Z","readAt":null}
```
### GET `/api/notifications`

Get notifications.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/notifications/unread`

Get unread notifications.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications/unread?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### PUT `/api/notifications/{notificationId}/read`

Mark one notification read.

**Auth:** User

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/notifications/1/read' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`200 OK` with an empty body
### PUT `/api/notifications/read-all`

Mark all notifications read.

**Auth:** User

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/notifications/read-all' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`200 OK` with an empty body
### DELETE `/api/notifications/{notificationId}`

Delete notification.

**Auth:** User

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/notifications/1' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`
### DELETE `/api/notifications`

Delete all notifications.

**Auth:** User

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/notifications' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`
### GET `/api/notifications/count/unread`

Get unread count.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications/count/unread' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
0
```
### GET `/api/notifications/type/{type}`

Get notifications by type. Current implementation returns an empty page.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/notifications/type/COMMENT_REPLY?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
