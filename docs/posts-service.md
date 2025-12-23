# Posts Service API Documentation

Social media-style posts management service for the Lambrk platform.

**Base URL**: `http://localhost:3104` (Direct) or `http://localhost:3100/api/posts` (Gateway)

## Table of Contents

- [Overview](#overview)
- [Endpoints](#endpoints)
- [Data Models](#data-models)
- [Examples](#examples)

## Overview

The Posts Service handles:
- Social media-style post CRUD operations
- Text and image content support
- View counting
- Integration with likes, comments, and shares

### Post Characteristics

- **Content**: Text-based with optional image
- **Format**: Social media feed style
- **Use Case**: Thoughts, updates, announcements with images
- **Discovery**: Featured in trending and user feeds

## Endpoints

### 1. Create Post

Create a new social media post.

**Endpoint:** `POST /api/posts`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "My Post Title",
  "content": "This is the content of my post. It can be quite long and detailed...",
  "imageUrl": "https://example.com/images/post.jpg",
  "status": "published"
}
```

**Validation Rules:**
- `title`: Required, non-empty string
- `content`: Required, non-empty string
- `imageUrl`: Optional, valid URL
- `status`: Optional, one of: 'draft', 'published'

**Success Response (201):**
```json
{
  "success": true,
  "data": {
    "post": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "My Post Title",
      "content": "This is the content of my post. It can be quite long and detailed...",
      "imageUrl": "https://example.com/images/post.jpg",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 0,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

---

### 2. Get All Posts

Retrieve a paginated list of posts.

**Endpoint:** `GET /api/posts`

**Authentication:** Not required

**Query Parameters:**
- `limit`: Number of posts per page (default: 20)
- `offset`: Pagination offset (default: 0)
- `status`: Filter by status: 'draft', 'published'

**Example:**
```
GET /api/posts?limit=20&offset=0&status=published
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "posts": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "Amazing Discovery",
        "content": "Today I learned something amazing...",
        "imageUrl": "https://example.com/images/post.jpg",
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 1500,
        "status": "published",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 3. Get User's Posts

Get all posts created by the authenticated user.

**Endpoint:** `GET /api/posts/my-posts`

**Authentication:** Required

**Query Parameters:**
- `limit`: Number of posts per page (default: 20)
- `offset`: Pagination offset (default: 0)

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "posts": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "title": "My Thoughts",
        "content": "Here's what I think about...",
        "imageUrl": "https://example.com/images/post.jpg",
        "userId": "660e8400-e29b-41d4-a716-446655440000",
        "views": 50,
        "status": "published",
        "createdAt": "2024-01-01T00:00:00.000Z",
        "updatedAt": "2024-01-01T00:00:00.000Z"
      }
    ]
  }
}
```

---

### 4. Get Post by ID

Retrieve a specific post by its ID.

**Endpoint:** `GET /api/posts/:id`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "post": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Amazing Discovery",
      "content": "Today I learned something amazing...",
      "imageUrl": "https://example.com/images/post.jpg",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 1500,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-01T00:00:00.000Z"
    }
  }
}
```

**Error Response (404):**
```json
{
  "success": false,
  "error": {
    "message": "Post not found",
    "statusCode": 404
  }
}
```

---

### 5. Update Post

Update an existing post. Users can only update their own posts.

**Endpoint:** `PUT /api/posts/:id`

**Authentication:** Required

**Request Body:**
```json
{
  "title": "Updated Post Title",
  "content": "Updated post content...",
  "imageUrl": "https://example.com/images/new-post.jpg",
  "status": "published"
}
```

**Success Response (200):**
```json
{
  "success": true,
  "data": {
    "post": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "title": "Updated Post Title",
      "content": "Updated post content...",
      "imageUrl": "https://example.com/images/new-post.jpg",
      "userId": "660e8400-e29b-41d4-a716-446655440000",
      "views": 1500,
      "status": "published",
      "createdAt": "2024-01-01T00:00:00.000Z",
      "updatedAt": "2024-01-02T00:00:00.000Z"
    }
  }
}
```

**Error Response (403):**
```json
{
  "success": false,
  "error": {
    "message": "You can only update your own posts",
    "statusCode": 403
  }
}
```

---

### 6. Delete Post

Delete a post. Users can only delete their own posts.

**Endpoint:** `DELETE /api/posts/:id`

**Authentication:** Required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Post deleted successfully"
}
```

---

### 7. Increment Views

Increment the view count for a post.

**Endpoint:** `POST /api/posts/:id/views`

**Authentication:** Not required

**Success Response (200):**
```json
{
  "success": true,
  "message": "Views incremented"
}
```

---

## Data Models

### Post Model

```typescript
interface Post {
  id: string;
  title: string;
  content: string;
  imageUrl?: string;
  userId: string;
  views: number;
  status: 'draft' | 'published';
  createdAt: Date;
  updatedAt: Date;
}
```

---

## Examples

### Complete Post Creation Flow

```javascript
// 1. Create post
const createResponse = await fetch('http://localhost:3100/api/posts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    title: 'My Post',
    content: 'This is my post content. It can be quite detailed...',
    imageUrl: 'https://example.com/image.jpg',
    status: 'published'
  })
});

const { data: { post } } = await createResponse.json();

// 2. Get post feed
const feedResponse = await fetch('http://localhost:3100/api/posts?limit=20&status=published');
const { data: { posts } } = await feedResponse.json();

// 3. Track view when user reads
await fetch(`http://localhost:3100/api/posts/${post.id}/views`, {
  method: 'POST'
});
```

### Post Feed Implementation

```javascript
// Paginated feed
let offset = 0;
const limit = 20;

async function loadMorePosts() {
  const response = await fetch(
    `http://localhost:3100/api/posts?limit=${limit}&offset=${offset}&status=published`
  );
  const { data: { posts } } = await response.json();
  
  // Render posts in feed
  posts.forEach(renderPost);
  
  offset += limit;
}

// Load initial posts
loadMorePosts();

// Load more on scroll
window.addEventListener('scroll', () => {
  if (isNearBottom()) {
    loadMorePosts();
  }
});
```

### Post with Interactions

```javascript
// Get post with engagement stats
const postId = '550e8400-e29b-41d4-a716-446655440000';

// Get post data
const postResponse = await fetch(`http://localhost:3100/api/posts/${postId}`);
const { data: { post } } = await postResponse.json();

// Get like stats
const likeResponse = await fetch(`http://localhost:3100/api/likes/post/${postId}`);
const { data: { stats } } = await likeResponse.json();
// stats: { likes: 100, dislikes: 5, userLikeType: 'like' }

// Get comment count
const commentResponse = await fetch(`http://localhost:3100/api/comments/post/${postId}/count`);
const { data: { count } } = await commentResponse.json();

// Like the post
await fetch('http://localhost:3100/api/likes', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    contentType: 'post',
    contentId: postId,
    likeType: 'like'
  })
});

// Add comment
await fetch('http://localhost:3100/api/comments', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    contentType: 'post',
    contentId: postId,
    commentText: 'Great post!'
  })
});
```

### Post Card Component (React Example)

```jsx
function PostCard({ post }) {
  const [stats, setStats] = useState({ likes: 0, dislikes: 0 });
  const [commentCount, setCommentCount] = useState(0);
  
  useEffect(() => {
    // Load engagement stats
    Promise.all([
      fetch(`/api/likes/post/${post.id}`).then(r => r.json()),
      fetch(`/api/comments/post/${post.id}/count`).then(r => r.json())
    ]).then(([likeData, commentData]) => {
      setStats(likeData.data.stats);
      setCommentCount(commentData.data.count);
    });
    
    // Track view
    fetch(`/api/posts/${post.id}/views`, { method: 'POST' });
  }, [post.id]);
  
  return (
    <div className="post-card">
      <h2>{post.title}</h2>
      {post.imageUrl && <img src={post.imageUrl} alt={post.title} />}
      <p>{post.content}</p>
      <div className="engagement">
        <span>{stats.likes} likes</span>
        <span>{commentCount} comments</span>
        <span>{post.views} views</span>
      </div>
    </div>
  );
}
```

---

## Best Practices

### Content Guidelines

- **Title**: Clear, descriptive (3-100 characters)
- **Content**: Well-formatted, readable
- **Images**: Optional but encouraged for engagement
- **Length**: No strict limit, but keep it readable

### Image Specifications

Recommended specifications for post images:
- **Aspect Ratio**: 16:9 or 1:1
- **Resolution**: At least 1200x675 or 1080x1080
- **Format**: JPEG or PNG
- **File Size**: < 5MB

### User Experience

- Display posts in reverse chronological order
- Show engagement metrics (likes, comments, views)
- Support rich text formatting in content
- Implement infinite scroll for feed
- Optimize images for different screen sizes

---

## Rate Limiting

- **Create/Update/Delete**: 30 operations per hour per user (higher rate for posts)
- **Read Operations**: 100 requests per 15 minutes per IP

---

## Integration with Other Services

### Likes/Dislikes
See [Interaction Service - Likes API](./interaction-service.md#likesdislikes-api)

### Comments
See [Interaction Service - Comments API](./interaction-service.md#comments-api)

### Trending
See [Interaction Service - Trending API](./interaction-service.md#trending-api)

---

[Back to Documentation Index](./README.md)
