# Search API

Base path: `/api/search`. JWT required.

### POST `/api/search`

Advanced search.

**Auth:** User

**Request body**

```json
{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","communities":[],"flairs":[],"includeNSFW":false,"includeOver18":false,"minScore":null,"minComments":null,"minVotes":null,"page":0,"size":20}
```

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
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
### GET `/api/search/posts`

Search posts.

**Auth:** User

**Query/path parameters**

Required `query`; optional `page`, `size`, `sort`, and type-specific filters documented in the controller.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/posts?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
### GET `/api/search/comments`

Search comments.

**Auth:** User

**Query/path parameters**

Required `query`; optional `page`, `size`, `sort`, and type-specific filters documented in the controller.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/comments?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
### GET `/api/search/users`

Search users.

**Auth:** User

**Query/path parameters**

Required `query`; optional `page`, `size`, `sort`, and type-specific filters documented in the controller.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/users?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
### GET `/api/search/communities`

Search communities.

**Auth:** User

**Query/path parameters**

Required `query`; optional `page`, `size`, `sort`, and type-specific filters documented in the controller.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/communities?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
### GET `/api/search/all`

Search all.

**Auth:** User

**Query/path parameters**

Required `query`; optional `page`, `size`, `sort`, and type-specific filters documented in the controller.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/all?query=spring&page=0&size=20&sort=RELEVANCE' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
### GET `/api/search/suggestions`

Get search suggestions.

**Auth:** User

**Query/path parameters**

| `query` | string | yes | - | Prefix/query. |
| `type` | string | no | `posts` | `posts`, `communities`, `users`. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/suggestions?query=spring&type=posts' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
["spring tutorial","spring guide","spring examples","spring best practices"]
```
### GET `/api/search/trending`

Get trending search response.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/search/trending?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"posts":[],"comments":[],"users":[],"communities":[],"metadata":{"query":"spring boot","type":"ALL","sort":"RELEVANCE","timeFilter":"ALL","totalResults":0,"pageNumber":0,"pageSize":20,"totalPages":0,"searchTimeMs":12,"suggestions":[]}}
```
