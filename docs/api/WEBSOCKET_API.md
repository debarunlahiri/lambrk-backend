# WebSocket API

Real-time updates and notifications via WebSocket connections.

---

## Connection

### WebSocket Endpoint

```
ws://localhost:9500/ws
```

### SockJS Fallback

```
http://localhost:9500/ws (with SockJS)
```

### Connection Headers

```
Authorization: Bearer <access_token>
Origin: http://localhost:9500
```

---

## Message Protocol

All messages use **STOMP** protocol over WebSocket.

### Connection Flow

1. Connect to `/ws` endpoint
2. Authenticate via JWT token
3. Subscribe to desired topics
4. Receive real-time updates

---

## Client Subscriptions

### User-Specific Topics

| Topic Pattern | Description |
|---------------|-------------|
| `/user/{username}/queue/connected` | Connection confirmation |
| `/user/{username}/queue/notifications` | User notifications |
| `/user/{username}/queue/karma` | Karma updates |
| `/user/{username}/queue/votes` | Vote updates |
| `/user/{username}/queue/posts` | User's post updates |
| `/user/{username}/queue/comments` | User's comment updates |

### Public Topics

| Topic Pattern | Description |
|---------------|-------------|
| `/topic/posts/{postId}` | Post-specific updates |
| `/topic/posts/{postId}/comments` | Comment updates for post |
| `/topic/sublambrks/{sublambrkId}` | Sublambrk updates |
| `/topic/announcements` | System announcements |
| `/topic/user-status/{username}` | User online status |

---

## Client Messages

### Connect

```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
  Authorization: 'Bearer ' + token
}, function(frame) {
  console.log('Connected: ' + frame);
  
  // Subscribe to notifications
  stompClient.subscribe('/user/' + username + '/queue/notifications', function(message) {
    const notification = JSON.parse(message.body);
    console.log('New notification:', notification);
  });
});
```

### Subscribe to Post Updates

```javascript
// Subscribe to specific post
stompClient.subscribe('/topic/posts/123', function(message) {
  const post = JSON.parse(message.body);
  console.log('Post updated:', post);
});

// Subscribe to post comments
stompClient.subscribe('/topic/posts/123/comments', function(message) {
  const comment = JSON.parse(message.body);
  console.log('New comment:', comment);
});
```

### Subscribe to Sublambrk

```javascript
stompClient.subscribe('/topic/sublambrks/programming', function(message) {
  const update = JSON.parse(message.body);
  console.log('Sublambrk update:', update);
});
```

---

## Server Messages

### Notification Updates

```json
{
  "id": 100,
  "type": "COMMENT_REPLY",
  "title": "New reply to your comment",
  "message": "john_doe replied to your comment",
  "isRead": false,
  "createdAt": "2026-02-07T16:30:00Z"
}
```

### Post Updates

```json
{
  "id": 1,
  "title": "Updated post title",
  "score": 25,
  "commentCount": 8,
  "updatedAt": "2026-02-07T16:30:00Z"
}
```

### Comment Updates

```json
{
  "id": 10,
  "content": "Updated comment content",
  "score": 5,
  "replyCount": 3,
  "updatedAt": "2026-02-07T16:30:00Z"
}
```

### Karma Updates

```json
{
  "userId": 1,
  "newKarma": 1250,
  "change": +5,
  "reason": "post_upvote"
}
```

### Vote Updates

```json
{
  "targetId": 1,
  "targetType": "POST",
  "voteType": "UPVOTE",
  "newScore": 25,
  "userVote": "UPVOTE"
}
```

### System Announcements

```json
{
  "id": "ann-001",
  "title": "System Maintenance",
  "message": "Platform will be down for maintenance at 2 AM UTC",
  "priority": "HIGH",
  "timestamp": "2026-02-07T16:30:00Z"
}
```

---

## API Integration

### Sending Messages from Server

```java
// Send notification to specific user
messagingTemplate.convertAndSendToUser(
    username,
    "/queue/notifications",
    notification
);

// Broadcast post update
messagingTemplate.convertAndSend(
    "/topic/posts/" + postId,
    postUpdate
);

// Send karma update
messagingTemplate.convertAndSendToUser(
    username,
    "/queue/karma",
    newKarma
);
```

### Client-Side Message Examples

```javascript
// Send subscription request
stompClient.send('/app/subscribe/posts/123', {}, JSON.stringify({
    postId: 123
}));

// Send connection acknowledgment
stompClient.send('/app/connect', {}, JSON.stringify({
    timestamp: Date.now()
}));
```

---

## Connection Management

### Connection Events

| Event | Description |
|-------|-------------|
| Connect | New WebSocket connection established |
| Disconnect | WebSocket connection closed |
| Subscribe | Client subscribes to topic |
| Unsubscribe | Client unsubscribes from topic |

### Connection Tracking

- **Active Connections**: Tracked in memory
- **User Sessions**: Multiple sessions per user allowed
- **Heartbeat**: Automatic connection health checks
- **Reconnection**: Automatic reconnection on disconnect

### Rate Limiting

- **Connections**: 10 connections per user
- **Messages**: 100 messages per minute per connection
- **Subscriptions**: 50 subscriptions per connection

---

## Security

### Authentication

- **JWT Required**: All connections must provide valid JWT
- **Token Validation**: Performed on connection establishment
- **User Context**: Extracted from JWT for authorization
- **Session Management**: Tokens validated periodically

### Authorization

- **Topic Access**: Users can only access their own topics
- **Public Topics**: All users can access public topics
- **Admin Topics**: Admin-only topics for system messages
- **Content Filtering**: NSFW content respects user preferences

---

## Performance

### Scaling

- **Horizontal Scaling**: Multiple WebSocket servers supported
- **Session Affinity**: Sticky sessions for stateful operations
- **Message Broadcasting**: Efficient pub/sub model
- **Connection Pooling**: Optimized resource usage

### Optimization

- **Message Compression**: Large messages compressed
- **Batch Updates**: Multiple updates batched together
- **Selective Updates**: Only send relevant updates
- **Connection Cleanup**: Automatic cleanup of dead connections

---

## Error Handling

### Connection Errors

| Error | Description | Client Action |
|-------|-------------|---------------|
| 401   | Invalid JWT token | Re-authenticate |
| 403   | Access denied | Check permissions |
| 429   | Rate limit exceeded | Back off and retry |
| 500   | Server error | Reconnect |

### Message Errors

| Error | Description | Client Action |
|-------|-------------|---------------|
| Invalid JSON | Malformed message | Fix message format |
| Unauthorized | Access to restricted topic | Check permissions |
| Not Found | Topic doesn't exist | Verify topic name |

---

## Metrics

WebSocket connections emit these metrics:

- `websocket.connections` - Total connections
- `websocket.connected` - New connections
- `websocket.disconnected` - Disconnections
- `websocket.subscribed` - New subscriptions
- `websocket.unsubscribed` - Unsubscriptions
- `websocket.messages.sent` - Messages sent
- `websocket.messages.received` - Messages received

---

## Browser Compatibility

### Supported Browsers

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

### Polyfills

For older browsers, include:

```html
<script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
```

---

## Example Client Implementation

```javascript
class LambrkWebSocket {
    constructor(token, username) {
        this.token = token;
        this.username = username;
        this.socket = null;
        this.stompClient = null;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect() {
        this.socket = new SockJS('/ws');
        this.stompClient = Stomp.over(this.socket);
        
        this.stompClient.connect({
            Authorization: 'Bearer ' + this.token
        }, this.onConnected.bind(this), this.onError.bind(this));
    }

    onConnected(frame) {
        console.log('WebSocket connected:', frame);
        this.reconnectAttempts = 0;
        
        // Subscribe to user-specific topics
        this.stompClient.subscribe('/user/' + this.username + '/queue/notifications', 
            this.onNotification.bind(this));
        
        this.stompClient.subscribe('/user/' + this.username + '/queue/karma', 
            this.onKarmaUpdate.bind(this));
        
        // Send connection message
        this.stompClient.send('/app/connect', {}, JSON.stringify({
            timestamp: Date.now()
        }));
    }

    onError(error) {
        console.error('WebSocket error:', error);
        this.reconnect();
    }

    reconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            setTimeout(() => {
                console.log('Reconnecting... attempt', this.reconnectAttempts);
                this.connect();
            }, 1000 * this.reconnectAttempts);
        }
    }

    subscribeToPost(postId) {
        this.stompClient.subscribe('/topic/posts/' + postId, this.onPostUpdate.bind(this));
        this.stompClient.subscribe('/topic/posts/' + postId + '/comments', 
            this.onCommentUpdate.bind(this));
        
        this.stompClient.send('/app/subscribe/posts/' + postId, {}, JSON.stringify({
            postId: postId
        }));
    }

    onNotification(message) {
        const notification = JSON.parse(message.body);
        this.showNotification(notification);
    }

    onKarmaUpdate(message) {
        const karma = JSON.parse(message.body);
        this.updateKarmaDisplay(karma);
    }

    onPostUpdate(message) {
        const post = JSON.parse(message.body);
        this.updatePostDisplay(post);
    }

    onCommentUpdate(message) {
        const comment = JSON.parse(message.body);
        this.updateCommentDisplay(comment);
    }

    showNotification(notification) {
        // Display notification in UI
        console.log('New notification:', notification);
    }

    updateKarmaDisplay(karma) {
        // Update karma display
        console.log('Karma updated:', karma);
    }

    updatePostDisplay(post) {
        // Update post in UI
        console.log('Post updated:', post);
    }

    updateCommentDisplay(comment) {
        // Update comment in UI
        console.log('Comment updated:', comment);
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// Usage
const ws = new LambrkWebSocket(token, username);
ws.connect();
```
