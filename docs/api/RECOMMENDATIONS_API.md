# Recommendations API

Base path: `/api/recommendations`. JWT required.

---

### POST `/api/recommendations`

Get recommendations.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Body | UUID | No | — | Target user UUID |
| `type` | Body | string | No | `POSTS` | `POSTS`, `COMMUNITIES`, `USERS`, `COMMENTS` |
| `limit` | Body | integer | No | `20` | Number of results |
| `excludeCommunities` | Body | array | No | `[]` | Community UUIDs to exclude |
| `excludeUsers` | Body | array | No | `[]` | User UUIDs to exclude |
| `includeNSFW` | Body | boolean | No | `false` | Include NSFW |
| `includeOver18` | Body | boolean | No | `false` | Include Over18 |
| `contextCommunityId` | Body | UUID | No | `null` | Context community |
| `contextPostId` | Body | UUID | No | `null` | Context post |

**Request body**

```json
{
  "userId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "type": "POSTS",
  "limit": 20,
  "excludeCommunities": [],
  "excludeUsers": [],
  "includeNSFW": false,
  "includeOver18": false,
  "contextCommunityId": null,
  "contextPostId": null
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Recommendations with explanation |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/recommendations' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "userId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "type": "POSTS",
  "limit": 20,
  "excludeCommunities": [],
  "excludeUsers": [],
  "includeNSFW": false,
  "includeOver18": false,
  "contextCommunityId": null,
  "contextPostId": null
}'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```

---

### GET `/api/recommendations/posts/{userId}`

Get recommended posts.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | — | Target user UUID |
| `limit` | Query | integer | No | `20` | Number of results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Recommended posts |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/posts/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```

---

### GET `/api/recommendations/communities/{userId}`

Get recommended communities.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | — | Target user UUID |
| `limit` | Query | integer | No | `20` | Number of results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Recommended communities |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/communities/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```

---

### GET `/api/recommendations/users/{userId}`

Get recommended users.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | — | Target user UUID |
| `limit` | Query | integer | No | `20` | Number of results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Recommended users |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/users/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```

---

### GET `/api/recommendations/comments/{userId}`

Get recommended comments.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | — | Target user UUID |
| `limit` | Query | integer | No | `20` | Number of results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Recommended comments |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/comments/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```

---

### GET `/api/recommendations/context/{userId}`

Get contextual recommendations.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | — | Target user UUID |
| `contextCommunityId` | Query | UUID | No | — | Context community |
| `contextPostId` | Query | UUID | No | — | Context post |
| `type` | Query | string | No | `posts` | Recommendation type |
| `limit` | Query | integer | No | `20` | Number of results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Contextual recommendations |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/context/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11?type=posts&limit=20&contextPostId=10' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```

---

### GET `/api/recommendations/trending`

Get trending recommendations.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `type` | Query | string | No | `posts` | Recommendation type |
| `limit` | Query | integer | No | `20` | Number of results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `RecommendationResponse` | Trending recommendations |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/trending?type=posts&limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "type": "POSTS",
  "posts": [],
  "communities": [],
  "users": [],
  "comments": [],
  "explanation": "Recommended for you",
  "confidence": 0.85,
  "factors": [
    "activity",
    "subscriptions"
  ]
}
```
