# Subreddits API

Base URL: `/api/subreddits`

All endpoints require **JWT authentication** unless noted otherwise.

---

## POST `/api/subreddits`

Create a new subreddit. The creator automatically becomes a moderator and subscriber.

### Headers

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "name": "springboot",
  "title": "Spring Boot Development",
  "description": "Everything about Spring Boot framework",
  "sidebarText": "Rules: Be respectful, stay on topic.",
  "isPublic": true,
  "isRestricted": false,
  "isOver18": false
}
```

### Validation Rules

| Field        | Rule                                                        |
|--------------|-------------------------------------------------------------|
| name         | Required, 3–21 chars, alphanumeric + underscores only       |
| title        | Required, max 100 chars                                     |
| description  | Optional                                                    |
| sidebarText  | Optional                                                    |
| isPublic     | Boolean (default true)                                      |
| isRestricted | Boolean (default false)                                     |
| isOver18     | Boolean (default false)                                     |

### Response `200 OK`

```json
{
  "id": 4,
  "name": "springboot",
  "title": "Spring Boot Development",
  "description": "Everything about Spring Boot framework",
  "sidebarText": "Rules: Be respectful, stay on topic.",
  "headerImageUrl": null,
  "iconImageUrl": null,
  "isPublic": true,
  "isRestricted": false,
  "isOver18": false,
  "memberCount": 1,
  "subscriberCount": 1,
  "activeUserCount": 0,
  "createdBy": {
    "id": 1,
    "username": "john_doe"
  },
  "createdAt": "2026-02-07T14:30:00Z",
  "updatedAt": "2026-02-07T14:30:00Z",
  "isUserSubscribed": true,
  "isUserModerator": true
}
```

### Error Responses

| Status | Condition                     |
|--------|-------------------------------|
| 400    | Validation failed             |
| 401    | Not authenticated             |
| 409    | Subreddit name already exists |

---

## GET `/api/subreddits/{subredditId}`

Get subreddit details by ID.

### Path Parameters

| Param       | Type | Description  |
|-------------|------|--------------|
| subredditId | Long | Subreddit ID |

### Response `200 OK`

Same shape as create response. `isUserSubscribed` and `isUserModerator` reflect the authenticated user's relationship.

---

## GET `/api/subreddits/r/{name}`

Get subreddit details by name (e.g. `/api/subreddits/r/programming`).

### Path Parameters

| Param | Type   | Description    |
|-------|--------|----------------|
| name  | String | Subreddit name |

---

## GET `/api/subreddits/trending`

Get trending subreddits sorted by active user count.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## GET `/api/subreddits`

Get all public subreddits sorted by subscriber count (descending).

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## GET `/api/subreddits/search`

Search subreddits by name, title, or description.

### Query Parameters

| Param | Type   | Default | Description       |
|-------|--------|---------|-------------------|
| query | String | —       | Search term (req) |
| page  | int    | 0       | Page number       |
| size  | int    | 20      | Page size         |

---

## PUT `/api/subreddits/{subredditId}`

Update subreddit settings. **Requires MODERATOR or ADMIN role.**

### Request Body

Same shape as create request (name is immutable — ignored on update).

### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| 403    | Not a moderator of this sub    |
| 404    | Subreddit not found            |

---

## POST `/api/subreddits/{subredditId}/subscribe`

Subscribe the authenticated user to a subreddit.

### Response `200 OK`

Empty body. Increments `subscriberCount` and `memberCount`.

---

## POST `/api/subreddits/{subredditId}/unsubscribe`

Unsubscribe the authenticated user from a subreddit.

### Response `200 OK`

Empty body. Decrements `subscriberCount` and `memberCount`.

---

## GET `/api/subreddits/user/subscriptions`

Get all subreddits the authenticated user is subscribed to.

### Response `200 OK`

Set of `SubredditResponse`.

---

## Caching Behaviour

| Endpoint                | Cache Name          | TTL    |
|-------------------------|---------------------|--------|
| GET /{subredditId}      | subreddits          | 15 min |
| GET /trending           | trendingSubreddits  | 15 min |

Creating, updating, subscribing, or unsubscribing evicts subreddit caches.
