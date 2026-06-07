# WebSocket API

Lambrk uses Spring STOMP messaging over WebSocket. `WebSocketConfig` registers `/ws` with native WebSocket and SockJS, application prefix `/app`, broker prefixes `/topic` and `/queue`, and user prefix `/user`.

---

## Handshake

### GET `/ws`

Open a websocket/STOMP connection.

**Auth:** JWT should be sent by the client during connect, commonly in STOMP `CONNECT` headers.

**What to send**

| Parameter       | Location     | Type   | Required | Description    |
| --------------- | ------------ | ------ | -------- | -------------- |
| `Authorization` | STOMP header | string | **Yes**  | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body | Description                     |
| ------ | ---- | ------------------------------- |
| `101`  | —    | WebSocket upgrade successful    |
| `401`  | —    | JWT invalid (STOMP ERROR frame) |

**Browser/STOMP example**

```javascript
const client = new Client({
  brokerURL: "ws://localhost:9500/ws",
  connectHeaders: { Authorization: "Bearer <token>" },
});

client.onConnect = () => {
  client.subscribe("/user/queue/notifications", (message) =>
    console.log(message.body),
  );
  client.publish({ destination: "/app/subscribe/notifications" });
};

client.activate();
```

**cURL note:** curl is not useful for STOMP messaging after websocket upgrade. Use a STOMP client, browser client, or websocket test tool.

---

## Client Messages

### SEND `/app/connect`

Confirm connection and push unread count.

**What to send**

| Parameter       | Location     | Type   | Required | Description    |
| --------------- | ------------ | ------ | -------- | -------------- |
| `Authorization` | STOMP header | string | **Yes**  | `Bearer <jwt>` |

No request payload.

**Response**

| Destination                              | Body    | Description             |
| ---------------------------------------- | ------- | ----------------------- |
| `/user/queue/connected`                  | text    | Connection confirmation |
| `/user/queue/notifications/unread-count` | integer | Unread count            |

**STOMP send**

```javascript
client.publish({ destination: "/app/connect" });
```

**Responses**

```text
/user/queue/connected -> "Connected to Lambrk WebSocket"
/user/queue/notifications/unread-count -> 0
```

---

### SEND `/app/subscribe/notifications`

Subscribe to notification updates.

**What to send**

| Parameter       | Location     | Type   | Required | Description    |
| --------------- | ------------ | ------ | -------- | -------------- |
| `Authorization` | STOMP header | string | **Yes**  | `Bearer <jwt>` |

No request payload.

**Response**

| Destination                 | Body                           | Description          |
| --------------------------- | ------------------------------ | -------------------- |
| `/user/queue/notifications` | `NotificationResponse` or list | Notification updates |

**STOMP send**

```javascript
client.publish({ destination: "/app/subscribe/notifications" });
```

**Response**

```json
[]
```

Sent to `/user/queue/notifications`. Current controller returns an empty placeholder list.

---

### SEND `/app/subscribe/posts/{postId}`

Subscribe to updates for one post.

**What to send**

| Parameter       | Location     | Type   | Required | Description      |
| --------------- | ------------ | ------ | -------- | ---------------- |
| `Authorization` | STOMP header | string | **Yes**  | `Bearer <jwt>`   |
| `postId`        | Path         | UUID   | **Yes**  | Post UUID        |
| `body`          | Body         | string | **Yes**  | Post UUID string |

**Request payload**

```json
"019e5a43-e0c2-7baa-9f6d-b9b9b82afb16"
```

**Response**

| Destination                            | Body | Description               |
| -------------------------------------- | ---- | ------------------------- |
| `/user/queue/post/{postId}/subscribed` | text | Subscription confirmation |

**STOMP send**

```javascript
client.publish({
  destination: "/app/subscribe/posts/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
  body: "019e5a43-e0c2-7baa-9f6d-b9b9b82afb16",
});
```

**Response**

```text
/user/queue/post/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16/subscribed -> "Subscribed to post updates: 019e5a43-e0c2-7baa-9f6d-b9b9b82afb16"
```

---

### SEND `/app/subscribe/community/{communityId}`

Subscribe to updates for one community.

**What to send**

| Parameter       | Location     | Type   | Required | Description           |
| --------------- | ------------ | ------ | -------- | --------------------- |
| `Authorization` | STOMP header | string | **Yes**  | `Bearer <jwt>`        |
| `communityId`   | Path         | UUID   | **Yes**  | Community UUID        |
| `body`          | Body         | string | **Yes**  | Community UUID string |

**Request payload**

```json
"019e5a43-e0c2-7baa-9f6d-b9b9b82afb15"
```

**Response**

| Destination                                      | Body | Description               |
| ------------------------------------------------ | ---- | ------------------------- |
| `/user/queue/community/{communityId}/subscribed` | text | Subscription confirmation |

**STOMP send**

```javascript
client.publish({
  destination: "/app/subscribe/community/019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  body: "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
});
```

**Response**

```text
/user/queue/community/019e5a43-e0c2-7baa-9f6d-b9b9b82afb15/subscribed -> "Subscribed to community updates: 019e5a43-e0c2-7baa-9f6d-b9b9b82afb15"
```

---

## Chat Messaging

See [MESSAGES_API.md](./MESSAGES_API.md) for the REST endpoints (inbox, history, send, mark-read, delete, unread count).

The WebSocket transport below is the preferred real-time path for sending messages and receiving events.

---

### SEND `/app/chat.send`

Send a new message to another user in real time.

**Auth:** JWT in STOMP `CONNECT` headers.

**What to send**

| Field               | Type   | Required | Description                                          |
| ------------------- | ------ | -------- | ---------------------------------------------------- |
| `recipientUsername` | string | **Yes**  | Username of the recipient                            |
| `content`           | string | **Yes**  | Text content (max 5000 characters)                   |
| `messageType`       | string | No       | `TEXT` (default), `IMAGE`, `VIDEO`, `FILE`, `SYSTEM` |
| `attachmentUrl`     | string | No       | URL of an uploaded attachment                        |
| `attachmentType`    | string | No       | MIME type of the attachment                          |

**Request payload**

```json
{
  "recipientUsername": "john_doe",
  "content": "Hey, how are you?",
  "messageType": "TEXT"
}
```

**STOMP send**

```javascript
client.publish({
  destination: "/app/chat.send",
  body: JSON.stringify({
    recipientUsername: "john_doe",
    content: "Hey, how are you?",
    messageType: "TEXT",
  }),
});
```

**Response** — pushed to both sender and recipient:

| Destination            | Body                          | Description                            |
| ---------------------- | ----------------------------- | -------------------------------------- |
| `/user/queue/messages` | `ChatMessageWebSocketPayload` | Delivered to both sender and recipient |
| `/user/queue/errors`   | string                        | Sent to sender only if delivery fails  |

```json
{
  "eventType": "MESSAGE_SENT",
  "message": {
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
  },
  "messageId": null,
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "actorUsername": "jane_doe",
  "timestamp": "2026-06-07T13:00:00Z"
}
```

---

### SEND `/app/chat.read`

Mark a specific message as read. Only the recipient should call this.

Side effects: sets `isRead = true`, resets unread counter in the conversation, and pushes a `READ_RECEIPT` event to the original sender.

**Auth:** JWT in STOMP `CONNECT` headers.

**What to send**

| Field  | Type   | Required | Description                     |
| ------ | ------ | -------- | ------------------------------- |
| `body` | string | **Yes**  | MongoDB ObjectId of the message |

**STOMP send**

```javascript
client.publish({
  destination: "/app/chat.read",
  body: "664f1a2b3c4d5e6f7a8b9c0e",
});
```

**Response** — pushed to the original sender:

| Destination            | Body                          | Description          |
| ---------------------- | ----------------------------- | -------------------- |
| `/user/queue/messages` | `ChatMessageWebSocketPayload` | `READ_RECEIPT` event |

```json
{
  "eventType": "READ_RECEIPT",
  "message": null,
  "messageId": "664f1a2b3c4d5e6f7a8b9c0e",
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "actorUsername": "john_doe",
  "timestamp": "2026-06-07T13:05:00Z"
}
```

---

### SEND `/app/chat.typing`

Broadcast a typing indicator to the other participant in a conversation.

The payload is published to the public topic `/topic/chat/typing/{conversationId}`. Both participants should subscribe to this topic when the conversation is open.

**Auth:** JWT in STOMP `CONNECT` headers.

**What to send**

| Field  | Type   | Required | Description     |
| ------ | ------ | -------- | --------------- |
| `body` | string | **Yes**  | Conversation ID |

**STOMP send**

```javascript
client.publish({
  destination: "/app/chat.typing",
  body: "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
});
```

**Subscribe to typing events**

```javascript
client.subscribe(
  "/topic/chat/typing/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  (message) => {
    const payload = JSON.parse(message.body);
    // payload.actorUsername is typing
  },
);
```

**Response** — broadcast on topic:

| Destination                           | Body                          | Description                        |
| ------------------------------------- | ----------------------------- | ---------------------------------- |
| `/topic/chat/typing/{conversationId}` | `ChatMessageWebSocketPayload` | `TYPING` event for all subscribers |

```json
{
  "eventType": "TYPING",
  "message": null,
  "messageId": null,
  "conversationId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14_019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
  "actorUsername": "jane_doe",
  "timestamp": "2026-06-07T13:00:05Z"
}
```

---

## Server Push Destinations

| Destination                           | Payload                                                                                      |
| ------------------------------------- | -------------------------------------------------------------------------------------------- |
| `/user/queue/notifications`           | `NotificationResponse` or list                                                               |
| `/user/queue/posts`                   | `PostResponse`                                                                               |
| `/user/queue/comments`                | `CommentResponse`                                                                            |
| `/user/queue/karma`                   | integer                                                                                      |
| `/user/queue/votes`                   | object                                                                                       |
| `/user/queue/messages`                | `ChatMessageWebSocketPayload` (eventType: `MESSAGE_SENT`, `READ_RECEIPT`, `MESSAGE_DELETED`) |
| `/user/queue/errors`                  | string (chat send failures)                                                                  |
| `/topic/posts/{postId}`               | `PostResponse`                                                                               |
| `/topic/posts/{postId}/comments`      | `CommentResponse`                                                                            |
| `/topic/communities/{communityId}`    | object                                                                                       |
| `/topic/announcements`                | string                                                                                       |
| `/topic/user-status/{username}`       | string                                                                                       |
| `/topic/chat/typing/{conversationId}` | `ChatMessageWebSocketPayload` (eventType: `TYPING`)                                          |
