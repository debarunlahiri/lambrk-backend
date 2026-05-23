# Categories API

Base path: `/api/categories`. JWT required for all endpoints. Write operations require `ADMIN` role.

---

### POST `/api/categories`

Create a new category. Admin only.

**Request body:**

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

**Response:**

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

**Query params:** `page`, `size`.

**Response:**

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
  "pageable": { ... },
  "totalElements": 5,
  "totalPages": 1
}
```

---

### GET `/api/categories/{categoryId}`

Get category by UUID.

**Response:** same as POST response.

---

### GET `/api/categories/slug/{slug}`

Get category by slug.

**Response:** same as POST response.

---

### PUT `/api/categories/{categoryId}`

Update a category. Admin only.

**Request body:** same as POST.

**Response:** same as POST response.

---

### DELETE `/api/categories/{categoryId}`

Delete a category. Admin only.

**Response:** `204 No Content`.

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
