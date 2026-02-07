# Files API

Base URL: `/api/files`

All endpoints require **JWT authentication**.

---

## POST `/api/files/upload`

Upload a file to the platform.

### Headers

```
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

### Request Body (multipart/form-data)

| Field        | Type    | Description |
|--------------|---------|-------------|
| file         | File    | File to upload (required) |
| type         | String  | File type (required) |
| description  | String  | File description (optional) |
| isPublic     | boolean | Public visibility (default false) |
| isNSFW       | boolean | NSFW content (default false) |
| altText      | String  | Alt text for accessibility (optional) |

### File Types

| Type            | Description                              | Max Size |
|-----------------|------------------------------------------|----------|
| AVATAR          | User profile picture                     | 5MB      |
| POST_IMAGE      | Post image content                        | 10MB     |
| POST_VIDEO       | Post video content                        | 50MB     |
| SUBREDDIT_ICON  | Sublambrk icon                           | 2MB      |
| SUBREDDIT_HEADER| Sublambrk header/banner                  | 10MB     |
| BANNER          | Site banner                              | 5MB      |

### Allowed MIME Types

- Images: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- Videos: `video/mp4`, `video/webm`, `video/ogg`

### Response `200 OK`

```json
{
  "fileId": 1001,
  "fileName": "abc123def456.jpg",
  "originalFileName": "profile-pic.jpg",
  "fileUrl": "/api/files/abc123def456.jpg",
  "thumbnailUrl": "/api/files/thumbnails/abc123def456.jpg",
  "type": "AVATAR",
  "fileSize": 1048576,
  "mimeType": "image/jpeg",
  "description": "My new profile picture",
  "isPublic": false,
  "isNSFW": false,
  "altText": "Profile picture of user",
  "uploadedBy": 1,
  "uploadedAt": "2026-02-07T16:00:00Z",
  "checksum": "sha256:abc123def456..."
}
```

---

## GET `/api/files/{fileId}`

Get file metadata by ID.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| fileId | Long | File ID |

### Response `200 OK`

Same shape as upload response.

### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| 401    | Not authenticated              |
| 403    | No access to private file     |
| 404    | File not found                 |

---

## GET `/api/files/{fileId}/content`

Download the actual file content.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| fileId | Long | File ID |

### Response `200 OK`

Binary file content with appropriate headers:

```
Content-Type: image/jpeg
Content-Disposition: attachment; filename="profile-pic.jpg"
Content-Length: 1048576
```

---

## GET `/api/files`

Get files uploaded by the authenticated user.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

```json
{
  "content": [
    {
      "fileId": 1001,
      "fileName": "abc123def456.jpg",
      "originalFileName": "profile-pic.jpg",
      "type": "AVATAR",
      "fileSize": 1048576,
      "uploadedAt": "2026-02-07T16:00:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## GET `/api/files/type/{type}`

Get public files by type.

### Path Parameters

| Param | Type   | Description |
|-------|--------|-------------|
| type  | String | File type |

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

### Response `200 OK`

Paginated public files of the specified type.

---

## GET `/api/files/public`

Get all public files.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| page  | int  | 0       | Page number |
| size  | int  | 20      | Page size   |

---

## PUT `/api/files/{fileId}`

Update file metadata (description, visibility, etc.).

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| fileId | Long | File ID |

### Request Body

```json
{
  "description": "Updated description",
  "isPublic": true,
  "isNSFW": false,
  "altText": "Updated alt text"
}
```

### Response `200 OK`

Updated file metadata.

---

## DELETE `/api/files/{fileId}`

Delete a file.

### Path Parameters

| Param  | Type | Description |
|--------|------|-------------|
| fileId | Long | File ID |

### Response `204 No Content`

File and metadata deleted from disk and database.

### Error Responses

| Status | Condition                      |
|--------|--------------------------------|
| 401    | Not authenticated              |
| 403    | Not file owner                 |
| 404    | File not found                 |

---

## GET `/api/files/stats`

Get file upload statistics for the authenticated user.

### Response `200 OK`

```json
{
  "totalFiles": 25,
  "totalSize": 52428800,
  "imageCount": 20,
  "videoCount": 3,
  "avatarCount": 1,
  "sublambrkIconCount": 1
}
```

---

## GET `/api/files/search`

Search files by filename.

### Query Parameters

| Param   | Type   | Default | Description |
|---------|--------|---------|-------------|
| query   | String | —       | Search term (req) |
| limit   | int    | 20      | Result limit |

### Response `200 OK`

```json
[
  {
    "fileId": 1001,
    "fileName": "abc123def456.jpg",
    "originalFileName": "profile-pic.jpg",
    "type": "AVATAR",
    "uploadedAt": "2026-02-07T16:00:00Z"
  }
]
```

---

## GET `/api/files/recent`

Get recently uploaded files.

### Query Parameters

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| limit | int  | 10      | Result limit |

---

## File Processing

### Automatic Processing

1. **Checksum Generation**: SHA-256 hash for integrity
2. **Thumbnail Generation**: For images and videos
3. **Virus Scanning**: (Optional) Security scanning
4. **Content Analysis**: NSFW detection
5. **Metadata Extraction**: EXIF, dimensions, duration

### Storage

- **AWS S3**: Primary cloud storage (configurable, default enabled)
- **Local Storage**: Fallback when S3 is disabled
- **File Organization**: Organized by type (avatars/, posts/, sublambrks/)
- **Presigned URLs**: Temporary secure access URLs for private files
- **Cleanup**: Automatic cleanup of orphaned files
- **Backup**: S3 lifecycle policies for archival

### Free Tier Limits

Free tier users have the following monthly limits:

| Limit | Value | Description |
|-------|-------|-------------|
| Storage | 5 GB | Total file storage per user |
| Uploads | 100 files | Maximum uploads per month |
| Bandwidth | 100 GB | Monthly download bandwidth |

**Note**: These limits reset at the beginning of each calendar month.

---

## Free Tier Management

### GET `/api/files/usage`

Get current free tier usage statistics for the authenticated user.

### Response `200 OK`

```json
{
  "storageUsedBytes": 2147483648,
  "storageLimitBytes": 5368709120,
  "uploadsThisMonth": 45,
  "uploadLimit": 100,
  "bandwidthUsedBytes": 10737418240,
  "bandwidthLimitBytes": 107374182400,
  "isFreeTier": true,
  "withinLimits": true,
  "storageUsedPercent": 40.0,
  "uploadsUsedPercent": 45.0,
  "bandwidthUsedPercent": 10.0
}
```

### Error Responses

| Status | Condition |
|--------|-----------|
| 402 | Free tier limit exceeded (Payment Required) |
| 429 | Monthly upload limit exceeded |
| 413 | File too large for remaining storage quota |

---

## Security

- **Access Control**: Users can only access their own files
- **Public Files**: Marked files are accessible to all users
- **NSFW Filtering**: Respects user preferences
- **File Type Validation**: Strict MIME type checking

---

## Performance

- **Caching**: File metadata cached for 10 minutes
- **Rate Limiting**: 20 uploads per minute
- **Chunked Upload**: Large files uploaded in chunks
- **CDN Integration**: Ready for CDN deployment

---

## Error Handling

| Status | Condition |
|--------|-----------|
| 400 | Invalid file or metadata |
| 401 | Not authenticated |
| 402 | Free tier storage/bandwidth limit exceeded |
| 403 | Access denied |
| 404 | File not found |
| 413 | File too large or exceeds remaining storage quota |
| 415 | Unsupported file type |
| 429 | Rate limit or monthly upload limit exceeded |
| 503 | S3 storage service unavailable |

---

## Metrics

All file endpoints emit these metrics:

- `files.uploaded` - Files uploaded by type
- `files.downloaded` - File downloads
- `files.deleted` - Files deleted
- `files.public.viewed` - Public file views
- `files.user.viewed` - User file views
- `files.searched` - File searches
- `files.recent.viewed` - Recent file views
- `files.s3.uploaded` - S3 upload operations
- `files.s3.downloaded` - S3 download operations
- `files.s3.deleted` - S3 delete operations
- `files.free_tier.limit_exceeded` - Free tier limit violations

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AWS_REGION` | AWS region for S3 | `us-east-1` |
| `AWS_S3_BUCKET` | S3 bucket name | `lambrk-files` |
| `AWS_ACCESS_KEY_ID` | AWS access key | - |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | - |
| `AWS_S3_ENDPOINT` | Custom S3 endpoint (MinIO) | - |
| `AWS_S3_ENABLED` | Enable/disable S3 | `true` |

### Free Tier Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `app.free-tier.enabled` | Enable free tier limits | `true` |
| `app.free-tier.storage-limit-mb` | Storage limit in MB | `5120` (5GB) |
| `app.free-tier.monthly-upload-limit` | Uploads per month | `100` |
| `app.free-tier.monthly-bandwidth-gb` | Bandwidth per month | `100` |
