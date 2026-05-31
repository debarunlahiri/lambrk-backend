# Feed API

Base path: `/api/feed`. JWT required with `USER` role.

### GET `/api/feed`

Get personalized feed.

**Auth:** User role

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `limit` | integer | no | `20` | Number of posts. |
| `sortBy` | string | no | `algorithm` | `algorithm`, `hot`, `new`, `top`. |
| `includeNsfw` | boolean | no | `false` | Include NSFW content. |
| `fromFollowingOnly` | boolean | no | `false` | Only followed communities. |
| `timeDecayFactor` | number | no | `1.0` | Ranking freshness factor. |

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
### POST `/api/feed`

Get feed with advanced filters.

**Auth:** User role

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
### GET `/api/feed/hot`

Get hot feed.

**Auth:** User role

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `limit` | integer | no | `20` | Number of posts. |

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
### GET `/api/feed/new`

Get newest feed.

**Auth:** User role

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `limit` | integer | no | `20` | Number of posts. |

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
### GET `/api/feed/top`

Get top feed.

**Auth:** User role

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `limit` | integer | no | `20` | Number of posts. |
| `timePeriod` | string | no | `all` | Accepted by controller. |

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
### GET `/api/feed/discover`

Get discovery feed.

**Auth:** User role

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `limit` | integer | no | `20` | Number of posts. |

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
