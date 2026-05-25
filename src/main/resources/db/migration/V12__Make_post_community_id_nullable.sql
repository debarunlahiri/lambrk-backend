-- Make post community_id nullable to allow posts without a community
ALTER TABLE posts ALTER COLUMN community_id DROP NOT NULL;
