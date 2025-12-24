-- Lambrk Platform - Separate Interaction Tables
-- This migration creates separate tables for likes, dislikes, reports, and shares
-- Also migrates data from the combined likes table to separate tables

-- ============================================
-- MIGRATE EXISTING DATA (if likes table exists)
-- ============================================
-- First, we'll create temporary tables to hold the data during migration
DO $$ 
BEGIN
    -- Migrate likes data
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'likes') THEN
        CREATE TEMP TABLE IF NOT EXISTS temp_likes AS
        SELECT user_id, content_type, content_id, created_at, updated_at
        FROM likes
        WHERE like_type = 'like';
        
        CREATE TEMP TABLE IF NOT EXISTS temp_dislikes AS
        SELECT user_id, content_type, content_id, created_at, updated_at
        FROM likes
        WHERE like_type = 'dislike';
    END IF;
END $$;

-- ============================================
-- DROP EXISTING COMBINED LIKES TABLE
-- ============================================
DROP TABLE IF EXISTS likes CASCADE;

-- ============================================
-- LIKES TABLE (Separate)
-- ============================================
CREATE TABLE IF NOT EXISTS likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, content_type, content_id)
);

-- Likes table indexes
CREATE INDEX IF NOT EXISTS idx_likes_user_id ON likes(user_id);
CREATE INDEX IF NOT EXISTS idx_likes_content ON likes(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_likes_created_at ON likes(created_at DESC);

-- Likes trigger
DROP TRIGGER IF EXISTS update_likes_updated_at ON likes;
CREATE TRIGGER update_likes_updated_at 
    BEFORE UPDATE ON likes
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- DISLIKES TABLE (Separate)
-- ============================================
CREATE TABLE IF NOT EXISTS dislikes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, content_type, content_id)
);

-- Dislikes table indexes
CREATE INDEX IF NOT EXISTS idx_dislikes_user_id ON dislikes(user_id);
CREATE INDEX IF NOT EXISTS idx_dislikes_content ON dislikes(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_dislikes_created_at ON dislikes(created_at DESC);

-- Dislikes trigger
DROP TRIGGER IF EXISTS update_dislikes_updated_at ON dislikes;
CREATE TRIGGER update_dislikes_updated_at 
    BEFORE UPDATE ON dislikes
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- REPORTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    reason VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'resolved', 'dismissed')),
    reviewed_by UUID REFERENCES users(id) ON DELETE SET NULL,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, content_type, content_id)
);

-- Reports table indexes
CREATE INDEX IF NOT EXISTS idx_reports_user_id ON reports(user_id);
CREATE INDEX IF NOT EXISTS idx_reports_content ON reports(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_reports_status ON reports(status);
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reports_reviewed_by ON reports(reviewed_by);

-- Reports trigger
DROP TRIGGER IF EXISTS update_reports_updated_at ON reports;
CREATE TRIGGER update_reports_updated_at 
    BEFORE UPDATE ON reports
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SHARES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS shares (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content_type content_type NOT NULL,
    content_id UUID NOT NULL,
    platform VARCHAR(50),
    share_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shares table indexes
CREATE INDEX IF NOT EXISTS idx_shares_user_id ON shares(user_id);
CREATE INDEX IF NOT EXISTS idx_shares_content ON shares(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_shares_created_at ON shares(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_shares_platform ON shares(platform);

-- ============================================
-- RESTORE MIGRATED DATA
-- ============================================
DO $$ 
BEGIN
    -- Restore likes data
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'temp_likes') THEN
        INSERT INTO likes (user_id, content_type, content_id, created_at, updated_at)
        SELECT user_id, content_type, content_id, created_at, updated_at
        FROM temp_likes
        ON CONFLICT (user_id, content_type, content_id) DO NOTHING;
        
        DROP TABLE IF EXISTS temp_likes;
    END IF;
    
    -- Restore dislikes data
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'temp_dislikes') THEN
        INSERT INTO dislikes (user_id, content_type, content_id, created_at, updated_at)
        SELECT user_id, content_type, content_id, created_at, updated_at
        FROM temp_dislikes
        ON CONFLICT (user_id, content_type, content_id) DO NOTHING;
        
        DROP TABLE IF EXISTS temp_dislikes;
    END IF;
END $$;

-- ============================================
-- UPDATE HELPER FUNCTIONS
-- ============================================

-- Function to get like/dislike counts for content (updated for separate tables)
DROP FUNCTION IF EXISTS get_content_likes(content_type, UUID);
CREATE OR REPLACE FUNCTION get_content_likes(p_content_type content_type, p_content_id UUID)
RETURNS TABLE(likes BIGINT, dislikes BIGINT) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        (SELECT COUNT(*) FROM likes WHERE content_type = p_content_type AND content_id = p_content_id) AS likes,
        (SELECT COUNT(*) FROM dislikes WHERE content_type = p_content_type AND content_id = p_content_id) AS dislikes;
END;
$$ LANGUAGE plpgsql;

-- Function to get share count for content
CREATE OR REPLACE FUNCTION get_content_share_count(p_content_type content_type, p_content_id UUID)
RETURNS BIGINT AS $$
BEGIN
    RETURN (
        SELECT COUNT(*)
        FROM shares
        WHERE content_type = p_content_type AND content_id = p_content_id
    );
END;
$$ LANGUAGE plpgsql;

-- Function to get report count for content
CREATE OR REPLACE FUNCTION get_content_report_count(p_content_type content_type, p_content_id UUID)
RETURNS BIGINT AS $$
BEGIN
    RETURN (
        SELECT COUNT(*)
        FROM reports
        WHERE content_type = p_content_type AND content_id = p_content_id
    );
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- UPDATE TRENDING VIEWS (to use separate tables)
-- ============================================

-- Drop existing trending views
DROP MATERIALIZED VIEW IF EXISTS trending_videos CASCADE;
DROP MATERIALIZED VIEW IF EXISTS trending_bitz CASCADE;
DROP MATERIALIZED VIEW IF EXISTS trending_posts CASCADE;

-- Recreate trending videos view with separate likes/dislikes tables
CREATE MATERIALIZED VIEW IF NOT EXISTS trending_videos AS
SELECT 
    v.*,
    (v.views * 0.5 + COALESCE(l.like_count, 0) * 2 - COALESCE(d.dislike_count, 0) * 1.5) AS trending_score
FROM videos v
LEFT JOIN (
    SELECT content_id, COUNT(*) AS like_count
    FROM likes
    WHERE content_type = 'video'
    GROUP BY content_id
) l ON v.id = l.content_id
LEFT JOIN (
    SELECT content_id, COUNT(*) AS dislike_count
    FROM dislikes
    WHERE content_type = 'video'
    GROUP BY content_id
) d ON v.id = d.content_id
WHERE v.status = 'published'
  AND v.created_at > NOW() - INTERVAL '7 days'
ORDER BY trending_score DESC
LIMIT 100;

CREATE UNIQUE INDEX IF NOT EXISTS idx_trending_videos_id ON trending_videos(id);
CREATE INDEX IF NOT EXISTS idx_trending_videos_score ON trending_videos(trending_score DESC);

-- Recreate trending bitz view with separate likes/dislikes tables
CREATE MATERIALIZED VIEW IF NOT EXISTS trending_bitz AS
SELECT 
    b.*,
    (b.views * 0.5 + COALESCE(l.like_count, 0) * 2 - COALESCE(d.dislike_count, 0) * 1.5) AS trending_score
FROM bitz b
LEFT JOIN (
    SELECT content_id, COUNT(*) AS like_count
    FROM likes
    WHERE content_type = 'bitz'
    GROUP BY content_id
) l ON b.id = l.content_id
LEFT JOIN (
    SELECT content_id, COUNT(*) AS dislike_count
    FROM dislikes
    WHERE content_type = 'bitz'
    GROUP BY content_id
) d ON b.id = d.content_id
WHERE b.status = 'published'
  AND b.created_at > NOW() - INTERVAL '7 days'
ORDER BY trending_score DESC
LIMIT 100;

CREATE UNIQUE INDEX IF NOT EXISTS idx_trending_bitz_id ON trending_bitz(id);
CREATE INDEX IF NOT EXISTS idx_trending_bitz_score ON trending_bitz(trending_score DESC);

-- Recreate trending posts view with separate likes/dislikes tables
CREATE MATERIALIZED VIEW IF NOT EXISTS trending_posts AS
SELECT 
    p.*,
    (p.views * 0.5 + COALESCE(l.like_count, 0) * 2 - COALESCE(d.dislike_count, 0) * 1.5) AS trending_score
FROM posts p
LEFT JOIN (
    SELECT content_id, COUNT(*) AS like_count
    FROM likes
    WHERE content_type = 'post'
    GROUP BY content_id
) l ON p.id = l.content_id
LEFT JOIN (
    SELECT content_id, COUNT(*) AS dislike_count
    FROM dislikes
    WHERE content_type = 'post'
    GROUP BY content_id
) d ON p.id = d.content_id
WHERE p.status = 'published'
  AND p.created_at > NOW() - INTERVAL '7 days'
ORDER BY trending_score DESC
LIMIT 100;

CREATE UNIQUE INDEX IF NOT EXISTS idx_trending_posts_id ON trending_posts(id);
CREATE INDEX IF NOT EXISTS idx_trending_posts_score ON trending_posts(trending_score DESC);

