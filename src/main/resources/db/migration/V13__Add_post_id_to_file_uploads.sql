ALTER TABLE file_uploads ADD COLUMN IF NOT EXISTS post_id UUID;
CREATE INDEX IF NOT EXISTS idx_file_upload_post ON file_uploads(post_id);
