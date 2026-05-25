-- Make post title nullable to allow text-only posts without titles
ALTER TABLE posts ALTER COLUMN title DROP NOT NULL;
