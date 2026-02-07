# Search API

Base URL: `/api/search`

All endpoints require **JWT authentication** unless noted otherwise.

---

## POST `/api/search`

Advanced search across all content types with complex filtering and sorting options.

### Request Body

```json
{
  "query": "spring boot virtual threads",
  "type": "ALL",
  "sort": "RELEVANCE",
  "timeFilter": "WEEK",
  "subreddits": ["programming", "java"],
  "flairs": ["tutorial", "guide"],
  "includeNSFW": false,
  "includeOver18": false,
  "minScore": 10,
  "minComments": 5,
  "minVotes": 3,
  "page": 0,
  "size": 20
}
```

### Validation Rules

| Field        | Rule                                      |
|--------------|-------------------------------------------|
| query        | Required, 2–100 chars                      |
| type         | `ALL`, `POSTS`, `COMMENTS`, `USERS`, `SUBREDDITS` |
| sort         | `RELEVANCE`, `NEW`, `HOT`, `TOP`, `CONTROVERSIAL` |
| timeFilter   | `ALL`, `HOUR`, `DAY`, `WEEK`, `MONTH`, `YEAR` |
| subreddits   | Optional, list of subreddit names          |
| flairs       | Optional, list of flair texts                 |
| minScore     | Optional, minimum score filter               |
| minComments  | Optional, minimum comment count filter       |
| minVotes     | Optional, minimum vote count filter           |
| page         | Default 0, pagination page number             |
| size         | Default 20, pagination page size             |

### Response `200 OK`

```json
{
  "posts": [
    {
      "id": 1,
      "title": "Spring Boot 3.5 with Virtual Threads",
      "content": "Exploring the new virtual thread features...",
      "author": { "id": 1, "username": "john_doe" },
      "subreddit": { "id": 1, "name": "programming" },
      "score": 25,
      "commentCount": 8,
      "createdAt": "2026-02-07T14:00:00Z"
    }
  ],
  "comments": [],
  "users": [],
  "subreddits": [],
  "metadata": {
    "query": "spring boot virtual threads",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "WEEK",
    "totalResults": 1,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 1,
    "searchTimeMs": 45,
    "suggestions": ["spring boot tutorial", "spring boot guide"]
  }
}
```

---

## GET `/api/search/posts`

Search posts only with filtering options.

### Query Parameters

| Param        | Type   | Default | Description       |
|--------------|--------|---------|-------------------|
| query        | String | —       | Search term (req) |
| page         | int    | 0       | Page number       |
| size         | int    | 20      | Page size         |
| sort         | String | RELEVANCE| Sort method       |
| timeFilter   | String | ALL      | Time filter       |
| subreddits   | String | —       | Comma-separated |
| flairs       | String | —       | Comma-separated |
| includeNSFW  | bool   | false    | Include NSFW     |
| includeOver18 | bool   | false    | Include 18+      |
| minScore     | int    | —       | Minimum score    |
| minComments  | int    | —       | Min comment count |

### Response `200 OK`

Same shape as POST endpoint with only posts populated.

---

## GET `/api/search/comments`

Search comments only.

### Query Parameters

| Param        | Type   | Default | Description       |
|--------------|--------|---------|-------------------|
| query        | String | —       | Search term (req) |
| page         | int    | 0       | Page number       |
| size         | int    | 20      | Page size         |
| sort         | String | RELEVANCE| Sort method       |
| timeFilter   | String | ALL      | Time filter       |
| includeNSFW  | bool   | false    | Include NSFW     |
| includeOver18 | bool   | false    | Include 18+      |
| minScore     | int    | —       | Minimum score    |

---

## GET `/api/search/users`

Search users by username or display name.

### Query Parameters

| Param        | Type   | Default | Description       |
|--------------|--------|---------|-------------------|
| query        | String | —       | Search term (req) |
| page         | int    | 0       | Page number       |
| size         | int    | 20      | Page size         |
| sort         | String | RELEVANCE| Sort method       |
| minScore     | int    | —       | Minimum karma    |

---

## GET `/api/search/subreddits`

Search subreddits by name, title, or description.

### Query Parameters

| Param        | Type   | Default | Description       |
|--------------|--------|---------|-------------------|
| query        | String | —       | Search term (req) |
| page         | int    | 0       | Page number       |
| size         | int    | 20      | Page size         |
| sort         | String | RELEVANCE| Sort method       |
| includeNSFW  | bool   | false    | Include NSFW     |
| includeOver18 | bool   | false    | Include 18+      |

---

## GET `/api/search/all`

Search all content types (equivalent to POST with defaults).

### Query Parameters

Same as POST endpoint with defaults:
- `type=ALL`
- `sort=RELEVANCE`
- `timeFilter=ALL`
- `page=0`
- `size=20`

---

## GET `/api/search/suggestions`

Get search suggestions for autocomplete.

### Query Parameters

| Param | Type   | Default | Description          |
|-------|--------|---------|----------------------|
| query | String | —       | Search term (req) |
| type  | String | posts    | Suggestion type |

### Response `200 OK`

```json
[
  "spring boot tutorial",
  "spring boot guide",
  "spring boot examples",
  "spring boot best practices"
]
```

---

## GET `/api/search/trending`

Get trending search terms.

### Query Parameters

| Param | Type   | Default | Description |
|-------|--------|---------|-------------|
| page  | int    | 0       | Page number  |
| size  | int    | 20      | Page size    |

### Response `200 OK`

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "subreddits": [],
  "metadata": {
    "query": "trending",
    "type": "ALL",
    "sort": "HOT",
    "timeFilter": "WEEK",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 0,
    "suggestions": ["spring boot", "java 25", "virtual threads", "kubernetes"]
  }
}
```

---

## Search Features

### Advanced Filtering

- **Time-based filtering**: Search content within specific time windows
- **Subreddit filtering**: Limit search to specific communities
- **Flair filtering**: Filter by post/comment flair text
- **Score filtering**: Minimum score/vote/comment thresholds
- **Content filtering**: NSFW and 18+ content controls

### Sorting Options

| Sort Method | Description |
|-------------|-------------|
| RELEVANCE   | AI-powered relevance scoring |
| NEW         | Most recent first |
| HOT         | High engagement (score + comments) |
| TOP         | Highest score |
| CONTROVERSIAL | High disagreement (upvotes vs downvotes) |

### Performance

- **Caching**: Search results cached for 5 minutes
- **Rate Limiting**: 50 requests per minute
- **Parallel Processing**: Structured concurrency for multi-type searches
- **Suggestions**: Auto-generated search suggestions

### Error Handling

| Status | Condition                          |
|--------|------------------------------------|
| 400    | Invalid search parameters           |
| 401    | Not authenticated                  |
| 429    | Rate limit exceeded                  |
| 503    | Search service unavailable          |

### Metrics

All search endpoints emit these metrics:

- `search.advanced` - Advanced search requests
- `search.posts` - Post search requests  
- `search.comments` - Comment search requests
- `search.users` - User search requests
- `search.subreddits` - Subreddit search requests
- `search.suggestions` - Suggestion requests
- `search.trending` - Trending search requests
