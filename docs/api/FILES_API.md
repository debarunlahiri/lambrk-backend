# Files API

Base path: `/api/files`. JWT required.

---

### POST `/api/files/upload`

Upload files with metadata. Supports bulk upload (up to 20 files, 5MB each).

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `files` | Body | multipart | **Yes** | — | Up to 20 files |
| `type` | Body | string | **Yes** | — | `POST_IMAGE`, `POST_VIDEO`, `AVATAR`, `COMMUNITY_BANNER`, `COMMUNITY_ICON`, `PROFILE_IMAGE`, `COVER_IMAGE` |
| `fileName` | Body | string | No | — | Custom filename |
| `description` | Body | string | No | — | File description |
| `isPublic` | Body | boolean | No | `true` | Public visibility |
| `isNSFW` | Body | boolean | No | `false` | NSFW flag |
| `altText` | Body | string | No | — | Accessibility alt text |

**Request body**

`multipart/form-data` fields:
- `files` (required, up to 20 files)
- `type` (required: `POST_IMAGE`, `POST_VIDEO`, `AVATAR`, `COMMUNITY_BANNER`, `COMMUNITY_ICON`, `PROFILE_IMAGE`, `COVER_IMAGE`)
- `fileName` (optional)
- `description` (optional)
- `isPublic` (optional, default `true`)
- `isNSFW` (optional, default `false`)
- `altText` (optional)

**S3 folder structure**

| Type | Main path | Thumbnail path |
| --- | --- | --- |
| `POST_IMAGE` | `lambrk/posts/media/image/main/{photo_id}.ext` | `lambrk/posts/media/image/thumb/{photo_id}.jpg` |
| `PROFILE_IMAGE` | `lambrk/profile/profile_img/{user_id}/{photo_id}/main/{photo_id}.ext` | `lambrk/profile/profile_img/{user_id}/{photo_id}/thumb/{photo_id}.jpg` |
| `COVER_IMAGE` | `lambrk/profile/cover_img/{user_id}/{photo_id}/main/{photo_id}.ext` | `lambrk/profile/cover_img/{user_id}/{photo_id}/thumb/{photo_id}.jpg` |

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `List<FileResponse>` | Uploaded file metadata |
| `401` | error | JWT missing or invalid |
| `400` | error | Validation or size limit exceeded |

**cURL — Post images**

```bash
curl -X POST 'http://localhost:9500/api/files/upload' \
  -H 'Authorization: Bearer <token>' \
  -F 'files=@/path/to/image1.png' \
  -F 'files=@/path/to/image2.jpg' \
  -F 'type=POST_IMAGE' \
  -F 'description=Post images' \
  -F 'isPublic=true' \
  -F 'isNSFW=false' \
  -F 'altText=Screenshots'
```

**cURL — Profile photo**

```bash
curl -X POST 'http://localhost:9500/api/files/upload' \
  -H 'Authorization: Bearer <token>' \
  -F 'files=@/path/to/avatar.png' \
  -F 'type=PROFILE_IMAGE'
```

**cURL — Cover photo**

```bash
curl -X POST 'http://localhost:9500/api/files/upload' \
  -H 'Authorization: Bearer <token>' \
  -F 'files=@/path/to/banner.jpg' \
  -F 'type=COVER_IMAGE'
```

**Response**

```json
[
  {
    "fileId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb19",
    "fileName": "stored-file-1.png",
    "originalFileName": "image1.png",
    "fileUrl": "https://s3.ap-south-1.amazonaws.com/lm-sm-001/lambrk/posts/media/image/main/019e5a43-e0c2-7baa-9f6d-b9b9b82afb19.png",
    "thumbnailUrl": "https://s3.ap-south-1.amazonaws.com/lm-sm-001/lambrk/posts/media/image/thumb/019e5a43-e0c2-7baa-9f6d-b9b9b82afb19.jpg",
    "type": "POST_IMAGE",
    "fileSize": 1024,
    "mimeType": "image/png",
    "description": "Post images",
    "isPublic": true,
    "isNSFW": false,
    "altText": "Screenshots",
    "uploadedBy": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
    "uploadedAt": "2026-05-02T10:00:00Z",
    "checksum": "abc123"
  }
]
```

---

### GET `/api/files/{fileId}`

Get file metadata.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `fileId` | Path | UUID | **Yes** | File UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FileResponse` | File metadata |
| `401` | error | JWT missing or invalid |
| `404` | error | File not found |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{
  "fileId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb19",
  "fileName": "stored-file.png",
  "originalFileName": "file.png",
  "fileUrl": "https://s3.ap-south-1.amazonaws.com/lm-sm-001/lambrk/posts/media/image/main/019e5a43-e0c2-7baa-9f6d-b9b9b82afb19.png",
  "thumbnailUrl": "https://s3.ap-south-1.amazonaws.com/lm-sm-001/lambrk/posts/media/image/thumb/019e5a43-e0c2-7baa-9f6d-b9b9b82afb19.jpg",
  "type": "POST_IMAGE",
  "fileSize": 1024,
  "mimeType": "image/png",
  "description": "Post image",
  "isPublic": true,
  "isNSFW": false,
  "altText": "Screenshot",
  "uploadedBy": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "uploadedAt": "2026-05-02T10:00:00Z",
  "checksum": "abc123"
}
```

---

### GET `/api/files/{fileId}/content`

Download file content.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `fileId` | Path | UUID | **Yes** | File UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | binary | File content with `Content-Disposition` |
| `401` | error | JWT missing or invalid |
| `404` | error | File not found |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/content' \
  -H 'Authorization: Bearer <token>'
```

**Response**

Binary file response with `Content-Disposition`, `Content-Type`, and `Content-Length` headers.

---

### GET `/api/files`

Get current user files.

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
| `200` | `Page<FileResponse>` | Paginated user files |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
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

### GET `/api/files/type/{type}`

Get files by type.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `type` | Path | string | **Yes** | — | File type |
| `page` | Query | integer | No | `0` | Page number |
| `size` | Query | integer | No | `20` | Page size |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `Page<FileResponse>` | Paginated files of the type |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/type/POST_IMAGE?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
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

### GET `/api/files/public`

Get public post image files.

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
| `200` | `Page<FileResponse>` | Public files |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/public?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
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

### PUT `/api/files/{fileId}`

Update file metadata.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `fileId` | Path | UUID | **Yes** | File UUID |
| `type` | Body | string | No | File type |
| `fileName` | Body | string | No | Filename |
| `description` | Body | string | No | Description |
| `isPublic` | Body | boolean | No | Visibility |
| `isNSFW` | Body | boolean | No | NSFW flag |
| `altText` | Body | string | No | Alt text |

**Request body**

```json
{
  "type": "POST_IMAGE",
  "fileName": "file.png",
  "description": "Updated",
  "isPublic": true,
  "isNSFW": false,
  "altText": "Screenshot"
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FileResponse` | Updated file metadata |
| `401` | error | JWT missing or invalid |
| `403` | error | Not the owner |
| `404` | error | File not found |

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/files/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
  "type": "POST_IMAGE",
  "fileName": "file.png",
  "description": "Updated",
  "isPublic": true,
  "isNSFW": false,
  "altText": "Screenshot"
}'
```

**Response**

```json
{
  "fileId": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb19",
  "fileName": "stored-file.png",
  "originalFileName": "file.png",
  "fileUrl": "https://s3.ap-south-1.amazonaws.com/lm-sm-001/lambrk/posts/media/image/main/019e5a43-e0c2-7baa-9f6d-b9b9b82afb19.png",
  "thumbnailUrl": "https://s3.ap-south-1.amazonaws.com/lm-sm-001/lambrk/posts/media/image/thumb/019e5a43-e0c2-7baa-9f6d-b9b9b82afb19.jpg",
  "type": "POST_IMAGE",
  "fileSize": 1024,
  "mimeType": "image/png",
  "description": "Post image",
  "isPublic": true,
  "isNSFW": false,
  "altText": "Screenshot",
  "uploadedBy": "019e5a43-e0c2-7baa-9f6d-b9b9b82afb14",
  "uploadedAt": "2026-05-02T10:00:00Z",
  "checksum": "abc123"
}
```

---

### DELETE `/api/files/{fileId}`

Delete file.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `fileId` | Path | UUID | **Yes** | File UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | File deleted |
| `401` | error | JWT missing or invalid |
| `403` | error | Not the owner |
| `404` | error | File not found |

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/files/b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`

---

### GET `/api/files/stats`

Get placeholder file stats.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `FileStats` | File statistics |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/stats' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"totalFiles":0,"totalSize":0,"imageCount":0,"videoCount":0,"avatarCount":0}
```

---

### GET `/api/files/search`

Search files. Current implementation returns an empty list.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `query` | Query | string | **Yes** | — | Search text |
| `limit` | Query | integer | No | `20` | Max results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `List<FileResponse>` | Search results |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/search?query=avatar&limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```

---

### GET `/api/files/recent`

Get recent files. Current implementation returns an empty list.

**Auth:** User

**What to send**

| Parameter | Location | Type | Required | Default | Description |
|-----------|----------|------|----------|---------|-------------|
| `Authorization` | Header | string | **Yes** | — | `Bearer <jwt>` |
| `limit` | Query | integer | No | `10` | Max results |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `List<FileResponse>` | Recent files |
| `401` | error | JWT missing or invalid |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/recent?limit=10' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
