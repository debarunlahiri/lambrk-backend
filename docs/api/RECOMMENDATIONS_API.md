# Recommendations API

Base URL: `/api/recommendations`

All endpoints require **JWT authentication**.

---

## POST `/api/recommendations`

Get personalized recommendations using ML-based analysis.

### Request Body

```json
{
  "userId": 1,
  "type": "POSTS",
  "limit": 20,
  "excludeSubreddits": ["gaming", "memes"],
  "excludeUsers": ["spammer123"],
  "includeNSFW": false,
  "includeOver18": false,
  "contextSubredditId": "1",
  "contextPostId": "1"
}
```

### Validation Rules

| Field            | Rule                                                    |
|------------------|---------------------------------------------------------|
| userId           | Required, must exist                                    |
| type             | `POSTS`, `SUBREDDITS`, `USERS`, `COMMENTS`              |
| limit            | 1–100, default 20                                        |
| excludeSubreddits| Optional, list of subreddit names to exclude        |
| excludeUsers     | Optional, list of usernames to exclude              |
| includeNSFW      | Boolean, default false                                  |
| includeOver18    | Boolean, default false                                  |
| contextSubredditId| Optional, context for recommendations              |
| contextPostId    | Optional, context for recommendations              |

### Response `200 OK`

```json
{
  "type": "POSTS",
  "posts": [
    {
      "id": 1,
      "title": "Advanced Spring Boot Patterns",
      "content": "Exploring advanced patterns...",
      "author": { "id": 2, "username": "expert_dev" },
      "subreddit": { "id": 1, "name": "programming" },
      "score": 45,
      "commentCount": 12,
      "createdAt": "2026-02-07T14:00:00Z"
    }
  ],
  "subreddits": [],
  "users": [],
  "comments": [],
  "explanation": "Based on your activity in 5 communities and 25 interactions, we've selected 20 posts that match your interests and engagement patterns.",
  "confidence": 0.85,
  "factors": [
    "User interaction history",
    "Subreddit preferences",
    "Content similarity",
    "Trending topics"
  ]
}
```

---

## GET `/api/recommendations/posts/{userId}`

Get post recommendations for a specific user.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |

### Query Parameters

| Param            | Type    | Default | Description |
|------------------|---------|---------|-------------|
| limit            | int     | 20      | Number of recommendations |
| excludeSubreddits| String  | —       | Comma-separated subreddit names |
| excludeUsers     | String  | —       | Comma-separated usernames |
| includeNSFW      | boolean | false   | Include NSFW content |
| includeOver18    | boolean | false   | Include 18+ content |
| contextSubredditId| String  | —       | Context subreddit |
| contextPostId    | String  | —       | Context post |

### Response `200 OK`

Same shape as POST endpoint with only posts populated.

---

## GET `/api/recommendations/subreddits/{userId}`

Get subreddit recommendations for a user.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |

### Query Parameters

| Param            | Type    | Default | Description |
|------------------|---------|---------|-------------|
| limit            | int     | 20      | Number of recommendations |
| excludeSubreddits| String  | —       | Comma-separated subreddit names |
| includeNSFW      | boolean | false   | Include NSFW subreddits |
| includeOver18    | boolean | false   | Include 18+ subreddits |

### Response `200 OK`

```json
{
  "type": "SUBREDDITS",
  "posts": [],
  "subreddits": [
    {
      "id": 5,
      "name": "java",
      "title": "Java Programming",
      "description": "All things Java",
      "memberCount": 15000,
      "isUserSubscribed": false,
      "isUserModerator": false
    }
  ],
  "users": [],
  "comments": [],
  "explanation": "Based on your subscriptions to programming communities, we recommend these related subreddits.",
  "confidence": 0.78,
  "factors": [
    "User subscriptions",
    "Similar communities",
    "Trending communities",
    "Content preferences"
  ]
}
```

---

## GET `/api/recommendations/users/{userId}`

Get user recommendations (similar users to follow).

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |

### Query Parameters

| Param        | Type   | Default | Description |
|--------------|--------|---------|-------------|
| limit        | int    | 20      | Number of recommendations |
| excludeUsers | String | —       | Comma-separated usernames |

### Response `200 OK`

```json
{
  "type": "USERS",
  "posts": [],
  "subreddits": [],
  "users": [
    {
      "id": 3,
      "username": "code_master",
      "displayName": "Code Master",
      "karma": 2500,
      "isActive": true,
      "isVerified": true
    }
  ],
  "comments": [],
  "explanation": "Users with similar interests and activity patterns to yours.",
  "confidence": 0.72,
  "factors": [
    "Similar interests",
    "Active in same communities",
    "Content overlap",
    "Engagement patterns"
  ]
}
```

---

## GET `/api/recommendations/comments/{userId}`

Get comment recommendations.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int  | 20      | Number of recommendations |

---

## GET `/api/recommendations/context/{userId}`

Get contextual recommendations based on current context.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| userId | Long | User ID |

### Query Parameters

| Param             | Type   | Default | Description |
|-------------------|--------|---------|-------------|
| contextSubredditId| String | —       | Current subreddit context |
| contextPostId    | String | —       | Current post context |
| type              | String | posts   | Recommendation type |
| limit             | int    | 20      | Number of recommendations |

### Response `200 OK`

Context-aware recommendations based on current viewing context.

---

## GET `/api/recommendations/trending`

Get trending recommendations for all users.

### Query Parameters

| Param | Type   | Default | Description |
|-------|--------|---------|-------------|
| type  | String | posts   | Recommendation type |
| limit | int    | 20      | Number of recommendations |

### Response `200 OK`

```json
{
  "type": "POSTS",
  "posts": [
    {
      "id": 10,
      "title": "Trending: Virtual Threads in Production",
      "score": 500,
      "commentCount": 200
    }
  ],
  "explanation": "Trending content across all communities.",
  "confidence": 0.90,
  "factors": [
    "High engagement",
    "Recent activity",
    "Community interest"
  ]
}
```

---

## Recommendation Algorithm

### Factors Considered

1. **User Interaction History**
   - Posts viewed, voted, commented on
   - Subreddit subscriptions and activity
   - Time spent on different content types

2. **Content Similarity**
   - Text similarity using embeddings
   - Topic modeling and classification
   - Tag and category matching

3. **Community Patterns**
   - Subreddit overlap analysis
   - User behavior in similar communities
   - Engagement patterns

4. **Trending Signals**
   - Recent popularity spikes
   - Viral content detection
   - Community growth metrics

5. **Collaborative Filtering**
   - User-to-user similarity
   - Item-to-item similarity
   - Matrix factorization

### Confidence Scoring

| Range | Interpretation |
|-------|----------------|
| 0.9+  | Very high confidence |
| 0.7-0.9| High confidence |
| 0.5-0.7| Medium confidence |
| 0.3-0.5| Low confidence |
| <0.3  | Very low confidence |

### AI Integration

- **Spring AI ChatClient** for natural language explanations
- **Embedding models** for content similarity
- **Structured concurrency** for parallel analysis
- **Caching** of recommendation results (15 minutes)

---

## Performance

- **Caching**: Recommendations cached for 15 minutes
- **Rate Limiting**: 30 requests per minute per user
- **Parallel Processing**: Structured concurrency for analysis
- **Fallback**: Simple popularity-based recommendations if AI fails

---

## Error Handling

| Status | Condition                      |
|--------|--------------------------------|
| 400    | Invalid recommendation request |
| 401    | Not authenticated              |
| 404    | User not found                 |
| 429    | Rate limit exceeded            |
| 503    | Recommendation service down    |

---

## Metrics

All recommendation endpoints emit these metrics:

- `recommendations.generated` - Recommendations generated
- `recommendations.posts` - Post recommendations
- `recommendations.subreddits` - Subreddit recommendations
- `recommendations.users` - User recommendations
- `recommendations.comments` - Comment recommendations
- `recommendations.contextual` - Contextual recommendations
- `recommendations.trending` - Trending recommendations
- `recommendations.error` - Recommendation generation failures
