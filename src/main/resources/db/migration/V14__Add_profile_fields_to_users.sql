-- ============================================================
-- V14: Add profile fields to users table
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS header_image_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS location VARCHAR(100),
    ADD COLUMN IF NOT EXISTS website VARCHAR(200);
