# Files API

Base path: `/api/files`. JWT required.

### POST `/api/files/upload`

Upload a file with metadata.

**Auth:** User

**Request body**

`multipart/form-data` fields: `file`, `type`, `fileName`, `description`, `isPublic`, `isNSFW`, `altText`.

**cURL**

```bash
curl -X POST 'http://localhost:9500/api/files/upload' \
  -H 'Authorization: Bearer <token>' \
  -F 'file=@/path/to/file.png' \
  -F 'type=POST_IMAGE' \
  -F 'description=Post image' \
  -F 'isPublic=true' \
  -F 'isNSFW=false' \
  -F 'altText=Screenshot'
```

**Response**

```json
{"fileId":1,"fileName":"stored-file.png","originalFileName":"file.png","fileUrl":"https://example.com/files/stored-file.png","thumbnailUrl":null,"type":"POST_IMAGE","fileSize":1024,"mimeType":"image/png","description":"Post image","isPublic":true,"isNSFW":false,"altText":"Screenshot","uploadedBy":1,"uploadedAt":"2026-05-02T10:00:00Z","checksum":"abc123"}
```
### GET `/api/files/{fileId}`

Get file metadata.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/1' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"fileId":1,"fileName":"stored-file.png","originalFileName":"file.png","fileUrl":"https://example.com/files/stored-file.png","thumbnailUrl":null,"type":"POST_IMAGE","fileSize":1024,"mimeType":"image/png","description":"Post image","isPublic":true,"isNSFW":false,"altText":"Screenshot","uploadedBy":1,"uploadedAt":"2026-05-02T10:00:00Z","checksum":"abc123"}
```
### GET `/api/files/{fileId}/content`

Download file content.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/1/content' \
  -H 'Authorization: Bearer <token>'
```

**Response**

Binary file response with `Content-Disposition`, `Content-Type`, and `Content-Length` headers.
### GET `/api/files`

Get current user files.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/files/type/{type}`

Get files by type.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/type/POST_IMAGE?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### GET `/api/files/public`

Get public post image files.

**Auth:** User

**Query/path parameters**

| Name | Type | Required | Default | Description |
| --- | --- | --- | --- | --- |
| `page` | integer | no | `0` | Zero-based page index. |
| `size` | integer | no | `20` | Page size. |

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/public?page=0&size=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"content":[],"totalElements":0,"totalPages":0,"size":20,"number":0,"first":true,"last":true,"numberOfElements":0,"empty":true}
```
### PUT `/api/files/{fileId}`

Update file metadata.

**Auth:** User

**Request body**

```json
{"type":"POST_IMAGE","fileName":"file.png","description":"Updated","isPublic":true,"isNSFW":false,"altText":"Screenshot"}
```

**cURL**

```bash
curl -X PUT 'http://localhost:9500/api/files/1' \
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
{"fileId":1,"fileName":"stored-file.png","originalFileName":"file.png","fileUrl":"https://example.com/files/stored-file.png","thumbnailUrl":null,"type":"POST_IMAGE","fileSize":1024,"mimeType":"image/png","description":"Post image","isPublic":true,"isNSFW":false,"altText":"Screenshot","uploadedBy":1,"uploadedAt":"2026-05-02T10:00:00Z","checksum":"abc123"}
```
### DELETE `/api/files/{fileId}`

Delete file.

**Auth:** User

**cURL**

```bash
curl -X DELETE 'http://localhost:9500/api/files/1' \
  -H 'Authorization: Bearer <token>'
```

**Response**

`204 No Content`
### GET `/api/files/stats`

Get placeholder file stats.

**Auth:** User

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/stats' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
{"totalFiles":0,"totalSize":0,"imageCount":0,"videoCount":0,"avatarCount":0}
```
### GET `/api/files/search`

Search files. Current implementation returns an empty list.

**Auth:** User

**Query/path parameters**

Required `query`; optional `limit` default `20`.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/search?query=avatar&limit=20' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
### GET `/api/files/recent`

Get recent files. Current implementation returns an empty list.

**Auth:** User

**Query/path parameters**

Optional `limit` default `10`.

**cURL**

```bash
curl -X GET 'http://localhost:9500/api/files/recent?limit=10' \
  -H 'Authorization: Bearer <token>'
```

**Response**

```json
[]
```
