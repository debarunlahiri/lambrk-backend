# WebSocket API

Lambrk uses Spring STOMP messaging over WebSocket. `WebSocketConfig` registers `/ws` with native WebSocket and SockJS, application prefix `/app`, broker prefixes `/topic` and `/queue`, and user prefix `/user`.

## Handshake

### GET `/ws`

Open a websocket/STOMP connection.

**Auth:** JWT should be sent by the client during connect, commonly in STOMP `CONNECT` headers.

**Browser/STOMP example**

```javascript
const client = new Client({
  brokerURL: 'ws://localhost:9500/ws',
  connectHeaders: { Authorization: 'Bearer <token>' }
});

client.onConnect = () => {
  client.subscribe('/user/queue/notifications', message => console.log(message.body));
  client.publish({ destination: '/app/subscribe/notifications' });
};

client.activate();
```

**cURL note:** curl is not useful for STOMP messaging after websocket upgrade. Use a STOMP client, browser client, or websocket test tool.

## Client Messages

### SEND `/app/connect`

Confirm connection and push unread count.

**Request payload:** none

**STOMP send**

```javascript
client.publish({ destination: '/app/connect' });
```

**Responses**

```text
/user/queue/connected -> "Connected to Lambrk WebSocket"
/user/queue/notifications/unread-count -> 0
```

### SEND `/app/subscribe/notifications`

Subscribe to notification updates.

**Request payload:** none

**STOMP send**

```javascript
client.publish({ destination: '/app/subscribe/notifications' });
```

**Response**

```json
[]
```

Sent to `/user/queue/notifications`. Current controller returns an empty placeholder list.

### SEND `/app/subscribe/posts/{postId}`

Subscribe to updates for one post.

**Request payload**

```json
"019e5a43-e0c2-7baa-9f6d-b9b9b82afb16"
```

**STOMP send**

```javascript
client.publish({ destination: '/app/subscribe/posts/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16', body: '019e5a43-e0c2-7baa-9f6d-b9b9b82afb16' });
```

**Response**

```text
/user/queue/post/019e5a43-e0c2-7baa-9f6d-b9b9b82afb16/subscribed -> "Subscribed to post updates: 019e5a43-e0c2-7baa-9f6d-b9b9b82afb16"
```

### SEND `/app/subscribe/community/{communityId}`

Subscribe to updates for one community.

**Request payload**

```json
"019e5a43-e0c2-7baa-9f6d-b9b9b82afb15"
```

**STOMP send**

```javascript
client.publish({ destination: '/app/subscribe/community/019e5a43-e0c2-7baa-9f6d-b9b9b82afb15', body: '019e5a43-e0c2-7baa-9f6d-b9b9b82afb15' });
```

**Response**

```text
/user/queue/community/019e5a43-e0c2-7baa-9f6d-b9b9b82afb15/subscribed -> "Subscribed to community updates: 019e5a43-e0c2-7baa-9f6d-b9b9b82afb15"
```

## Server Push Destinations

| Destination | Payload |
| --- | --- |
| `/user/queue/notifications` | `NotificationResponse` or list |
| `/user/queue/posts` | `PostResponse` |
| `/user/queue/comments` | `CommentResponse` |
| `/user/queue/karma` | integer |
| `/user/queue/votes` | object |
| `/topic/posts/{postId}` | `PostResponse` |
| `/topic/posts/{postId}/comments` | `CommentResponse` |
| `/topic/communities/{communityId}` | object |
| `/topic/announcements` | string |
| `/topic/user-status/{username}` | string |
