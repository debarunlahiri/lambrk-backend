# LoopMix API (Reels / Shorts)

Short-form vertical media feed endpoints — like Instagram Reels or YouTube Shorts.

Base path: `/api/posts`. `GET /api/posts/media` is public (works without auth). `/view` and `/related` are public. All other post endpoints require JWT.

---

### GET `/api/posts/media`

Returns only posts that have at least one media item (IMAGE or VIDEO). Sorted by newest first.

**Auth:** Public (JWT optional for personalized `isBookmarked` / `userVote`)

**What to send**

| Parameter       | Location | Type   | Required | Default | Description                       |
| --------------- | -------- | ------ | -------- | ------- | --------------------------------- |
| `Authorization` | Header   | string | No       | —       | `Bearer <jwt>` (optional)         |
| `type`          | Query    | string | No       | `ALL`   | `ALL` (both), `IMAGE`, or `VIDEO` |
| `page`          | Query    | int    | No       | `0`     | Page number (0-indexed)           |
| `size`          | Query    | int    | No       | `20`    | Items per page (max 50)           |

No request body.

**Response**

| Status | Body                 | Description               |
| ------ | -------------------- | ------------------------- |
| `200`  | `Page<PostResponse>` | Paginated media posts     |
| `401`  | error                | JWT invalid (if provided) |

**cURL**

```bash
# All media (images + videos)
curl 'http://localhost:9500/api/posts/media?type=ALL&page=0&size=20' \
  -H 'Authorization: Bearer <token>'

# Videos only
curl 'http://localhost:9500/api/posts/media?type=VIDEO&page=0&size=20' \
  -H 'Authorization: Bearer <token>'

# Images only
curl 'http://localhost:9500/api/posts/media?type=IMAGE&page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response** — same shape as `listPostsHot`, guaranteed `media.length > 0`

```json
{
  "content": [
    {
      "id": "019e9e36-d777-7648-b2b4-e6f350715b72",
      "title": null,
      "content": "",
      "url": null,
      "postType": "VIDEO",
      "thumbnailUrl": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/posts/media/video/thumb/019e9e36-d5a2-7555-b991-6620ed14edc9.jpg",
      "flairText": null,
      "flairCssClass": null,
      "isSpoiler": false,
      "isStickied": false,
      "isLocked": false,
      "isArchived": false,
      "isOver18": false,
      "score": 42,
      "likeCount": 15,
      "dislikeCount": 0,
      "commentCount": 3,
      "viewCount": 128,
      "awardCount": 0,
      "author": {
        "id": "019e5a64-1328-7a3d-a5b0-856ccf997dec",
        "username": "debarunlahiri",
        "displayName": "Debarun Lahiri",
        "bio": null,
        "avatarUrl": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/profile/profile_img/...",
        "headerImageUrl": null,
        "location": null,
        "website": null,
        "isActive": true,
        "isVerified": false,
        "karma": 30,
        "createdAt": "2026-05-24T14:29:28.852551Z",
        "updatedAt": "2026-06-07T00:04:14.774340Z"
      },
      "community": {
        "id": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb15",
        "name": "tech",
        "title": "Technology",
        "description": "All things tech",
        "sidebarText": null,
        "headerImageUrl": null,
        "iconImageUrl": null,
        "isPublic": true,
        "isRestricted": false,
        "isOver18": false,
        "memberCount": 150,
        "subscriberCount": 150,
        "activeUserCount": 12,
        "createdBy": { ... },
        "categories": [],
        "createdAt": "2026-05-24T14:29:28.852551Z",
        "updatedAt": "2026-05-24T14:29:28.852551Z",
        "isUserSubscribed": true,
        "isUserModerator": false
      },
      "createdAt": "2026-06-06T18:34:14.780528Z",
      "updatedAt": "2026-06-06T18:34:14.780528Z",
      "archivedAt": null,
      "userVote": null,
      "isBookmarked": false,
      "media": [
        {
          "id": "019e9e36-d747-7b1a-99e8-c3a2eb94f43a",
          "url": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/posts/media/video/019e9e36-d5a2-7555-b991-6620ed14edc9.mp4",
          "thumbnailUrl": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/posts/media/video/thumb/019e9e36-d5a2-7555-b991-6620ed14edc9.jpg",
          "type": "POST_VIDEO",
          "mimeType": "video/mp4",
          "fileSize": 1693304,
          "altText": null
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "last": false,
  "totalElements": 87,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "empty": false
}
```

---

### POST `/api/posts/{postId}/view`

Records a view when a LoopMix item has been in viewport for ~2 seconds. Increments `viewCount` atomically. Returns `204 No Content`.

**Auth:** Public (no auth required — works for both logged-in and anonymous users)

**What to send**

| Parameter | Location | Type | Required | Description |
| --------- | -------- | ---- | -------- | ----------- |
| `postId`  | Path     | UUID | **Yes**  | Post UUID   |

No request body. No headers required.

**Response**

| Status | Body  | Description    |
| ------ | ----- | -------------- |
| `204`  | empty | View recorded  |
| `404`  | error | Post not found |

**cURL**

```bash
# Without auth (anonymous)
curl -X POST 'http://localhost:9500/api/posts/019e9e36-d777-7648-b2b4-e6f350715b72/view'

# With auth (logged-in user)
curl -X POST 'http://localhost:9500/api/posts/019e9e36-d777-7648-b2b4-e6f350715b72/view' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```
HTTP/1.1 204 No Content
```

---

### GET `/api/posts/{postId}/related`

Returns related media posts (IMAGE or VIDEO) for infinite scroll when the user reaches the end of the feed. Results are the same author's posts + posts from the same community, sorted by newest first.

**Auth:** Public (JWT optional for personalized `isBookmarked` / `userVote`)

**What to send**

| Parameter       | Location | Type   | Required | Default | Description                           |
| --------------- | -------- | ------ | -------- | ------- | ------------------------------------- |
| `Authorization` | Header   | string | No       | —       | `Bearer <jwt>` (optional)             |
| `postId`        | Path     | UUID   | **Yes**  | —       | Post UUID to find related content for |
| `size`          | Query    | int    | No       | `10`    | Number of related items to return     |

No request body.

**Response**

| Status | Body                 | Description               |
| ------ | -------------------- | ------------------------- |
| `200`  | `List<PostResponse>` | Related media posts       |
| `401`  | error                | JWT invalid (if provided) |
| `404`  | error                | Post not found            |

**cURL**

```bash
curl 'http://localhost:9500/api/posts/019e9e36-d777-7648-b2b4-e6f350715b72/related?size=10' \
  -H 'Authorization: Bearer <token>'
```

**Response** — `List<PostResponse>` (same shape as `/api/posts/media` items)

```json
[
  {
    "id": "019e9e36-96e7-7a69-837c-a7d9986cde09",
    "title": null,
    "content": "",
    "url": null,
    "postType": "VIDEO",
    "thumbnailUrl": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/posts/media/video/thumb/019e9e36-9423-7788-8d78-28454d650fd9.jpg",
    "flairText": null,
    "flairCssClass": null,
    "isSpoiler": false,
    "isStickied": false,
    "isLocked": false,
    "isArchived": false,
    "isOver18": false,
    "score": 12,
    "likeCount": 5,
    "dislikeCount": 0,
    "commentCount": 1,
    "viewCount": 64,
    "awardCount": 0,
    "author": { ... },
    "community": { ... },
    "createdAt": "2026-06-06T18:33:58.249779Z",
    "updatedAt": "2026-06-06T18:33:58.249779Z",
    "archivedAt": null,
    "userVote": null,
    "isBookmarked": false,
    "media": [
      {
        "id": "019e9e36-96d0-7ece-9ded-f9ccb3fd91fe",
        "url": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/posts/media/video/019e9e36-9423-7788-8d78-28454d650fd9.mp4",
        "thumbnailUrl": "https://d2l1t2x4807mjw.cloudfront.net/lambrk/posts/media/video/thumb/019e9e36-9423-7788-8d78-28454d650fd9.jpg",
        "type": "POST_VIDEO",
        "mimeType": "video/mp4",
        "fileSize": 4002375,
        "altText": null
      }
    ]
  }
]
```

---

## Implementation Notes

- All media URLs are resolved through the CDN (`d2l1t2x4807mjw.cloudfront.net`) automatically
- `POST /view` is intentionally lightweight — a single atomic `UPDATE` query, no DB reads
- `/related` excludes the current post and only returns IMAGE/VIDEO types
- Cache keys:
  - `mediaPosts:{type}-{page}-{size}`
  - `relatedPosts:{postId}-{size}`

## Errors

| Code | Meaning                         |
| ---- | ------------------------------- |
| 401  | JWT missing / invalid           |
| 404  | Post not found (for `/related`) |
| 500  | Internal server error           |
