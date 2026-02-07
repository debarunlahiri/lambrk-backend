# Users API

Base URL: `/api/users`

All endpoints require **JWT authentication** unless noted otherwise.

---

## GET `/api/users/{userId}`

Get a user's public profile by ID.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| userId | Long | User ID     |

### Response `200 OK`

```json
{
  "id": 2,
  "username": "john_doe",
  "displayName": "John Doe",
  "bio": "Software engineer who loves Spring Boot.",
  "avatarUrl": "https://cdn.example.com/avatars/john.png",
  "isActive": true,
  "isVerified": true,
  "karma": 1250,
  "createdAt": "2025-06-15T10:00:00Z",
  "updatedAt": "2026-02-07T14:00:00Z"
}
```

### Error Responses

| Status | Condition      |
|--------|----------------|
| 404    | User not found |

---

## GET `/api/users/username/{username}`

Get a user's public profile by username.

### Path Parameters

| Param    | Type   | Description |
|----------|--------|-------------|
| username | String | Username    |

### Response `200 OK`

Same shape as above.

---

## GET `/api/users/me`

Get the currently authenticated user's profile.

### Headers

```
Authorization: Bearer <access_token>
```

### Response `200 OK`

Same shape as above, for the token owner.

---

## GET `/api/users/top`

Get users with the highest karma.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

Paginated `UserResponse` list sorted by karma descending.

---

## GET `/api/users/search`

Search active users by username or display name.

### Query Parameters

| Param | Type   | Default | Description       |
|-------|--------|---------|-------------------|
| query | String | —       | Search term (req) |
| page  | int    | 0       | Page number       |
| size  | int    | 20      | Page size         |

### Response `200 OK`

Paginated `UserResponse` list.

---

## DELETE `/api/users/{userId}`

Delete a user account. **Requires ADMIN role.**

### Headers

```
Authorization: Bearer <access_token>
```

### Response `204 No Content`

### Error Responses

| Status | Condition      |
|--------|----------------|
| 403    | Not an admin   |
| 404    | User not found |

---

## User Response Schema

```json
{
  "id": "Long",
  "username": "String",
  "displayName": "String | null",
  "bio": "String | null",
  "avatarUrl": "String | null",
  "isActive": "boolean",
  "isVerified": "boolean",
  "karma": "int",
  "createdAt": "ISO-8601 Instant",
  "updatedAt": "ISO-8601 Instant"
}
```

### Notes

- `email` and `password` are **never** exposed in API responses
- `karma` is updated automatically when the user's posts/comments are voted on
- `isVerified` is set by admin actions (not self-service)
