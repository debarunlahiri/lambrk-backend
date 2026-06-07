# Categories API

Base path: `/api/categories`. JWT required for all endpoints. Write operations require `ADMIN` role.

---

### POST `/api/categories`

Create a new category. Admin only.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` (Admin) |
| `name` | Body | string | **Yes** | Display name (2–50 chars) |
| `description` | Body | string | No | Category description |
| `iconUrl` | Body | string | No | Icon SVG/PNG URL |
| `imageUrl` | Body | string | No | Cover image URL |
| `color` | Body | string | No | Hex color code |
| `slug` | Body | string | **Yes** | URL-friendly identifier |
| `sortOrder` | Body | integer | No | Display order |

**Request body**

```json
{
  "name": "Technology",
  "description": "All things tech, software, and hardware",
  "iconUrl": "https://example.com/icon-tech.svg",
  "imageUrl": "https://example.com/image-tech.jpg",
  "color": "#2563EB",
  "slug": "technology",
  "sortOrder": 1
}
```

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `CategoryResponse` | Created category |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |

**Response body**

```json
{
  "id": "f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
  "name": "Technology",
  "description": "All things tech, software, and hardware",
  "iconUrl": "https://example.com/icon-tech.svg",
  "imageUrl": "https://example.com/image-tech.jpg",
  "color": "#2563EB",
  "slug": "technology",
  "sortOrder": 1,
  "communityCount": 0,
  "createdAt": "2026-05-23T10:00:00Z",
  "updatedAt": "2026-05-23T10:00:00Z"
}
```

---

### GET `/api/categories`

List all categories ordered by sortOrder and name.

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
| `200` | `Page<CategoryResponse>` | Paginated categories |
| `401` | error | JWT missing or invalid |

**Response body**

```json
{
  "content": [
    {
      "id": "f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
      "name": "Technology",
      "description": "All things tech, software, and hardware",
      "iconUrl": "https://example.com/icon-tech.svg",
      "imageUrl": "https://example.com/image-tech.jpg",
      "color": "#2563EB",
      "slug": "technology",
      "sortOrder": 1,
      "communityCount": 2,
      "createdAt": "2026-05-23T10:00:00Z",
      "updatedAt": "2026-05-23T10:00:00Z"
    }
  ],
  "pageable": { },
  "totalElements": 5,
  "totalPages": 1
}
```

---

### GET `/api/categories/{categoryId}`

Get category by UUID.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `categoryId` | Path | UUID | **Yes** | Category UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `CategoryResponse` | Category details |
| `401` | error | JWT missing or invalid |
| `404` | error | Category not found |

**Response body**

Same as POST response.

---

### GET `/api/categories/slug/{slug}`

Get category by slug.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` |
| `slug` | Path | string | **Yes** | Category slug |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `CategoryResponse` | Category details |
| `401` | error | JWT missing or invalid |
| `404` | error | Category not found |

**Response body**

Same as POST response.

---

### PUT `/api/categories/{categoryId}`

Update a category. Admin only.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` (Admin) |
| `categoryId` | Path | UUID | **Yes** | Category UUID |
| `name` | Body | string | No | Display name |
| `description` | Body | string | No | Description |
| `iconUrl` | Body | string | No | Icon URL |
| `imageUrl` | Body | string | No | Cover image URL |
| `color` | Body | string | No | Hex color |
| `slug` | Body | string | No | URL slug |
| `sortOrder` | Body | integer | No | Display order |

**Request body**

Same as POST request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `200` | `CategoryResponse` | Updated category |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |
| `404` | error | Category not found |

**Response body**

Same as POST response.

---

### DELETE `/api/categories/{categoryId}`

Delete a category. Admin only.

**What to send**

| Parameter | Location | Type | Required | Description |
|-----------|----------|------|----------|-------------|
| `Authorization` | Header | string | **Yes** | `Bearer <jwt>` (Admin) |
| `categoryId` | Path | UUID | **Yes** | Category UUID |

No request body.

**Response**

| Status | Body | Description |
|--------|------|-------------|
| `204` | empty | Category deleted |
| `401` | error | JWT missing or invalid |
| `403` | error | Not an admin |
| `404` | error | Category not found |

---

## Category Fields

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Category UUIDv7 ID |
| `name` | string | Display name (2–50 chars) |
| `description` | string | Optional description |
| `iconUrl` | string | Optional icon SVG/PNG URL |
| `imageUrl` | string | Optional cover image URL |
| `color` | string | Hex color code (e.g., `#2563EB`) |
| `slug` | string | URL-friendly identifier |
| `sortOrder` | int | Display order |
| `communityCount` | int | Number of communities in this category |
| `createdAt` | ISO 8601 | Creation timestamp |
| `updatedAt` | ISO 8601 | Last update timestamp |
