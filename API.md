# Lambrk Backend API Documentation

A Reddit-like social news aggregation platform API built with Spring Boot 3.5 and Java 21.

## Base URL

```
http://localhost:9500
```

## Authentication

Most endpoints require JWT authentication. Use the `/api/auth/login` or `/api/auth/register` endpoints to obtain a token.

### Headers

Include the JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

---

## Endpoints

### Authentication

#### Register User

```
POST /api/auth/register
```

Register a new user account.

**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "displayName": "string"
}
```

**cURL:**
```bash
curl -X POST http://localhost:9500/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securePassword123",
    "displayName": "John Doe"
  }'
```

**Response (201):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "displayName": "John Doe"
  }
}
```

---

#### Login

```
POST /api/auth/login
```

Authenticate and receive JWT tokens.

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**cURL:**
```bash
curl -X POST http://localhost:9500/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securePassword123"
  }'
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "displayName": "John Doe"
  }
}
```

---

#### Refresh Token

```
POST /api/auth/refresh
```

Refresh an expired access token.

**cURL:**
```bash
curl -X POST http://localhost:9500/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "displayName": "John Doe"
  }
}
```

---

### Posts

#### Create Post

```
POST /api/posts
```

Create a new post. Requires authentication.

**Request Body:**
```json
{
  "title": "My First Post",
  "content": "This is the content of my post.",
  "url": "",
  "subredditId": 1,
  "postType": "TEXT",
  "flairText": null,
  "flairCssClass": null,
  "isSpoiler": false,
  "isOver18": false
}
```

**cURL:**
```bash
curl -X POST http://localhost:9500/api/posts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Post",
    "content": "This is the content of my post.",
    "url": "",
    "subredditId": 1,
    "postType": "TEXT",
    "isSpoiler": false,
    "isOver18": false
  }'
```

**Response (201):**
```json
{
  "id": 1,
  "title": "My First Post",
  "content": "This is the content of my post.",
  "url": null,
  "postType": "TEXT",
  "score": 1,
  "upvoteCount": 1,
  "downvoteCount": 0,
  "commentCount": 0,
  "viewCount": 0,
  "isSpoiler": false,
  "isStickied": false,
  "isLocked": false,
  "isArchived": false,
  "isRemoved": false,
  "isOver18": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "author": {
    "id": 1,
    "username": "johndoe",
    "displayName": "John Doe",
    "karma": 1
  },
  "subreddit": {
    "id": 1,
    "name": "programming",
    "title": "Programming"
  }
}
```

---

#### Get Post

```
GET /api/posts/{postId}
```

Get a single post by ID.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/posts/1 \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "id": 1,
  "title": "My First Post",
  "content": "This is the content of my post.",
  "url": null,
  "postType": "TEXT",
  "score": 1,
  "upvoteCount": 1,
  "downvoteCount": 0,
  "commentCount": 0,
  "viewCount": 5,
  "isSpoiler": false,
  "isStickied": false,
  "isLocked": false,
  "isArchived": false,
  "isRemoved": false,
  "isOver18": false,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z",
  "author": {
    "id": 1,
    "username": "johndoe",
    "displayName": "John Doe",
    "karma": 1
  },
  "subreddit": {
    "id": 1,
    "name": "programming",
    "title": "Programming"
  },
  "userVote": "UPVOTE"
}
```

---

#### Get Hot Posts

```
GET /api/posts/hot?page=0&size=20
```

Get posts sorted by score (hot).

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/hot?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "My First Post",
      "score": 100,
      ...
    },
    {
      "id": 2,
      "title": "Another Post",
      "score": 50,
      ...
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

---

#### Get New Posts

```
GET /api/posts/new?page=0&size=20
```

Get newest posts.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/new?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Get Top Posts

```
GET /api/posts/top?page=0&size=20
```

Get top rated posts.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/top?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Get Posts by Subreddit

```
GET /api/posts/subreddit/{subredditId}?page=0&size=20
```

Get posts from a specific subreddit.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/subreddit/1?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Get Posts by User

```
GET /api/posts/user/{userId}?page=0&size=20
```

Get posts by a specific user.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/user/1?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Search Posts

```
GET /api/posts/search?query=text&page=0&size=20
```

Search posts by query.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/search?query=javascript&page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Update Post

```
PUT /api/posts/{postId}
```

Update an existing post. Requires authentication as post author.

**cURL:**
```bash
curl -X PUT http://localhost:9500/api/posts/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "content": "Updated content.",
    "subredditId": 1,
    "postType": "TEXT"
  }'
```

**Response (200):**
```json
{
  "id": 1,
  "title": "Updated Title",
  "content": "Updated content.",
  ...
}
```

---

#### Delete Post

```
DELETE /api/posts/{postId}
```

Delete a post. Requires authentication as post author.

**cURL:**
```bash
curl -X DELETE http://localhost:9500/api/posts/1 \
  -H "Authorization: Bearer <token>"
```

**Response (204):** No content

---

#### Get Stickied Posts

```
GET /api/posts/stickied?subredditId=1
```

Get stickied posts from a subreddit.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/posts/stickied?subredditId=1" \
  -H "Authorization: Bearer <token>"
```

---

### Comments

#### Create Comment

```
POST /api/comments
```

Create a new comment. Requires authentication.

**Request Body:**
```json
{
  "content": "Great post! Thanks for sharing.",
  "postId": 1,
  "parentId": null
}
```

**cURL:**
```bash
curl -X POST http://localhost:9500/api/comments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Great post! Thanks for sharing.",
    "postId": 1,
    "parentId": null
  }'
```

**Response (201):**
```json
{
  "id": 1,
  "content": "Great post! Thanks for sharing.",
  "score": 1,
  "isEdited": false,
  "isDeleted": false,
  "depthLevel": 0,
  "createdAt": "2024-01-15T10:30:00Z",
  "author": {
    "id": 1,
    "username": "johndoe",
    "displayName": "John Doe"
  },
  "postId": 1
}
```

---

#### Get Comment

```
GET /api/comments/{commentId}
```

Get a specific comment.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/comments/1 \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "id": 1,
  "content": "Great post! Thanks for sharing.",
  "score": 1,
  "upvoteCount": 1,
  "downvoteCount": 0,
  "replyCount": 0,
  "isEdited": false,
  "isDeleted": false,
  "isCollapsed": false,
  "depthLevel": 0,
  "createdAt": "2024-01-15T10:30:00Z",
  "author": {
    "id": 1,
    "username": "johndoe",
    "displayName": "John Doe"
  },
  "postId": 1
}
```

---

#### Get Comments by Post

```
GET /api/comments/post/{postId}?page=0&size=20
```

Get comments for a post.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/comments/post/1?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Get Comment Replies

```
GET /api/comments/{commentId}/replies
```

Get replies to a comment.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/comments/1/replies \
  -H "Authorization: Bearer <token>"
```

---

#### Get Comments by User

```
GET /api/comments/user/{userId}?page=0&size=20
```

Get comments by a user.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/comments/user/1?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

#### Update Comment

```
PUT /api/comments/{commentId}
```

Update a comment.

**cURL:**
```bash
curl -X PUT http://localhost:9500/api/comments/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: text/plain" \
  -d 'Updated comment content'
```

**Response (200):**
```json
{
  "id": 1,
  "content": "Updated comment content",
  "isEdited": true,
  ...
}
```

---

#### Delete Comment

```
DELETE /api/comments/{commentId}
```

Delete a comment.

**cURL:**
```bash
curl -X DELETE http://localhost:9500/api/comments/1 \
  -H "Authorization: Bearer <token>"
```

**Response (204):** No content

---

#### Search Comments

```
GET /api/comments/search?query=text&page=0&size=20
```

Search comments.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/comments/search?query=helpful&page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

---

### Subreddits

#### Create Subreddit

```
POST /api/subreddits
```

Create a new subreddit. Requires authentication.

**Request Body:**
```json
{
  "name": "technology",
  "title": "Technology",
  "description": "讨论最新科技",
  "sidebar": "Welcome to Tech!",
  "rules": "Be respectful.",
  "isNsfw": false,
  "isPrivate": false
}
```

**cURL:**
```bash
curl -X POST http://localhost:9500/api/subreddits \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "technology",
    "title": "Technology",
    "description": "讨论最新科技",
    "isNsfw": false,
    "isPrivate": false
  }'
```

**Response (201):**
```json
{
  "id": 2,
  "name": "technology",
  "title": "Technology",
  "description": "讨论最新科技",
  "sidebar": null,
  "rules": null,
  "isNsfw": false,
  "isPrivate": false,
  "subscriberCount": 1,
  "postCount": 0,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

#### Get Subreddit

```
GET /api/subreddits/{subredditId}
```

Get a subreddit by ID.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/subreddits/1 \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "id": 1,
  "name": "programming",
  "title": "Programming",
  "description": "Subreddit for programming",
  "sidebar": "Rules: Be nice",
  "rules": "Be respectful",
  "isNsfw": false,
  "isPrivate": false,
  "subscriberCount": 100,
  "postCount": 50,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

#### Get Subreddit by Name

```
GET /api/subreddits/r/{name}
```

Get a subreddit by name (e.g., /r/programming).

**cURL:**
```bash
curl -X GET http://localhost:9500/api/subreddits/r/programming \
  -H "Authorization: Bearer <token>"
```

---

#### Get Trending Subreddits

```
GET /api/subreddits/trending?page=0&size=20
```

Get trending subreddits.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/subreddits/trending?page=0&size=20"
```

---

#### Get Public Subreddits

```
GET /api/subreddits?page=0&size=20
```

Get public subreddits.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/subreddits?page=0&size=20"
```

---

#### Subscribe to Subreddit

```
POST /api/subreddits/{subredditId}/subscribe
```

Subscribe to a subreddit.

**cURL:**
```bash
curl -X POST http://localhost:9500/api/subreddits/1/subscribe \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "success": true,
  "subscribed": true
}
```

---

#### Unsubscribe from Subreddit

```
DELETE /api/subreddits/{subredditId}/subscribe
```

Unsubscribe from a subreddit.

**cURL:**
```bash
curl -X DELETE http://localhost:9500/api/subreddits/1/subscribe \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "success": true,
  "subscribed": false
}
```

---

### Users

#### Get User

```
GET /api/users/{userId}
```

Get a user by ID.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/users/1
```

**Response (200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "displayName": "John Doe",
  "bio": null,
  "avatarUrl": null,
  "karma": 100,
  "postCount": 10,
  "commentCount": 50,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

---

#### Get User by Username

```
GET /api/users/username/{username}
```

Get a user by username.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/users/username/johndoe
```

---

#### Get Current User

```
GET /api/users/me
```

Get the authenticated user's profile.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/users/me \
  -H "Authorization: Bearer <token>"
```

---

#### Get Top Users

```
GET /api/users/top?page=0&size=20
```

Get users with highest karma.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/users/top?page=0&size=20"
```

---

#### Search Users

```
GET /api/users/search?query=text&page=0&size=20
```

Search users.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/users/search?query=john&page=0&size=20"
```

---

### Votes

#### Vote on Post

```
POST /api/votes/post
```

Vote on a post. Requires authentication.

**Request Body:**
```json
{
  "postId": 1,
  "voteType": "UPVOTE"
}
```

`voteType` can be: UPVOTE, DOWNVOTE, NONE

**cURL:**
```bash
curl -X POST http://localhost:9500/api/votes/post \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "postId": 1,
    "voteType": "UPVOTE"
  }'
```

**Response (200):** OK (empty body)

---

#### Vote on Comment

```
POST /api/votes/comment
```

Vote on a comment.

**cURL:**
```bash
curl -X POST http://localhost:9500/api/votes/comment \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "commentId": 1,
    "voteType": "UPVOTE"
  }'
```

**Response (200):** OK (empty body)

---

### Feed

#### Get Personalized Feed

```
POST /api/feed
```

Get a personalized feed for the user.

**Request Body:**
```json
{
  "userId": 1,
  "sortBy": "HOT",
  "limit": 20,
  "includeNsfw": false
}
```

`sortBy` can be: HOT, NEW, TOP, CONTROVERSIAL

**cURL:**
```bash
curl -X POST http://localhost:9500/api/feed \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "sortBy": "HOT",
    "limit": 20,
    "includeNsfw": false
  }'
```

**Response (200):**
```json
{
  "posts": [...],
  "algorithm": "PERSONALIZED",
  "algorithmInfo": {
    "name": "Personalized Feed",
    "description": "Feed customized based on your activity",
    "factors": ["Subreddit subscriptions", "Upvote history", "Comment history"]
  }
}
```

---

### Search

#### Search All

```
POST /api/search
```

Search across posts, comments, users, and subreddits.

**Request Body:**
```json
{
  "query": "javascript",
  "type": "ALL",
  "page": 0,
  "size": 20
}
```

`type` can be: ALL, POSTS, COMMENTS, USERS, SUBREDDITS

**cURL:**
```bash
curl -X POST http://localhost:9500/api/search \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "javascript",
    "type": "ALL",
    "page": 0,
    "size": 20
  }'
```

**Response (200):**
```json
{
  "posts": [...],
  "comments": [...],
  "users": [...],
  "subreddits": [...],
  "metadata": {
    "query": "javascript",
    "totalResults": 50,
    "page": 0,
    "size": 20
  }
}
```

---

### Recommendations

#### Get Recommendations

```
POST /api/recommendations
```

Get AI-powered recommendations.

**Request Body:**
```json
{
  "userId": 1,
  "type": "POSTS",
  "limit": 10
}
```

`type` can be: POSTS, SUBREDDITS, USERS, COMMENTS

**cURL:**
```bash
curl -X POST http://localhost:9500/api/recommendations \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "type": "POSTS",
    "limit": 10
  }'
```

**Response (200):**
```json
{
  "posts": [
    {
      "id": 5,
      "title": "Recommended Post",
      "score": 80,
      ...
    }
  ],
  "explanation": "Based on your activity in 5 communities and 20 interactions",
  "confidence": 0.75,
  "factors": ["User interaction history", "Subreddit preferences"]
}
```

---

### File Uploads

#### Upload File

```
POST /api/files/upload
```

Upload a file (image, video, etc.). Requires authentication.

**cURL:**
```bash
curl -X POST http://localhost:9500/api/files/upload \
  -H "Authorization: Bearer <token>" \
  -F "file=@image.jpg" \
  -F "type=POST_IMAGE" \
  -F "isPublic=true"
```

**Response (201):**
```json
{
  "id": 1,
  "fileName": "abc123.jpg",
  "originalFileName": "image.jpg",
  "fileUrl": "/api/files/1",
  "thumbnailUrl": "/api/files/thumbnails/1",
  "fileSize": 102400,
  "mimeType": "image/jpeg",
  "isPublic": true,
  "uploadedAt": "2024-01-15T10:30:00Z"
}
```

---

#### Get File

```
GET /api/files/{fileId}
```

Get a file by ID.

**cURL:**
```bash
curl -X GET http://localhost:9500/api/files/1
```

**Response (200):**
```json
{
  "id": 1,
  "fileName": "abc123.jpg",
  "originalFileName": "image.jpg",
  "fileUrl": "/api/files/1",
  "fileSize": 102400,
  "mimeType": "image/jpeg",
  "isPublic": true,
  "uploadedAt": "2024-01-15T10:30:00Z"
}
```

---

#### Get User Files

```
GET /api/files/user/{userId}?page=0&size=20
```

Get files uploaded by a user.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/files/user/1?page=0&size=20"
```

---

### Notifications

#### Get Notifications

```
GET /api/notifications?page=0&size=20
```

Get notifications for the authenticated user.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/notifications?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "content": [
    {
      "id": 1,
      "type": "COMMENT_REPLY",
      "title": "New reply to your comment",
      "message": "Someone replied to your comment",
      "isRead": false,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "unreadCount": 1
}
```

---

#### Mark Notification as Read

```
PUT /api/notifications/{notificationId}/read
```

Mark a notification as read.

**cURL:**
```bash
curl -X PUT http://localhost:9500/api/notifications/1/read \
  -H "Authorization: Bearer <token>"
```

**Response (200):**
```json
{
  "id": 1,
  "type": "COMMENT_REPLY",
  "title": "New reply to your comment",
  "message": "Someone replied to your comment",
  "isRead": true,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

#### Mark All Notifications as Read

```
PUT /api/notifications/read-all
```

Mark all notifications as read.

**cURL:**
```bash
curl -X PUT http://localhost:9500/api/notifications/read-all \
  -H "Authorization: Bearer <token>"
```

---

### Admin

#### Perform Admin Action

```
POST /api/admin/action
```

Admin-only endpoint to perform administrative actions.

**Request Body:**
```json
{
  "action": "BAN_USER",
  "targetId": 1,
  "reason": "Violation of terms",
  "notifyUser": true,
  "durationDays": 0,
  "permanent": true
}
```

`action` can be: BAN_USER, SUSPEND_USER, DELETE_POST, DELETE_COMMENT, LOCK_POST, LOCK_COMMENT, REMOVE_MODERATOR, ADD_MODERATOR, BAN_SUBREDDIT, QUARANTINE_POST, QUARANTINE_COMMENT

**cURL:**
```bash
curl -X POST http://localhost:9500/api/admin/action \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "action": "BAN_USER",
    "targetId": 2,
    "reason": "Spam account",
    "notifyUser": true,
    "permanent": true
  }'
```

**Response (200):**
```json
{
  "id": 1,
  "type": "BAN_USER",
  "targetId": 2,
  "targetType": "User",
  "reason": "Spam account",
  "performedBy": 1,
  "isPermanent": true,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

#### Get Admin Actions

```
GET /api/admin/actions?page=0&size=20
```

Get all admin actions. Requires ADMIN role.

**cURL:**
```bash
curl -X GET "http://localhost:9500/api/admin/actions?page=0&size=20" \
  -H "Authorization: Bearer <admin_token>"
```

---

### WebSocket

#### Connect

```
WS /ws
```

WebSocket endpoint for real-time updates. Uses STOMP protocol.

**Subscribe to destinations:**
- `/user/queue/notifications` - User notifications
- `/user/queue/connected` - Connection confirmation
- `/topic/posts/{postId}` - Post updates
- `/topic/posts/{postId}/comments` - Comment updates

**Example STOMP frame (connect):**
```
CONNECT
accept-version:1.2
host:localhost

^@
```

**Example STOMP frame (subscribe):**
```
SUBSCRIBE
id:sub-0
destination:/user/queue/notifications

^@
```

---

## Error Responses

### Standard Error Format

```json
{
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/users/999",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 404
}
```

### Common HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict (duplicate resource) |
| 429 | Too Many Requests |
| 500 | Internal Server Error |

### Error Examples

**401 Unauthorized:**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "status": 401
}
```

**404 Not Found:**
```json
{
  "error": "Not Found",
  "message": "User not found with id: 999",
  "status": 404
}
```

**409 Conflict:**
```json
{
  "error": "Conflict",
  "message": "Username already exists: johndoe",
  "status": 409
}
```

---

## Rate Limiting

API endpoints are rate-limited:
- POST /api/posts: 100 requests/minute
- POST /api/comments: 500 requests/minute
- Other endpoints: Default limits

**Rate Limit Response (429):**
```json
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Try again later.",
  "status": 429
}
```

---

## OpenAPI/Swagger

Access the interactive API documentation at:
- Swagger UI: `http://localhost:9500/swagger-ui.html`
- OpenAPI JSON: `http://localhost:9500/v3/api-docs`

---

## Actuator Endpoints

| Endpoint | Description | Example |
|----------|-------------|---------|
| /actuator/health | Health check | `curl http://localhost:9500/actuator/health` |
| /actuator/info | Build information | `curl http://localhost:9500/actuator/info` |
| /actuator/metrics | Metrics | `curl http://localhost:9500/actuator/metrics` |
| /actuator/prometheus | Prometheus metrics | `curl http://localhost:9500/actuator/prometheus` |
| /actuator/env | Environment properties | `curl http://localhost:9500/actuator/env` |
| /actuator/loggers | Logger configuration | `curl http://localhost:9500/actuator/loggers` |

---

## Notes

- All timestamps are in ISO-8601 format (e.g., `2024-01-15T10:30:00Z`)
- Pagination uses 0-based indexing
- Default page size is 20
- Maximum page size is 100
- `null` values in responses indicate optional/not set fields
- Replace `<token>` with your actual JWT token
- Replace `<admin_token>` with an admin JWT token