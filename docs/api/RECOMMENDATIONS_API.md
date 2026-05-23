# Recommendations API

Base path: `/api/recommendations`. JWT required.

### POST `/api/recommendations`

Get recommendations.

**Auth:** User

**Request body**

```json
{"userId":1,"type":"POSTS","limit":20,"excludeCommunities":[],"excludeUsers":[],"includeNSFW":false,"includeOver18":false,"contextCommunityId":null,"contextPostId":null}
```

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/recommendations' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "userId": 1,
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
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
### GET `/api/recommendations/posts/{userId}`

Get recommended posts.

**Auth:** User

**Query/path parameters**

Optional `limit` plus endpoint-specific exclude/context flags.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/posts/1?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
### GET `/api/recommendations/communities/{userId}`

Get recommended communities.

**Auth:** User

**Query/path parameters**

Optional `limit` plus endpoint-specific exclude/context flags.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/communities/1?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
### GET `/api/recommendations/users/{userId}`

Get recommended users.

**Auth:** User

**Query/path parameters**

Optional `limit` plus endpoint-specific exclude/context flags.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/users/1?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
### GET `/api/recommendations/comments/{userId}`

Get recommended comments.

**Auth:** User

**Query/path parameters**

Optional `limit` plus endpoint-specific exclude/context flags.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/comments/1?limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
### GET `/api/recommendations/context/{userId}`

Get contextual recommendations.

**Auth:** User

**Query/path parameters**

Optional `contextCommunityId`, `contextPostId`, `type`, `limit`.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/context/1?type=posts&limit=20&contextPostId=10' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
### GET `/api/recommendations/trending`

Get trending recommendations.

**Auth:** User

**Query/path parameters**

Optional `type` default `posts`, `limit` default `20`.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/recommendations/trending?type=posts&limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"type":"POSTS","posts":[],"communities":[],"users":[],"comments":[],"explanation":"Recommended for you","confidence":0.85,"factors":["activity","subscriptions"]}
```
