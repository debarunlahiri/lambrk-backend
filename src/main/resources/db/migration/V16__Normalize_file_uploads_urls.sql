-- ============================================================
-- V16: Normalize file_uploads URLs to raw S3 keys
-- Clean up old full S3 URLs and broken local thumbnail paths
-- ============================================================

-- Convert full S3 file URLs to raw keys
UPDATE file_uploads
SET file_url = SUBSTRING(file_url FROM POSITION('.amazonaws.com/' IN file_url) + 16)
WHERE file_url LIKE '%.amazonaws.com/%';

-- Convert full S3 thumbnail URLs to raw keys
UPDATE file_uploads
SET thumbnail_url = SUBSTRING(thumbnail_url FROM POSITION('.amazonaws.com/' IN thumbnail_url) + 16)
WHERE thumbnail_url LIKE '%.amazonaws.com/%';

-- Remove broken local thumbnail paths for S3 video uploads
UPDATE file_uploads
SET thumbnail_url = NULL
WHERE type = 'POST_VIDEO' AND thumbnail_url LIKE '/api/files/thumbnails/%';

-- Also clean up any other broken local thumbnail paths in S3 mode
UPDATE file_uploads
SET thumbnail_url = NULL
WHERE thumbnail_url LIKE '/api/files/thumbnails/%';
