-- Lambrk Platform - Comprehensive Schema
-- This migration adds support for Bitz, Posts, Comments, Likes, Playlists, Subscriptions, and Downloads

-- ============================================
-- CONTENT TYPES ENUM
-- ============================================
DO $$ BEGIN
    CREATE TYPE content_type AS ENUM ('video', 'bitz', 'post');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- ============================================
-- BITZ TABLE (Short Vertical Videos)
-- ============================================
CREATE TABLE IF NOT EXISTS bitz (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    url TEXT NOT NULL,
    thumbnail_url TEXT,
    duration INTEGER,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    views INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published', 'processing')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bitz table indexes
CREATE INDEX IF NOT EXISTS idx_bitz_user_id ON bitz(user_id);
CREATE INDEX IF NOT EXISTS idx_bitz_status ON bitz(status);
CREATE INDEX IF NOT EXISTS idx_bitz_created_at ON bitz(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_bitz_views ON bitz(views DESC);

-- Bitz trigger
DROP TRIGGER IF EXISTS update_bitz_updated_at ON bitz;
CREATE TRIGGER update_bitz_updated_at 
    BEFORE UPDATE ON bitz
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- POSTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url TEXT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    views INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'draft' CHECK (status IN ('draft', 'published')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts table indexes
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);

-- Posts trigger
DROP TRIGGER IF EXISTS update_posts_updated_at ON posts;
CREATE TRIGGER update_posts_updated_at 
    BEFORE UPDATE ON posts
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- LIKES TABLE (Universal Likes/Dislikes)
-- ============================================
CREATE TABLE IF NOT EXISTS likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    like_type VARCHAR(10) NOT NULL CHECK (like_type IN ('like', 'dislike')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, content_type, content_id)
);

-- Likes table indexes
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes(user_id);
CREATE INDEX IF NOT EXISTS idx_likes_content ON likes(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_likes_content_type ON likes(content_type, content_id, like_type);

-- Likes trigger
DROP TRIGGER IF EXISTS update_likes_updated_at ON likes;
CREATE TRIGGER update_likes_updated_at 
    BEFORE UPDATE ON likes
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- COMMENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    parent_comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Comments table indexes
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_content ON comments(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments(parent_comment_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at DESC);

-- Comments trigger
DROP TRIGGER IF EXISTS update_comments_updated_at ON comments;
CREATE TRIGGER update_comments_updated_at 
    BEFORE UPDATE ON comments
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- PLAYLISTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS playlists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT true,
    is_watch_later BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Playlists table indexes
CREATE INDEX IF NOT EXISTS idx_playlists_user_id ON playlists(user_id);
CREATE INDEX IF NOT EXISTS idx_playlists_is_public ON playlists(is_public);
CREATE INDEX IF NOT EXISTS idx_playlists_watch_later ON playlists(user_id, is_watch_later);

-- Playlists trigger
DROP TRIGGER IF EXISTS update_playlists_updated_at ON playlists;
CREATE TRIGGER update_playlists_updated_at 
    BEFORE UPDATE ON playlists
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- PLAYLIST ITEMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS playlist_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    playlist_id UUID NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    position INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(playlist_id, content_type, content_id)
);

-- Playlist items table indexes
CREATE INDEX IF NOT EXISTS idx_playlist_items_playlist_id ON playlist_items(playlist_id);
CREATE INDEX IF NOT EXISTS idx_playlist_items_content ON playlist_items(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_playlist_items_position ON playlist_items(playlist_id, position);

-- ============================================
-- SUBSCRIPTIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    subscriber_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    channel_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(subscriber_id, channel_id),
    CHECK (subscriber_id != channel_id)
);

-- Subscriptions table indexes
CREATE INDEX IF NOT EXISTS idx_subscriptions_subscriber_id ON subscriptions(subscriber_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_channel_id ON subscriptions(channel_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_created_at ON subscriptions(created_at DESC);

-- ============================================
-- DOWNLOADS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS downloads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    download_url TEXT NOT NULL,
    file_size BIGINT,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'completed', 'failed')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Downloads table indexes
CREATE INDEX IF NOT EXISTS idx_downloads_user_id ON downloads(user_id);
CREATE INDEX IF NOT EXISTS idx_downloads_content ON downloads(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_downloads_status ON downloads(status);
CREATE INDEX IF NOT EXISTS idx_downloads_created_at ON downloads(created_at DESC);

-- Downloads trigger
DROP TRIGGER IF EXISTS update_downloads_updated_at ON downloads;
CREATE TRIGGER update_downloads_updated_at 
    BEFORE UPDATE ON downloads
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- VIEW HISTORY TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS view_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    watch_duration INTEGER DEFAULT 0,
    completed BOOLEAN DEFAULT false
);

-- View history table indexes
CREATE INDEX IF NOT EXISTS idx_view_history_user_id ON view_history(user_id);
CREATE INDEX IF NOT EXISTS idx_view_history_content ON view_history(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_view_history_watched_at ON view_history(watched_at DESC);

-- ============================================
-- ADD DISLIKES COLUMN TO EXISTING TABLES
-- ============================================
ALTER TABLE videos ADD COLUMN IF NOT EXISTS dislikes INTEGER DEFAULT 0;
ALTER TABLE videos ADD COLUMN IF NOT EXISTS is_trending BOOLEAN DEFAULT false;

-- Add indexes for new columns
CREATE INDEX IF NOT EXISTS idx_videos_dislikes ON videos(dislikes DESC);
CREATE INDEX IF NOT EXISTS idx_videos_trending ON videos(is_trending);
CREATE INDEX IF NOT EXISTS idx_videos_views ON videos(views DESC);

-- ============================================
-- HELPER FUNCTIONS FOR STATISTICS
-- ============================================

-- Function to get like/dislike counts for content
CREATE OR REPLACE FUNCTION get_content_likes(p_content_type content_type, p_content_id UUID)
RETURNS TABLE(likes BIGINT, dislikes BIGINT) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) FILTER (WHERE like_type = 'like') AS likes,
        COUNT(*) FILTER (WHERE like_type = 'dislike') AS dislikes
    FROM likes
    WHERE content_type = p_content_type AND content_id = p_content_id;
END;
$$ LANGUAGE plpgsql;

-- Function to get comment count for content
CREATE OR REPLACE FUNCTION get_content_comment_count(p_content_type content_type, p_content_id UUID)
RETURNS BIGINT AS $$
BEGIN
    RETURN (
        SELECT COUNT(*)
        FROM comments
        WHERE content_type = p_content_type AND content_id = p_content_id
    );
END;
$$ LANGUAGE plpgsql;

-- Function to get subscriber count for a channel
CREATE OR REPLACE FUNCTION get_subscriber_count(p_channel_id UUID)
RETURNS BIGINT AS $$
BEGIN
    RETURN (
        SELECT COUNT(*)
        FROM subscriptions
        WHERE channel_id = p_channel_id
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- MATERIALIZED VIEW FOR TRENDING CONTENT
-- ============================================

-- Trending videos view
CREATE MATERIALIZED VIEW IF NOT EXISTS trending_videos AS
SELECT 
    v.*,
    (v.views * 0.5 + v.likes * 2 - v.dislikes * 1.5) AS trending_score
FROM videos v
WHERE v.status = 'published'
  AND v.created_at > NOW() - INTERVAL '7 days'
ORDER BY trending_score DESC
LIMIT 100;

CREATE UNIQUE INDEX IF NOT EXISTS idx_trending_videos_id ON trending_videos(id);
CREATE INDEX IF NOT EXISTS idx_trending_videos_score ON trending_videos(trending_score DESC);

-- Trending bitz view
CREATE MATERIALIZED VIEW IF NOT EXISTS trending_bitz AS
SELECT 
    b.*,
    (b.views * 0.5 + COALESCE(l.like_count, 0) * 2 - COALESCE(l.dislike_count, 0) * 1.5) AS trending_score
FROM bitz b
LEFT JOIN (
    SELECT content_id, 
           COUNT(*) FILTER (WHERE like_type = 'like') AS like_count,
           COUNT(*) FILTER (WHERE like_type = 'dislike') AS dislike_count
    FROM likes
    WHERE content_type = 'bitz'
    GROUP BY content_id
) l ON b.id = l.content_id
WHERE b.status = 'published'
  AND b.created_at > NOW() - INTERVAL '7 days'
ORDER BY trending_score DESC
LIMIT 100;

CREATE UNIQUE INDEX IF NOT EXISTS idx_trending_bitz_id ON trending_bitz(id);
CREATE INDEX IF NOT EXISTS idx_trending_bitz_score ON trending_bitz(trending_score DESC);

-- Trending posts view
CREATE MATERIALIZED VIEW IF NOT EXISTS trending_posts AS
SELECT 
    p.*,
    (p.views * 0.5 + COALESCE(l.like_count, 0) * 2 - COALESCE(l.dislike_count, 0) * 1.5) AS trending_score
FROM posts p
LEFT JOIN (
    SELECT content_id, 
           COUNT(*) FILTER (WHERE like_type = 'like') AS like_count,
           COUNT(*) FILTER (WHERE like_type = 'dislike') AS dislike_count
    FROM likes
    WHERE content_type = 'post'
    GROUP BY content_id
) l ON p.id = l.content_id
WHERE p.status = 'published'
  AND p.created_at > NOW() - INTERVAL '7 days'
ORDER BY trending_score DESC
LIMIT 100;

CREATE UNIQUE INDEX IF NOT EXISTS idx_trending_posts_id ON trending_posts(id);
CREATE INDEX IF NOT EXISTS idx_trending_posts_score ON trending_posts(trending_score DESC);

-- Function to refresh all trending views
CREATE OR REPLACE FUNCTION refresh_trending_views()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY trending_videos;
    REFRESH MATERIALIZED VIEW CONCURRENTLY trending_bitz;
    REFRESH MATERIALIZED VIEW CONCURRENTLY trending_posts;
END;
$$ LANGUAGE plpgsql;
