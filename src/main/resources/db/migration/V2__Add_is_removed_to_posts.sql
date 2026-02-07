-- Add is_removed column to posts table
ALTER TABLE posts ADD COLUMN is_removed BOOLEAN NOT NULL DEFAULT FALSE;

-- Add index for is_removed
CREATE INDEX idx_post_is_removed ON posts(is_removed);
