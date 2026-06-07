# Search API

Base path: `/api/search`. JWT required.

---

### POST `/api/search`

Advanced search.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Body | string | **Yes** | — | Search text |
| `type` | Body | string | No | `ALL` | `ALL`, `POSTS`, `COMMENTS`, `USERS`, `COMMUNITIES` |
| `sort` | Body | string | No | `RELEVANCE` | Sort order |
| `timeFilter` | Body | string | No | `ALL` | Time filter |
| `communities` | Body | array | No | `[]` | Filter by community UUIDs |
| `flairs` | Body | array | No | `[]` | Filter by flair names |
| `includeNSFW` | Body | boolean | No | `false` | Include NSFW |
| `includeOver18` | Body | boolean | No | `false` | Include Over18 |
| `minScore` | Body | integer | No | `null` | Minimum score |
| `minComments` | Body | integer | No | `null` | Minimum comments |
| `minVotes` | Body | integer | No | `null` | Minimum votes |
| `page` | Body | integer | No | `0` | Page number |
| `size` | Body | integer | No | `20` | Page size |

**Request body**

```json
{
  "query": "spring boot",
  "type": "ALL",
  "sort": "RELEVANCE",
  "timeFilter": "ALL",
  "communities": [],
  "flairs": [],
  "includeNSFW": false,
  "includeOver18": false,
  "minScore": null,
  "minComments": null,
  "minVotes": null,
  "page": 0,
  "size": 20
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | Unified search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/search' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "query": "spring boot",
  "type": "ALL",
  "sort": "RELEVANCE",
  "timeFilter": "ALL",
  "communities": [],
  "flairs": [],
  "includeNSFW": false,
  "includeOver18": false,
  "minScore": null,
  "minComments": null,
  "minVotes": null,
  "page": 0,
  "size": 20
}'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```

---

### GET `/api/search/posts`

Search posts.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Search text |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |
| `sort` | Query | string | No | `RELEVANCE` | Sort order |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | Post search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/posts?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```

---

### GET `/api/search/comments`

Search comments.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Search text |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |
| `sort` | Query | string | No | `RELEVANCE` | Sort order |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | Comment search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/comments?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```

---

### GET `/api/search/users`

Search users.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Search text |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |
| `sort` | Query | string | No | `RELEVANCE` | Sort order |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | User search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/users?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```

---

### GET `/api/search/communities`

Search communities.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Search text |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |
| `sort` | Query | string | No | `RELEVANCE` | Sort order |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | Community search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/communities?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```

---

### GET `/api/search/all`

Search all.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Search text |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |
| `sort` | Query | string | No | `RELEVANCE` | Sort order |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | Unified search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/all?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```

---

### GET `/api/search/suggestions`

Get search suggestions.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Prefix/query |
| `type` | Query | string | No | `posts` | `posts`, `communities`, `users` |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `List<string>` | Suggestion strings |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/suggestions?query=spring&type=posts' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[
  "spring tutorial",
  "spring guide",
  "spring examples",
  "spring best practices"
]
```

---

### GET `/api/search/trending`

Get trending search response.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SearchResponse` | Trending results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/trending?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "posts": [],
  "comments": [],
  "users": [],
  "communities": [],
  "metadata": {
    "query": "spring boot",
    "type": "ALL",
    "sort": "RELEVANCE",
    "timeFilter": "ALL",
    "totalResults": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "totalPages": 0,
    "searchTimeMs": 12,
    "suggestions": []
  }
}
```
