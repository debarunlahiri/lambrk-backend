# Feed API

Base URL: `/api/feed`

All endpoints require **JWT authentication**.

The Feed API provides personalized content feeds based on user interactions, preferences, and an algorithmic ranking system.

---

## GET `/api/feed`

Get a personalized feed of posts and suggested users.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int | 20 | Number of posts to return (1-100) |
| sortBy | String | algorithm | Sort method: `algorithm`, `hot`, `new`, `top` |
| includeNsfw | boolean | false | Include NSFW content |
| fromFollowingOnly | boolean | false | Only show posts from subscribed subreddits |
| timeDecayFactor | double | 1.0 | Time decay factor for freshness (0.1-3.0) |

### cURL Example

```bash
curl "http://localhost:8080/api/feed?limit=20&sortBy=algorithm&includeNsfw=false" \
  -H "Authorization: Bearer <access_token>"
```

### Response `200 OK`

```json
{
  "posts": [
    {
      "id": 1,
      "title": "Advanced Spring Boot Patterns",
      "content": "Virtual threads are amazing for high-concurrency workloads...",
      "url": null,
      "postType": "TEXT",
      "thumbnailUrl": null,
      "flairText": "Tutorial",
      "isSpoiler": false,
      "isOver18": false,
      "score": 245,
      "upvoteCount": 250,
      "downvoteCount": 5,
      "commentCount": 42,
      "viewCount": 1200,
      "algorithmScore": 87.5,
      "reasons": [
        "From your subscribed community",
        "Popular post",
        "Matches your content preferences",
        "Fresh content"
      ],
      "author": {
        "id": 2,
        "username": "expert_dev",
        "displayName": "Expert Developer",
        "avatarUrl": "https://...",
        "karma": 15000,
        "isVerified": true,
        "type": "VERIFIED"
      },
      "subreddit": {
        "id": 1,
        "name": "programming",
        "title": "Programming",
        "iconImageUrl": "https://...",
        "isUserSubscribed": true
      },
      "createdAt": "2026-02-07T14:00:00Z",
      "userInteraction": {
        "hasUpvoted": false,
        "hasDownvoted": false,
        "hasCommented": false,
        "hasViewed": false,
        "isSaved": false,
        "isHidden": false,
        "viewCount": 0,
        "lastInteractionAt": null
      }
    }
  ],
  "suggestedUsers": [
    {
      "id": 5,
      "username": "java_master",
      "displayName": "Java Master",
      "bio": "Java enthusiast and Spring Boot expert",
      "avatarUrl": "https://...",
      "karma": 8500,
      "isVerified": true,
      "type": "VERIFIED",
      "relevanceScore": 78.5,
      "reasons": [
        "Active in 3 communities you follow",
        "Verified user"
      ],
      "mutualSubreddits": 3,
      "commonInterests": ["programming", "java", "springboot"]
    }
  ],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "User engagement history",
      "Post popularity (upvotes/downvotes)",
      "Time decay (freshness)",
      "Subreddit affinity",
      "Content type preferences",
      "Author reputation"
    ],
    "processingTimeMs": 45
  },
  "totalAvailable": 150,
  "hasMore": true
}
```

---

## POST `/api/feed`

Get personalized feed with advanced filtering options.

### Request Body

```json
{
  "userId": 1,
  "limit": 20,
  "sortBy": "algorithm",
  "postTypes": ["TEXT", "IMAGE", "VIDEO"],
  "includeNsfw": false,
  "includeFromFollowingOnly": false,
  "timeDecayFactor": 1.0
}
```

### cURL Example

```bash
curl -X POST http://localhost:8080/api/feed \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "limit": 30,
    "sortBy": "algorithm",
    "postTypes": ["TEXT", "IMAGE"],
    "includeNsfw": false,
    "timeDecayFactor": 1.5
  }'
```

### Response `200 OK`

Same as GET endpoint.

---

## GET `/api/feed/hot`

Get trending posts based on popularity and recent activity.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int | 20 | Number of posts to return |

### cURL Example

```bash
curl "http://localhost:8080/api/feed/hot?limit=20" \
  -H "Authorization: Bearer <access_token>"
```

---

## GET `/api/feed/new`

Get the most recent posts with minimal algorithmic ranking.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int | 20 | Number of posts to return |

### cURL Example

```bash
curl "http://localhost:8080/api/feed/new?limit=20" \
  -H "Authorization: Bearer <access_token>"
```

---

## GET `/api/feed/top`

Get highest scoring posts.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int | 20 | Number of posts to return |
| timePeriod | String | all | Time period: `all`, `day`, `week`, `month`, `year` |

### cURL Example

```bash
curl "http://localhost:8080/api/feed/top?limit=20&timePeriod=week" \
  -H "Authorization: Bearer <access_token>"
```

---

## GET `/api/feed/discover`

Discover new content from subreddits you don't follow.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int | 20 | Number of posts to return |

### cURL Example

```bash
curl "http://localhost:8080/api/feed/discover?limit=20" \
  -H "Authorization: Bearer <access_token>"
```

---

## Algorithm Details

### Scoring Factors

The algorithm considers multiple factors when ranking posts:

| Factor | Weight | Description |
|--------|--------|-------------|
| Popularity | 25% | Based on upvotes, downvotes, comments, views |
| Freshness | 20% | Time decay for newer content |
| Subreddit Affinity | 25% | Your subscribed communities and activity |
| Content Type | 15% | Your preferred post types (TEXT, IMAGE, VIDEO, etc.) |
| Author Reputation | 10% | Author's karma and verification status |

### User Types

Users are categorized into types based on their activity:

| Type | Criteria |
|------|----------|
| REGULAR | Normal user |
| INFLUENCER | Karma > 10,000 |
| VERIFIED | Verified account |
| NEW_USER | Recently joined |
| MODERATOR | Subreddit moderator |
| ADMIN | Site admin |

### Time Decay

The `timeDecayFactor` parameter controls how quickly posts lose relevance:

- **0.1**: Minimal decay (newest first)
- **1.0**: Normal decay (default)
- **2.0**: Fast decay (hot/trending)
- **3.0**: Very fast decay (real-time)

---

## Error Responses

| Status | Condition |
|--------|-----------|
| 400 | Invalid request parameters |
| 401 | Not authenticated |
| 429 | Rate limit exceeded |
| 503 | Feed generation failed |

---

## Metrics

All feed endpoints emit these metrics:

- `lambrk.feed.generated` - Feed generation count
- `lambrk.feed.error` - Feed generation failures
- `lambrk.recommendation.error` - Recommendation errors
