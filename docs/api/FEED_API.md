# Feed API

Base path: `/api/feed`. Public — works without auth (trending content). JWT optional for personalized ranking.

---

### GET `/api/feed`

Get personalized feed.

**Auth:** User role

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | No | — | `Bearer <jwt>` (optional) |
| `limit` | Query | integer | No | `20` | Number of posts |
| `sortBy` | Query | string | No | `algorithm` | `algorithm`, `hot`, `new`, `top` |
| `includeNsfw` | Query | boolean | No | `false` | Include NSFW content |
| `fromFollowingOnly` | Query | boolean | No | `false` | Only followed communities |
| `timeDecayFactor` | Query | number | No | `1.0` | Ranking freshness factor |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FeedResponse` | Personalized feed |
| `401` | error | JWT invalid (if provided) |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/feed?limit=20&sortBy=algorithm&includeNsfw=false' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "suggestedUsers": [],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "score",
      "freshness"
    ],
    "processingTimeMs": 10
  },
  "totalAvailable": 0,
  "hasMore": false
}
```

---

### POST `/api/feed`

Get feed with advanced filters.

**Auth:** User role

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `userId` | Body | UUID | No | — | Target user UUID |
| `limit` | Body | integer | No | `20` | Number of posts |
| `sortBy` | Body | string | No | `algorithm` | Sort method |
| `postTypes` | Body | array | No | `[]` | Filter by post types |
| `includeNsfw` | Body | boolean | No | `false` | Include NSFW |
| `includeFromFollowingOnly` | Body | boolean | No | `false` | Only followed |
| `timeDecayFactor` | Body | number | No | `1.0` | Freshness factor |

**Request body**

```json
{
  "userId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "limit": 20,
  "sortBy": "algorithm",
  "postTypes": [
    "TEXT"
  ],
  "includeNsfw": false,
  "includeFromFollowingOnly": false,
  "timeDecayFactor": 1.0
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FeedResponse` | Filtered feed |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/feed' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "userId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "limit": 20,
  "sortBy": "algorithm",
  "postTypes": [
    "TEXT"
  ],
  "includeNsfw": false,
  "includeFromFollowingOnly": false,
  "timeDecayFactor": 1
}'
```

**Response**

```json
{
  "posts": [],
  "suggestedUsers": [],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "score",
      "freshness"
    ],
    "processingTimeMs": 10
  },
  "totalAvailable": 0,
  "hasMore": false
}
```

---

### GET `/api/feed/hot`

Get hot feed.

**Auth:** User role

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | No | — | `Bearer <jwt>` (optional) |
| `limit` | Query | integer | No | `20` | Number of posts |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FeedResponse` | Hot feed |
| `401` | error | JWT invalid (if provided) |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/feed/hot?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "suggestedUsers": [],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "score",
      "freshness"
    ],
    "processingTimeMs": 10
  },
  "totalAvailable": 0,
  "hasMore": false
}
```

---

### GET `/api/feed/new`

Get newest feed.

**Auth:** User role

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | No | — | `Bearer <jwt>` (optional) |
| `limit` | Query | integer | No | `20` | Number of posts |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FeedResponse` | Newest feed |
| `401` | error | JWT invalid (if provided) |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/feed/new?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "suggestedUsers": [],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "score",
      "freshness"
    ],
    "processingTimeMs": 10
  },
  "totalAvailable": 0,
  "hasMore": false
}
```

---

### GET `/api/feed/top`

Get top feed.

**Auth:** User role

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | No | — | `Bearer <jwt>` (optional) |
| `limit` | Query | integer | No | `20` | Number of posts |
| `timePeriod` | Query | string | No | `all` | Time filter |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FeedResponse` | Top feed |
| `401` | error | JWT invalid (if provided) |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/feed/top?limit=20&timePeriod=all' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "suggestedUsers": [],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "score",
      "freshness"
    ],
    "processingTimeMs": 10
  },
  "totalAvailable": 0,
  "hasMore": false
}
```

---

### GET `/api/feed/discover`

Get discovery feed.

**Auth:** User role

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | No | — | `Bearer <jwt>` (optional) |
| `limit` | Query | integer | No | `20` | Number of posts |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FeedResponse` | Discovery feed |
| `401` | error | JWT invalid (if provided) |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/feed/discover?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "suggestedUsers": [],
  "algorithmInfo": {
    "sortMethod": "algorithm",
    "timeDecayFactor": 1.0,
    "freshnessHours": 24,
    "factorsConsidered": [
      "score",
      "freshness"
    ],
    "processingTimeMs": 10
  },
  "totalAvailable": 0,
  "hasMore": false
}
```
