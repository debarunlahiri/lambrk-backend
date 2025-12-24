-- Lambrk Database Initial Schema
-- Database: lambrk
-- This script creates all necessary tables, indexes, functions, and constraints

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255),
    google_id VARCHAR(255) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Users table indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON users(google_id);

-- ============================================
-- VIDEOS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS videos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    url TEXT NOT NULL,
    thumbnail_url TEXT,
    duration INTEGER,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    views INTEGER DEFAULT 0,
    likes INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'archived', 'deleted')),
    processing_status VARCHAR(20) DEFAULT 'pending' CHECK (processing_status IN ('pending', 'queued', 'processing', 'completed', 'failed')),
    file_size BIGINT,
    format VARCHAR(50),
    codec VARCHAR(50),
    resolution_width INTEGER,
    resolution_height INTEGER,
    bitrate INTEGER,
    frame_rate DECIMAL(5, 2),
    category VARCHAR(100),
    tags TEXT[],
    privacy VARCHAR(20) DEFAULT 'public' CHECK (privacy IN ('public', 'unlisted', 'private')),
    is_live BOOLEAN DEFAULT false,
    live_stream_url TEXT,
    published_at TIMESTAMP,
    scheduled_publish_at TIMESTAMP,
    language VARCHAR(10),
    location VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Videos table indexes
CREATE INDEX IF NOT EXISTS idx_videos_user_id ON videos(user_id);
CREATE INDEX IF NOT EXISTS idx_videos_status ON videos(status);
CREATE INDEX IF NOT EXISTS idx_videos_processing_status ON videos(processing_status);
CREATE INDEX IF NOT EXISTS idx_videos_created_at ON videos(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_videos_published_at ON videos(published_at DESC);
CREATE INDEX IF NOT EXISTS idx_videos_category ON videos(category);
CREATE INDEX IF NOT EXISTS idx_videos_privacy ON videos(privacy);
CREATE INDEX IF NOT EXISTS idx_videos_is_live ON videos(is_live);
CREATE INDEX IF NOT EXISTS idx_videos_scheduled_publish_at ON videos(scheduled_publish_at) WHERE scheduled_publish_at IS NOT NULL;

-- ============================================
-- UPDATE TRIGGER FUNCTION
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ============================================
-- TRIGGERS
-- ============================================
-- Users table trigger
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Videos table trigger
DROP TRIGGER IF EXISTS update_videos_updated_at ON videos;
CREATE TRIGGER update_videos_updated_at 
    BEFORE UPDATE ON videos
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

