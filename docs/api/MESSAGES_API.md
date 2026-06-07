# Messages API

Base path: `/api/messages`. JWT required on all endpoints.

Users are stored in **PostgreSQL**. Messages and conversations are stored in **MongoDB** (`chat_messages` and `chat_conversations` collections).

The primary real-time delivery path is WebSocket (STOMP). These REST endpoints serve as a fallback and as the canonical API for fetching inbox and message history. See [WEBSOCKET_API.md](./WEBSOCKET_API.md) for the real-time transport.

---

## Message Types

| Value    | Preview shown in inbox         |
| -------- | ------------------------------ |
| `TEXT`   | First 80 characters of content |
| `IMAGE`  | `[Image]`                      |
| `VIDEO`  | `[Video]`                      |
| `FILE`   | `[File]`                       |
| `SYSTEM` | Full content                   |

---

## WebSocket Event Types

| `eventType`       | When emitted                      | Payload fields populated                                    |
| ----------------- | --------------------------------- | ----------------------------------------------------------- |
| `MESSAGE_SENT`    | A new message is delivered        | `message`, `conversationId`, `actorUsername`, `timestamp`   |
| `READ_RECEIPT`    | Recipient marks a message as read | `messageId`, `conversationId`, `actorUsername`, `timestamp` |
| `TYPING`          | User is typing                    | `conversationId`, `actorUsername`, `timestamp`              |
| `MESSAGE_DELETED` | A message is soft-deleted         | `message`, `conversationId`, `actorUsername`, `timestamp`   |

---

### GET `/api/messages/conversations`

Get the authenticated user's conversation inbox, sorted by most recent message.

**Auth:** User

**What to send**

| Parameter       | Location | Type    | Required | Default | Description           |
| --------------- | -------- | ------- | -------- | ------- | --------------------- |
| `Authorization` | Header   | string  | **Yes**  | —       | `Bearer <jwt>`        |
| `page`          | Query    | integer | No       | `0`     | Zero-based page index |
| `size`          | Query    | integer | No       | `20`    | Page size             |

No request body.

**Response**

| Status | Body                         | Description            |
| ------ | ---------------------------- | ---------------------- |
| `200`  | `Page<ConversationResponse>` | Paginated inbox        |
| `401`  | error                        | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/messages/conversations?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "664f1a2b3c4d5e6f7a8b9c0d",
      "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
      "otherParticipantId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
      "otherParticipantUsername": "john_doe",
      "lastMessage": "Hey, how are you?",
      "lastMessageSenderId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
      "lastMessageAt": "2026-06-07T13:00:00Z",
      "unreadCount": 2,
      "createdAt": "2026-06-01T10:00:00Z"
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

---

### GET `/api/messages/conversations/{username}`

Open or create a 1:1 conversation with the given user. Idempotent — calling it multiple times returns the same conversation.

Use this when a user taps on another user's profile to start chatting. The returned `conversationId` is used for loading history and subscribing to WebSocket events.

**Auth:** User

**What to send**

| Parameter       | Location | Type   | Required | Description            |
| --------------- | -------- | ------ | -------- | ---------------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>`         |
| `username`      | Path     | string | **Yes**  | Target user's username |

No request body.

**Response**

| Status | Body                   | Description                                      |
| ------ | ---------------------- | ------------------------------------------------ |
| `200`  | `ConversationResponse` | Existing or newly created conversation           |
| `400`  | error                  | Attempting to start a conversation with yourself |
| `401`  | error                  | JWT missing or invalid                           |
| `404`  | error                  | Target username does not exist                   |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/messages/conversations/john_doe' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0d",
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "otherParticipantId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "otherParticipantUsername": "john_doe",
  "lastMessage": null,
  "lastMessageSenderId": null,
  "lastMessageAt": null,
  "unreadCount": 0,
  "createdAt": "2026-06-07T13:00:00Z"
}
```

---

### GET `/api/messages/{conversationId}`

Get paginated message history for a conversation, newest first. Excludes soft-deleted messages. Only participants of the conversation may call this.

**Auth:** User (must be a participant)

**What to send**

| Parameter        | Location | Type    | Required | Default | Description                   |
| ---------------- | -------- | ------- | -------- | ------- | ----------------------------- |
| `Authorization`  | Header   | string  | **Yes**  | —       | `Bearer <jwt>`                |
| `conversationId` | Path     | string  | **Yes**  | —       | Deterministic conversation ID |
| `page`           | Query    | integer | No       | `0`     | Zero-based page index         |
| `size`           | Query    | integer | No       | `30`    | Page size                     |

No request body.

**conversationId format**

The conversation ID is deterministic: `{smallerUUID}_{largerUUID}`. Obtain it from a `ConversationResponse` via the inbox or the open-conversation endpoint.

**Response**

| Status | Body                        | Description                                     |
| ------ | --------------------------- | ----------------------------------------------- |
| `200`  | `Page<ChatMessageResponse>` | Paginated messages, newest first                |
| `401`  | error                       | JWT missing or invalid                          |
| `403`  | error                       | Caller is not a participant in the conversation |
| `404`  | error                       | Conversation not found                          |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/messages/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15?page=0&size=30' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "content": [
    {
      "id": "664f1a2b3c4d5e6f7a8b9c0e",
      "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
      "senderId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
      "senderUsername": "jane_doe",
      "senderAvatarUrl": "https://cdn.lambrk.com/avatars/jane.jpg",
      "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
      "recipientUsername": "john_doe",
      "content": "Hey, how are you?",
      "messageType": "TEXT",
      "attachmentUrl": null,
      "attachmentType": null,
      "isRead": true,
      "readAt": "2026-06-07T13:05:00Z",
      "isDeleted": false,
      "createdAt": "2026-06-07T13:00:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 30,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### POST `/api/messages`

Send a message to another user via REST.

> **Note:** The preferred delivery path is the WebSocket endpoint (`/app/chat.send`) to avoid the HTTP round-trip. After persisting, the service always pushes a real-time `MESSAGE_SENT` WebSocket event to both the sender and recipient regardless of which transport was used.

**Auth:** User

**What to send**

| Parameter           | Location | Type   | Required | Description                                                    |
| ------------------- | -------- | ------ | -------- | -------------------------------------------------------------- |
| `Authorization`     | Header   | string | **Yes**  | `Bearer <jwt>`                                                 |
| `recipientUsername` | Body     | string | **Yes**  | Username of the message recipient                              |
| `content`           | Body     | string | **Yes**  | Text content (max 5000 characters)                             |
| `messageType`       | Body     | string | No       | `TEXT`, `IMAGE`, `VIDEO`, `FILE`, `SYSTEM`. Defaults to `TEXT` |
| `attachmentUrl`     | Body     | string | No       | URL of an uploaded attachment                                  |
| `attachmentType`    | Body     | string | No       | MIME type of the attachment, e.g. `image/jpeg`                 |

**Request body**

```json
{
  "recipientUsername": "john_doe",
  "content": "Hey, how are you?",
  "messageType": "TEXT",
  "attachmentUrl": null,
  "attachmentType": null
}
```

**Response**

| Status | Body                  | Description                                          |
| ------ | --------------------- | ---------------------------------------------------- |
| `200`  | `ChatMessageResponse` | The saved message                                    |
| `400`  | error                 | Validation failure or attempting to message yourself |
| `401`  | error                 | JWT missing or invalid                               |
| `404`  | error                 | Recipient username does not exist                    |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/messages' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "recipientUsername": "john_doe",
  "content": "Hey, how are you?",
  "messageType": "TEXT"
}'
```

**Response**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0e",
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "senderId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "senderUsername": "jane_doe",
  "senderAvatarUrl": "https://cdn.lambrk.com/avatars/jane.jpg",
  "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "recipientUsername": "john_doe",
  "content": "Hey, how are you?",
  "messageType": "TEXT",
  "attachmentUrl": null,
  "attachmentType": null,
  "isRead": false,
  "readAt": null,
  "isDeleted": false,
  "createdAt": "2026-06-07T13:00:00Z"
}
```

---

### PUT `/api/messages/{messageId}/read`

Mark a single message as read. Only the recipient of the message may call this. Idempotent — calling on an already-read message is a no-op.

Side effects:

- Sets `isRead = true` and `readAt` timestamp on the message.
- Resets the caller's unread counter in the parent conversation.
- Pushes a `READ_RECEIPT` WebSocket event to the original sender.

**Auth:** User (must be the message recipient)

**What to send**

| Parameter       | Location | Type   | Required | Description                     |
| --------------- | -------- | ------ | -------- | ------------------------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>`                  |
| `messageId`     | Path     | string | **Yes**  | MongoDB ObjectId of the message |

No request body.

**Response**

| Status | Body                  | Description                         |
| ------ | --------------------- | ----------------------------------- |
| `200`  | `ChatMessageResponse` | Updated message                     |
| `401`  | error                 | JWT missing or invalid              |
| `403`  | error                 | Caller is not the message recipient |
| `404`  | error                 | Message not found                   |

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/messages/664f1a2b3c4d5e6f7a8b9c0e/read' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0e",
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "senderId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "senderUsername": "jane_doe",
  "senderAvatarUrl": "https://cdn.lambrk.com/avatars/jane.jpg",
  "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "recipientUsername": "john_doe",
  "content": "Hey, how are you?",
  "messageType": "TEXT",
  "attachmentUrl": null,
  "attachmentType": null,
  "isRead": true,
  "readAt": "2026-06-07T13:05:00Z",
  "isDeleted": false,
  "createdAt": "2026-06-07T13:00:00Z"
}
```

---

### DELETE `/api/messages/{messageId}`

Soft-delete a message. Only the original sender may delete their own message. The MongoDB document is not physically removed — `isDeleted` is set to `true` and content is redacted in all future responses.

Side effects:

- Sets `isDeleted = true` and `deletedAt` timestamp on the message.
- Pushes a `MESSAGE_DELETED` WebSocket event to both the sender and recipient.

**Auth:** User (must be the original sender)

**What to send**

| Parameter       | Location | Type   | Required | Description                     |
| --------------- | -------- | ------ | -------- | ------------------------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>`                  |
| `messageId`     | Path     | string | **Yes**  | MongoDB ObjectId of the message |

No request body.

**Response**

| Status | Body                  | Description                           |
| ------ | --------------------- | ------------------------------------- |
| `200`  | `ChatMessageResponse` | Updated message with content redacted |
| `401`  | error                 | JWT missing or invalid                |
| `403`  | error                 | Caller is not the original sender     |
| `404`  | error                 | Message not found                     |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/messages/664f1a2b3c4d5e6f7a8b9c0e' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "id": "664f1a2b3c4d5e6f7a8b9c0e",
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "senderId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "senderUsername": "jane_doe",
  "senderAvatarUrl": null,
  "recipientId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "recipientUsername": "john_doe",
  "content": null,
  "messageType": "TEXT",
  "attachmentUrl": null,
  "attachmentType": null,
  "isRead": true,
  "readAt": "2026-06-07T13:05:00Z",
  "isDeleted": true,
  "createdAt": "2026-06-07T13:00:00Z"
}
```

---

### GET `/api/messages/unread/count`

Get the total number of unread messages across all conversations for the authenticated user. Use this to drive the navigation badge.

**Auth:** User

**What to send**

| Parameter       | Location | Type   | Required | Description    |
| --------------- | -------- | ------ | -------- | -------------- |
| `Authorization` | Header   | string | **Yes**  | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body    | Description                            |
| ------ | ------- | -------------------------------------- |
| `200`  | integer | Total unread message count (0 or more) |
| `401`  | error   | JWT missing or invalid                 |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/messages/unread/count' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
5
```

---

## Response Schemas

### ChatMessageResponse

| Field               | Type     | Description                                      |
| ------------------- | -------- | ------------------------------------------------ |
| `id`                | string   | MongoDB ObjectId of the message                  |
| `conversationId`    | string   | Deterministic conversation ID                    |
| `senderId`          | UUID     | UUID of the sender                               |
| `senderUsername`    | string   | Username of the sender                           |
| `senderAvatarUrl`   | string   | Avatar URL of the sender                         |
| `recipientId`       | UUID     | UUID of the recipient                            |
| `recipientUsername` | string   | Username of the recipient                        |
| `content`           | string   | Text content. `null` if the message is deleted   |
| `messageType`       | string   | `TEXT`, `IMAGE`, `VIDEO`, `FILE`, or `SYSTEM`    |
| `attachmentUrl`     | string   | Attachment URL. `null` if deleted or not present |
| `attachmentType`    | string   | MIME type of the attachment                      |
| `isRead`            | boolean  | Whether the recipient has read the message       |
| `readAt`            | ISO-8601 | Timestamp when it was read                       |
| `isDeleted`         | boolean  | Whether the message has been soft-deleted        |
| `createdAt`         | ISO-8601 | Timestamp when the message was sent              |

### ConversationResponse

| Field                      | Type     | Description                                              |
| -------------------------- | -------- | -------------------------------------------------------- |
| `id`                       | string   | MongoDB document ID                                      |
| `conversationId`           | string   | Deterministic conversation ID (`smallerUUID_largerUUID`) |
| `otherParticipantId`       | UUID     | UUID of the other participant                            |
| `otherParticipantUsername` | string   | Username of the other participant                        |
| `lastMessage`              | string   | Preview of the last message sent                         |
| `lastMessageSenderId`      | UUID     | UUID of who sent the last message                        |
| `lastMessageAt`            | ISO-8601 | When the last message was sent                           |
| `unreadCount`              | integer  | Unread count for the requesting user                     |
| `createdAt`                | ISO-8601 | When the conversation was first created                  |
