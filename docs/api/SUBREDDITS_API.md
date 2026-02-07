# Sublambrks API

Base URL: `/api/sublambrks`

All endpoints require **JWT authentication** unless noted otherwise.

---

## POST `/api/sublambrks`

Create a new sublambrk. The creator automatically becomes a moderator and subscriber.

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
| 409    | Sublambrk name already exists |

---

## GET `/api/sublambrks/{sublambrkId}`

Get sublambrk details by ID.

### Path Parameters

| Param       | Type | Description  |
|-------------|------|--------------|
| sublambrkId | Long | Sublambrk ID |

### Response `200 OK`

Same shape as create response. `isUserSubscribed` and `isUserModerator` reflect the authenticated user's relationship.

---

## GET `/api/sublambrks/r/{name}`

Get sublambrk details by name (e.g. `/api/sublambrks/r/programming`).

### Path Parameters

| Param | Type   | Description    |
|-------|--------|----------------|
| name  | String | Sublambrk name |

---

## GET `/api/sublambrks/trending`

Get trending sublambrks sorted by active user count.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## GET `/api/sublambrks`

Get all public sublambrks sorted by subscriber count (descending).

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## GET `/api/sublambrks/search`

Search sublambrks by name, title, or description.

### Query Parameters

| Param | Type   | Default | Description       |
|-------|--------|---------|-------------------|
| query | String | —       | Search term (req) |
| page  | int    | 0       | Page number       |
| size  | int    | 20      | Page size         |

---

## PUT `/api/sublambrks/{sublambrkId}`

Update sublambrk settings. **Requires MODERATOR or ADMIN role.**

### Request Body

Same shape as create request (name is immutable — ignored on update).

### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| 403    | Not a moderator of this sub    |
| 404    | Sublambrk not found            |

---

## POST `/api/sublambrks/{sublambrkId}/subscribe`

Subscribe the authenticated user to a sublambrk.

### Response `200 OK`

Empty body. Increments `subscriberCount` and `memberCount`.

---

## POST `/api/sublambrks/{sublambrkId}/unsubscribe`

Unsubscribe the authenticated user from a sublambrk.

### Response `200 OK`

Empty body. Decrements `subscriberCount` and `memberCount`.

---

## GET `/api/sublambrks/user/subscriptions`

Get all sublambrks the authenticated user is subscribed to.

### Response `200 OK`

Set of `SublambrkResponse`.

---

## Caching Behaviour

| Endpoint                | Cache Name          | TTL    |
|-------------------------|---------------------|--------|
| GET /{sublambrkId}      | sublambrks          | 15 min |
| GET /trending           | trendingSublambrks  | 15 min |

Creating, updating, subscribing, or unsubscribing evicts sublambrk caches.
