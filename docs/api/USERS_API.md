# Users API

Base path: `/api/users`. JWT required unless noted.

---

## Response Types

### `SocialUserResponse`

Used by public profile and social-list endpoints.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | User id |
| `username` | string | Public username |
| `displayName` | string | Public display name |
| `bio` | string | Public bio |
| `avatarUrl` | string | CDN-resolved avatar URL |
| `headerImageUrl` | string | CDN-resolved profile header URL |
| `location` | string | Public location |
| `website` | string | Public website |
| `isVerified` | boolean | Verification status |
| `karma` | integer | Karma from other users' votes |
| `followerCount` | integer | Active followers |
| `followingCount` | integer | Active following count |
| `friendCount` | integer | Accepted friend count |
| `followedByCurrentUser` | boolean | True when authenticated viewer follows this user |
| `followingCurrentUser` | boolean | True when this user follows authenticated viewer |
| `friend` | boolean | True when authenticated viewer is an accepted friend |
| `friendshipStatus` | string | Current friendship status, or `null` |
| `privateAccount` | boolean | True when profile social details are limited to owner/friends |
| `canViewFollowerCount` | boolean | True when `followerCount` is visible to this viewer |
| `canViewFollowingCount` | boolean | True when `followingCount` is visible to this viewer |
| `canViewFollowerList` | boolean | True when the followers endpoint should show data to this viewer |
| `canViewFollowingList` | boolean | True when the following endpoint should show data to this viewer |
| `canShowAddFriendButton` | boolean | True when UI should show the add-friend button |
| `canShowFollowButton` | boolean | True when UI should show the follow button |
| `canShowInMutualLists` | boolean | True when this user may appear in mutual follower/following/friend lists |
| `messageButtonEnabled` | boolean | True when UI should show/enable the message button |
| `createdAt` | timestamp | User creation timestamp |

### `UserPrivacySettingsResponse`

Used by `/api/users/me/privacy`.

| Field | Type | Description |
|-------|------|-------------|
| `userId` | UUID | User id |
| `privateAccount` | boolean | Limit profile social details to owner/friends |
| `hideFollowerCount` | boolean | Hide follower count from other viewers |
| `hideFollowingCount` | boolean | Hide following count from other viewers |
| `hideFollowerList` | boolean | Hide followers list from other viewers |
| `hideFollowingList` | boolean | Hide following list from other viewers |
| `hideAddFriendButton` | boolean | Hide and disable friend requests to this user |
| `hideFollowButton` | boolean | Hide and disable follow actions to this user |
| `hideFromMutualList` | boolean | Exclude this user from mutual-list endpoints |
| `messageButtonEnabled` | boolean | Enable or disable the profile message button |
| `updatedAt` | timestamp | Last settings update timestamp |

### `FriendRequestResponse`

Used by friend request actions and request-list endpoints.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Friendship row id |
| `requester` | `UserResponse` | User who sent the request |
| `addressee` | `UserResponse` | User who should respond |
| `status` | string | `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `REMOVED`, or `BLOCKED` |
| `source` | string | Optional source such as `profile` or `search` |
| `requestMessage` | string | Optional message, max 280 chars |
| `createdAt` | timestamp | Request creation timestamp |
| `respondedAt` | timestamp | Response/cancel timestamp, or `null` |

---

### GET `/api/users/{userId}`

Get a user by id.

**Auth:** Public

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `userId` | Path | UUID | **Yes** | UUID of the user |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SocialUserResponse` | User profile details with social counts |
| `404` | error | User not found |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "username": "johndoe",
  "displayName": "John Doe",
  "bio": "Builder",
  "avatarUrl": "https://example.com/avatar.png",
  "headerImageUrl": "https://example.com/header.png",
  "location": "Bangalore",
  "website": "https://johndoe.dev",
  "isVerified": false,
  "karma": 42,
  "followerCount": 120,
  "followingCount": 64,
  "friendCount": 18,
  "followedByCurrentUser": false,
  "followingCurrentUser": false,
  "friend": false,
  "friendshipStatus": null,
  "privateAccount": false,
  "canViewFollowerCount": true,
  "canViewFollowingCount": true,
  "canViewFollowerList": true,
  "canViewFollowingList": true,
  "canShowAddFriendButton": true,
  "canShowFollowButton": true,
  "canShowInMutualLists": true,
  "messageButtonEnabled": true,
  "createdAt": "2026-05-02T10:00:00Z"
}
```

---

### PUT `/api/users/me`

Update current user's profile. All fields are optional — only provided fields are updated.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `displayName` | Body | string | No | Public display name |
| `bio` | Body | string | No | Short bio text |
| `location` | Body | string | No | User location |
| `website` | Body | string | No | Personal website URL |
| `avatarUrl` | Body | string | No | Profile image URL |
| `headerImageUrl` | Body | string | No | Cover/banner image URL |

**Request body**

```json
{
  "displayName": "Debarun",
  "bio": "Full-stack developer",
  "location": "Kolkata",
  "website": "https://debarun.dev",
  "avatarUrl": "https://s3.../profile_img/.../main/....jpg",
  "headerImageUrl": "https://s3.../cover_img/.../main/....jpg"
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `UserResponse` | Updated profile |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/users/me' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "displayName": "Debarun",
  "bio": "Full-stack developer",
  "location": "Kolkata",
  "website": "https://debarun.dev",
  "avatarUrl": "https://s3.../profile_img/.../main/....jpg",
  "headerImageUrl": "https://s3.../cover_img/.../main/....jpg"
}'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "username": "debarunlahiri",
  "displayName": "Debarun",
  "bio": "Full-stack developer",
  "avatarUrl": "https://s3.../profile_img/.../main/....jpg",
  "headerImageUrl": "https://s3.../cover_img/.../main/....jpg",
  "location": "Kolkata",
  "website": "https://debarun.dev",
  "isActive": true,
  "isVerified": false,
  "karma": 0,
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z"
}
```

---

### GET `/api/users/me/privacy`

Get the authenticated user's account privacy settings.

**Auth:** User

**Response:** `UserPrivacySettingsResponse`

```bash
curl -X GET 'http://localhost:9500/api/users/me/privacy' \
  -H 'Authorization: Bearer <token>'
```

---

### PUT `/api/users/me/privacy`

Update the authenticated user's account privacy settings. If `privateAccount` is provided, it acts as a bulk preset: `true` enables all privacy restrictions and disables the message button; `false` disables all privacy restrictions and enables the message button. Individual fields are applied only when `privateAccount` is omitted.

**Auth:** User

**Request body**

```json
{
  "privateAccount": true
}
```

**Response:** `UserPrivacySettingsResponse`

```bash
curl -X PUT 'http://localhost:9500/api/users/me/privacy' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "privateAccount": true
}'
```

---

### GET `/api/users/username/{username}`

Get a user by username.

**Auth:** Public

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `username` | Path | string | **Yes** | Username to lookup |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `SocialUserResponse` | User profile details with social counts |
| `404` | error | User not found |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/username/johndoe'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "username": "johndoe",
  "displayName": "John Doe",
  "bio": "Builder",
  "avatarUrl": "https://example.com/avatar.png",
  "headerImageUrl": "https://example.com/header.png",
  "location": "Bangalore",
  "website": "https://johndoe.dev",
  "isVerified": false,
  "karma": 42,
  "followerCount": 120,
  "followingCount": 64,
  "friendCount": 18,
  "followedByCurrentUser": false,
  "followingCurrentUser": false,
  "friend": false,
  "friendshipStatus": null,
  "createdAt": "2026-05-02T10:00:00Z"
}
```

---

### GET `/api/users/me`

Get the authenticated user.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `UserResponse` | Current user profile |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/me' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "username": "johndoe",
  "displayName": "John Doe",
  "bio": "Builder",
  "avatarUrl": "https://example.com/avatar.png",
  "isActive": true,
  "isVerified": false,
  "karma": 42,
  "createdAt": "2026-05-02T10:00:00Z",
  "updatedAt": "2026-05-02T10:00:00Z"
}
```

---

### GET `/api/users/top`

List top users by karma.

**Auth:** Public

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `page` | Query | integer | No | `0` | Zero-based page index |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<UserResponse>` | Paginated list of users |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/top?page=0&size=20'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### GET `/api/users/search`

Search active users.

**Auth:** Public

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `query` | Query | string | **Yes** | — | Search text |
| `page` | Query | integer | No | `0` | Zero-based page index |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<UserResponse>` | Paginated search results |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/users/search?query=john&page=0&size=20'
```

**Response**

```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "empty": true
}
```

---

### DELETE `/api/users/{userId}`

Delete a user.

**Auth:** Admin

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | UUID of the user to delete |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | User deleted |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/users/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`

---

## Social Graph Data Model

### `user_follows`

Directional follow relationship. One row exists per follower/following pair and is reused when a user unfollows/refollows.

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Follow row id |
| `follower_id` | UUID | User who follows |
| `following_id` | UUID | User being followed |
| `status` | string | `ACTIVE` or `REMOVED` |
| `notification_enabled` | boolean | Whether follower wants notifications |
| `source` | string | Optional source such as `profile`, `search`, `suggested` |
| `last_interaction_at` | timestamp | Last follow/unfollow interaction |
| `created_at` | timestamp | First follow row creation time |
| `updated_at` | timestamp | Last row update time |
| `removed_at` | timestamp | When the follow was removed |

Constraints/indexes include no self-follow, unique `(follower_id, following_id)`, follower/following indexes, status index, and partial active-list indexes.

### `user_friendships`

Undirected friendship relationship with request state and audit fields.

| Column | Type | Description |
|--------|------|-------------|
| `id` | UUID | Friendship row id |
| `user_one_id` | UUID | Canonical first user in the pair |
| `user_two_id` | UUID | Canonical second user in the pair |
| `requester_id` | UUID | User who sent the current/latest request |
| `addressee_id` | UUID | User who should respond to the current request |
| `last_action_user_id` | UUID | User who last changed the friendship state |
| `status` | string | `PENDING`, `ACCEPTED`, `DECLINED`, `CANCELLED`, `REMOVED`, `BLOCKED` |
| `source` | string | Optional source such as `profile`, `search`, `suggested` |
| `request_message` | string | Optional request message, max 280 chars |
| `accepted_at` | timestamp | When the request was accepted |
| `responded_at` | timestamp | When request was accepted/declined/cancelled |
| `removed_at` | timestamp | When an accepted friendship was removed |
| `blocked_at` | timestamp | Reserved for blocking workflows |
| `created_at` | timestamp | First friendship row creation time |
| `updated_at` | timestamp | Last row update time |

Constraints/indexes include no self-friendship, canonical unique `(user_one_id, user_two_id)`, requester/addressee indexes, status index, pending-request indexes, and accepted-friend indexes.

### User Privacy Columns

Privacy settings are stored on `users` and default to a public account.

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `private_account` | boolean | `false` | Limits social details to owner and accepted friends |
| `hide_follower_count` | boolean | `false` | Masks follower count for other viewers |
| `hide_following_count` | boolean | `false` | Masks following count for other viewers |
| `hide_follower_list` | boolean | `false` | Returns an empty followers page for other viewers |
| `hide_following_list` | boolean | `false` | Returns an empty following page for other viewers |
| `hide_add_friend_button` | boolean | `false` | Hides add-friend UI and rejects friend requests |
| `hide_follow_button` | boolean | `false` | Hides follow UI and rejects follow actions |
| `hide_from_mutual_list` | boolean | `false` | Filters the user out of mutual-list endpoints |
| `message_button_enabled` | boolean | `true` | Controls whether profile messaging should be enabled |

---

### POST `/api/users/{userId}/follow`

Follow a user. Refollowing reactivates the existing follow row.

**Auth:** User

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `userId` | Path | UUID | **Yes** | User to follow |
| `source` | Query | string | No | Optional follow source |

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | Followed |
| `401` | error | JWT missing or invalid |
| `403` | error | Target user disabled follow actions |
| `404` | error | User not found |

```bash
curl -X POST 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/follow?source=profile' \
  -H 'Authorization: Bearer <token>'
```

---

### DELETE `/api/users/{userId}/follow`

Unfollow a user.

**Auth:** User

```bash
curl -X DELETE 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/follow' \
  -H 'Authorization: Bearer <token>'
```

**Response:** `204 No Content`

---

### GET `/api/users/{userId}/followers`

List followers for a user. Returns an empty page when the target user's privacy settings hide the followers list from the current viewer.

**Auth:** Public

| Parameter | Location | Type | Required | Default |
|-----------|----------|------|----------|---------|
| `userId` | Path | UUID | **Yes** | — |
| `page` | Query | integer | No | `0` |
| `size` | Query | integer | No | `20` |

**Response:** `Page<SocialUserResponse>`

```bash
curl -X GET 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/followers?page=0&size=20'
```

---

### GET `/api/users/{userId}/following`

List users followed by a user. Returns an empty page when the target user's privacy settings hide the following list from the current viewer.

**Auth:** Public

**Response:** `Page<SocialUserResponse>`

```bash
curl -X GET 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/following?page=0&size=20'
```

---

### GET `/api/users/{userId}/friends`

List accepted friends for a user.

**Auth:** Public

**Response:** `Page<SocialUserResponse>`

```bash
curl -X GET 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/friends?page=0&size=20'
```

---

### GET `/api/users/{userId}/mutual/followers`

List users who follow both `userId` and another comparison user. Users with `hideFromMutualList=true` are filtered out.

**Auth:** Public. If the request is anonymous, `withUserId` is required. If logged in and `withUserId` is omitted, the authenticated user is used as the comparison user.

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `userId` | Path | UUID | **Yes** | — | First user |
| `withUserId` | Query | UUID | No | current user | User to compare against |
| `page` | Query | integer | No | `0` | Zero-based page index |
| `size` | Query | integer | No | `20` | Page size |

**Response:** `Page<SocialUserResponse>`

```bash
curl -X GET 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/mutual/followers?withUserId=019e5a43-e0c2-7baa-9f6d-b9b9b82afb15&page=0&size=20'
```

---

### GET `/api/users/{userId}/mutual/following`

List users followed by both `userId` and another comparison user.

**Auth:** Public. If the request is anonymous, `withUserId` is required. If logged in and `withUserId` is omitted, the authenticated user is used as the comparison user.

**Response:** `Page<SocialUserResponse>`

```bash
curl -X GET 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/mutual/following?withUserId=019e5a43-e0c2-7baa-9f6d-b9b9b82afb15&page=0&size=20'
```

---

### GET `/api/users/{userId}/mutual/friends`

List accepted friends shared by `userId` and another comparison user.

**Auth:** Public. If the request is anonymous, `withUserId` is required. If logged in and `withUserId` is omitted, the authenticated user is used as the comparison user.

**Response:** `Page<SocialUserResponse>`

```bash
curl -X GET 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/mutual/friends?withUserId=019e5a43-e0c2-7baa-9f6d-b9b9b82afb15&page=0&size=20'
```

---

### GET `/api/users/{userId}/social-stats`

Get follower, following, and friend counts. Hidden follower/following counts are returned as `0` for viewers who cannot see them.

**Auth:** Public

**Response**

```json
{
  "followerCount": 120,
  "followingCount": 64,
  "friendCount": 18
}
```

---

### POST `/api/users/{userId}/friend-request`

Send a friend request.

**Auth:** User

Returns `403` when the target user has disabled add-friend actions.

```bash
curl -X POST 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/friend-request' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"source":"profile","message":"Let us connect"}'
```

**Response:** `FriendRequestResponse`

---

### POST `/api/users/{userId}/friend-request/accept`

Accept an incoming friend request from `userId`.

**Auth:** User

```bash
curl -X POST 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/friend-request/accept' \
  -H 'Authorization: Bearer <token>'
```

---

### POST `/api/users/{userId}/friend-request/decline`

Decline an incoming friend request from `userId`.

**Auth:** User

```bash
curl -X POST 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/friend-request/decline' \
  -H 'Authorization: Bearer <token>'
```

---

### DELETE `/api/users/{userId}/friend-request`

Cancel an outgoing friend request to `userId`.

**Auth:** User

```bash
curl -X DELETE 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/friend-request' \
  -H 'Authorization: Bearer <token>'
```

---

### DELETE `/api/users/{userId}/friend`

Remove an accepted friend.

**Auth:** User

```bash
curl -X DELETE 'http://localhost:9500/api/users/019e5a43-e0c2-7baa-9f6d-b9b9b82afb14/friend' \
  -H 'Authorization: Bearer <token>'
```

**Response:** `204 No Content`

---

### GET `/api/users/me/friend-requests/incoming`

List incoming pending friend requests for the authenticated user.

**Auth:** User

**Response:** `Page<FriendRequestResponse>`

---

### GET `/api/users/me/friend-requests/outgoing`

List outgoing pending friend requests for the authenticated user.

**Auth:** User

**Response:** `Page<FriendRequestResponse>`
