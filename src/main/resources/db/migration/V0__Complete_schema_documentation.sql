-- ============================================================
-- Database Schema Documentation
-- Reddit Backend Application - PostgreSQL
-- ============================================================

-- ============================================================
-- 1. USERS TABLE
-- ============================================================
-- Purpose: Stores registered user accounts with authentication
-- and profile information

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,           -- Unique login identifier
    email VARCHAR(100) UNIQUE NOT NULL,             -- Unique email for auth
    password VARCHAR(255) NOT NULL,                 -- BCrypt hashed password
    display_name VARCHAR(100),                      -- Public display name
    bio TEXT,                                       -- User bio/description
    avatar_url VARCHAR(500),                        -- Profile picture URL
    role VARCHAR(20) NOT NULL DEFAULT 'USER',       -- RBAC: USER, MODERATOR, ADMIN
    is_active BOOLEAN NOT NULL DEFAULT TRUE,        -- Account enabled status
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,     -- Email verified badge
    karma INTEGER NOT NULL DEFAULT 0,               -- Combined post+comment score
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Users
CREATE INDEX idx_user_username ON users(username);           -- Login lookups
CREATE INDEX idx_user_email ON users(email);               -- Email lookups
CREATE INDEX idx_user_created_at ON users(created_at);     -- Sorting/pagination
CREATE INDEX idx_user_role ON users(role);                 -- RBAC queries

-- ============================================================
-- 2. SUBREDDITS TABLE
-- ============================================================
-- Purpose: Communities/forums where users post content

CREATE TABLE subreddits (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(21) UNIQUE NOT NULL,                 -- URL-friendly name (r/name)
    title VARCHAR(100) NOT NULL,                    -- Display title
    description TEXT,                               -- Community description
    sidebar_text TEXT,                              -- Sidebar content (markdown)
    header_image_url VARCHAR(500),                  -- Banner image
    icon_image_url VARCHAR(500),                    -- Community icon
    is_public BOOLEAN NOT NULL DEFAULT TRUE,        -- Public vs private
    is_restricted BOOLEAN NOT NULL DEFAULT FALSE,   -- Posting restrictions
    is_over_18 BOOLEAN NOT NULL DEFAULT FALSE,      -- NSFW community
    member_count INTEGER NOT NULL DEFAULT 0,        -- Total members
    subscriber_count INTEGER NOT NULL DEFAULT 0,    -- Active subscribers
    active_user_count INTEGER NOT NULL DEFAULT 0,   -- Currently active
    created_by BIGINT NOT NULL REFERENCES users(id),-- Founder reference
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for Subreddits
CREATE INDEX idx_subreddit_name ON subreddits(name);
CREATE INDEX idx_subreddit_created_at ON subreddits(created_at);
CREATE INDEX idx_subreddit_member_count ON subreddits(member_count);
CREATE INDEX idx_subreddit_created_by ON subreddits(created_by);

-- ============================================================
-- 3. POSTS TABLE
-- ============================================================
-- Purpose: User-submitted content to subreddits

CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(300) NOT NULL,                    -- Post headline
    content TEXT,                                   -- Text body (for text posts)
    url VARCHAR(2000),                              -- External link (for link posts)
    post_type VARCHAR(10) NOT NULL DEFAULT 'TEXT',  -- TEXT, LINK, IMAGE, VIDEO
    thumbnail_url VARCHAR(500),                     -- Generated thumbnail
    flair_text VARCHAR(64),                         -- Post flair/tag
    flair_css_class VARCHAR(64),                    -- Flair styling
    is_spoiler BOOLEAN NOT NULL DEFAULT FALSE,      -- Spoiler content
    is_stickied BOOLEAN NOT NULL DEFAULT FALSE,     -- Pinned to top
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,       -- Comments disabled
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,     -- Read-only
    is_removed BOOLEAN NOT NULL DEFAULT FALSE,      -- Moderator removed
    is_over_18 BOOLEAN NOT NULL DEFAULT FALSE,      -- NSFW content
    score INTEGER NOT NULL DEFAULT 1,               -- Net votes (up - down)
    upvote_count INTEGER NOT NULL DEFAULT 1,        -- Upvote count
    downvote_count INTEGER NOT NULL DEFAULT 0,      -- Downvote count
    comment_count INTEGER NOT NULL DEFAULT 0,       -- Total comments
    view_count INTEGER NOT NULL DEFAULT 0,          -- Page views
    award_count INTEGER NOT NULL DEFAULT 0,         -- Awards received
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subreddit_id BIGINT NOT NULL REFERENCES subreddits(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP,                          -- When archived
    removed_at TIMESTAMP                            -- When removed
);

-- Indexes for Posts
CREATE INDEX idx_post_author ON posts(author_id);
CREATE INDEX idx_post_subreddit ON posts(subreddit_id);
CREATE INDEX idx_post_created_at ON posts(created_at);
CREATE INDEX idx_post_score ON posts(score);
CREATE INDEX idx_post_title ON posts(title);
CREATE INDEX idx_post_type ON posts(post_type);
CREATE INDEX idx_post_is_stickied ON posts(is_stickied);
CREATE INDEX idx_post_is_removed ON posts(is_removed);
CREATE INDEX idx_post_is_archived ON posts(is_archived);
-- Composite: Hot posts by subreddit
CREATE INDEX idx_post_subreddit_score ON posts(subreddit_id, score DESC);
-- Composite: New posts by subreddit
CREATE INDEX idx_post_subreddit_created ON posts(subreddit_id, created_at DESC);

-- ============================================================
-- 4. COMMENTS TABLE
-- ============================================================
-- Purpose: User replies to posts and other comments (nested)

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,                          -- Comment body
    flair_text VARCHAR(64),                         -- Comment flair
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,       -- Was modified
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,      -- User deleted
    is_removed BOOLEAN NOT NULL DEFAULT FALSE,      -- Mod removed
    is_collapsed BOOLEAN NOT NULL DEFAULT FALSE,    -- Hidden by default
    is_stickied BOOLEAN NOT NULL DEFAULT FALSE,     -- Pinned comment
    is_over_18 BOOLEAN NOT NULL DEFAULT FALSE,      -- NSFW comment
    score INTEGER NOT NULL DEFAULT 1,               -- Net votes
    upvote_count INTEGER NOT NULL DEFAULT 1,      -- Upvotes
    downvote_count INTEGER NOT NULL DEFAULT 0,    -- Downvotes
    reply_count INTEGER NOT NULL DEFAULT 0,         -- Direct replies
    award_count INTEGER NOT NULL DEFAULT 0,       -- Awards
    depth_level INTEGER NOT NULL DEFAULT 0,       -- Nesting depth (0=top)
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    parent_id BIGINT REFERENCES comments(id) ON DELETE CASCADE, -- For replies
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMP,                            -- When last edited
    deleted_at TIMESTAMP,                           -- When deleted
    removed_at TIMESTAMP                          -- When removed
);

-- Indexes for Comments
CREATE INDEX idx_comment_author ON comments(author_id);
CREATE INDEX idx_comment_post ON comments(post_id);
CREATE INDEX idx_comment_parent ON comments(parent_id);
CREATE INDEX idx_comment_created_at ON comments(created_at);
CREATE INDEX idx_comment_score ON comments(score);
CREATE INDEX idx_comment_depth ON comments(depth_level);
-- Composite: Top-level comments for a post
CREATE INDEX idx_comment_post_parent ON comments(post_id, parent_id) WHERE parent_id IS NULL;

-- ============================================================
-- 5. VOTES TABLE
-- ============================================================
-- Purpose: User upvotes/downvotes on posts and comments

CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    vote_type VARCHAR(10) NOT NULL CHECK (vote_type IN ('UPVOTE', 'DOWNVOTE')),
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),                         -- For rate limiting
    user_agent VARCHAR(500),                        -- For analytics
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- One vote per user per target
    CONSTRAINT unique_user_post_vote UNIQUE (user_id, post_id),
    CONSTRAINT unique_user_comment_vote UNIQUE (user_id, comment_id),
    -- Must vote on either post OR comment, not both
    CONSTRAINT vote_target_check CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL) OR 
        (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

-- Indexes for Votes
CREATE INDEX idx_vote_user ON votes(user_id);
CREATE INDEX idx_vote_post ON votes(post_id);
CREATE INDEX idx_vote_comment ON votes(comment_id);
CREATE INDEX idx_vote_created_at ON votes(created_at);
CREATE INDEX idx_vote_user_post ON votes(user_id, post_id);
CREATE INDEX idx_vote_user_comment ON votes(user_id, comment_id);

-- ============================================================
-- 6. USER-SUBREDDIT MEMBERSHIPS TABLE
-- ============================================================
-- Purpose: Many-to-many join table for users subscribed to subreddits

CREATE TABLE user_subreddit_memberships (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subreddit_id BIGINT NOT NULL REFERENCES subreddits(id) ON DELETE CASCADE,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, subreddit_id)
);

-- ============================================================
-- 7. USER-SUBREDDIT MODERATORS TABLE
-- ============================================================
-- Purpose: Users with moderation privileges for subreddits

CREATE TABLE user_subreddit_moderators (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subreddit_id BIGINT NOT NULL REFERENCES subreddits(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, subreddit_id)
);

-- ============================================================
-- 8. NOTIFICATIONS TABLE
-- ============================================================
-- Purpose: User notifications for replies, mentions, etc.

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(30) NOT NULL,                      -- COMMENT_REPLY, POST_MENTION, etc.
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,                    -- Notification headline
    message TEXT NOT NULL,                          -- Notification body
    related_post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    related_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    related_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    action_url VARCHAR(500),                        -- Link to action
    action_text VARCHAR(100),                       -- CTA button text
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

-- Indexes for Notifications
CREATE INDEX idx_notification_recipient ON notifications(recipient_id);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
-- Partial index: Unread notifications only (most common query)
CREATE INDEX idx_notification_recipient_unread ON notifications(recipient_id, is_read) WHERE is_read = FALSE;

-- ============================================================
-- 9. ADMIN ACTIONS TABLE
-- ============================================================
-- Purpose: Audit log for administrative actions

CREATE TABLE admin_actions (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(30) NOT NULL,                      -- BAN, UNBAN, REMOVE_POST, etc.
    target_id BIGINT NOT NULL,                      -- ID of affected entity
    target_type VARCHAR(50) NOT NULL,             -- USER, POST, COMMENT, SUBREDDIT
    reason TEXT NOT NULL,                           -- Why action was taken
    notes TEXT,                                     -- Additional details
    performed_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,                           -- For temporary actions
    is_active BOOLEAN NOT NULL DEFAULT TRUE,        -- Is action still in effect
    result VARCHAR(500)                             -- Outcome description
);

-- Indexes for Admin Actions
CREATE INDEX idx_admin_action_type ON admin_actions(type);
CREATE INDEX idx_admin_action_target ON admin_actions(target_id);
CREATE INDEX idx_admin_action_performed_by ON admin_actions(performed_by);
CREATE INDEX idx_admin_action_created_at ON admin_actions(created_at);
CREATE INDEX idx_admin_action_is_active ON admin_actions(is_active);
-- Partial index: Active actions only
CREATE INDEX idx_admin_action_expires_at ON admin_actions(expires_at) WHERE expires_at IS NOT NULL;

-- ============================================================
-- 10. FILE_UPLOADS TABLE
-- ============================================================
-- Purpose: Metadata for user-uploaded files (stored in S3)

CREATE TABLE file_uploads (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL UNIQUE,         -- Unique S3 key
    original_file_name VARCHAR(255) NOT NULL,       -- Original upload name
    file_url VARCHAR(500) NOT NULL,                 -- S3 URL or CDN path
    thumbnail_url VARCHAR(500),                     -- Generated thumbnail
    type VARCHAR(30) NOT NULL,                      -- AVATAR, POST_IMAGE, POST_VIDEO, etc.
    file_size BIGINT NOT NULL,                      -- Bytes
    mime_type VARCHAR(100) NOT NULL,                -- image/jpeg, video/mp4, etc.
    description TEXT,                               -- User description
    is_public BOOLEAN NOT NULL DEFAULT FALSE,       -- Public visibility
    is_nsfw BOOLEAN NOT NULL DEFAULT FALSE,         -- NSFW content flag
    alt_text VARCHAR(500),                          -- Accessibility text
    uploaded_by BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64) NOT NULL                     -- SHA-256 for integrity
);

-- Indexes for File Uploads
CREATE INDEX idx_file_upload_type ON file_uploads(type);
CREATE INDEX idx_file_upload_uploaded_by ON file_uploads(uploaded_by);
CREATE INDEX idx_file_upload_uploaded_at ON file_uploads(uploaded_at);
CREATE INDEX idx_file_upload_is_public ON file_uploads(is_public);
CREATE INDEX idx_file_upload_is_nsfw ON file_uploads(is_nsfw);
CREATE INDEX idx_file_upload_checksum ON file_uploads(checksum);
-- Composite: User's files by date
CREATE INDEX idx_file_upload_user_date ON file_uploads(uploaded_by, uploaded_at DESC);

-- ============================================================
-- 11. FREE_TIER_USAGE TABLE
-- ============================================================
-- Purpose: Track monthly free tier limits per user

CREATE TABLE free_tier_usage (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    period_year INTEGER NOT NULL,                   -- YYYY
    period_month INTEGER NOT NULL,                  -- MM (1-12)
    storage_bytes_used BIGINT NOT NULL DEFAULT 0,   -- Total storage this month
    uploads_count INTEGER NOT NULL DEFAULT 0,       -- Files uploaded
    bandwidth_bytes BIGINT NOT NULL DEFAULT 0,      -- Data transferred
    is_free_tier BOOLEAN NOT NULL DEFAULT TRUE,     -- Free vs paid tier
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_period UNIQUE (user_id, period_year, period_month)
);

-- Indexes for Free Tier Usage
CREATE INDEX idx_free_tier_user_id ON free_tier_usage(user_id);
CREATE INDEX idx_free_tier_period ON free_tier_usage(period_year, period_month);
CREATE INDEX idx_free_tier_user_period ON free_tier_usage(user_id, period_year, period_month);
CREATE INDEX idx_free_tier_created_at ON free_tier_usage(created_at);
CREATE INDEX idx_free_tier_is_free_tier ON free_tier_usage(is_free_tier);
-- Partial: Free tier users only (most queried)
CREATE INDEX idx_free_tier_free_users ON free_tier_usage(user_id, period_year, period_month) WHERE is_free_tier = TRUE;

-- ============================================================
-- AUTO-UPDATE TRIGGERS
-- ============================================================
-- Function to automatically update updated_at column

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables with updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_subreddits_updated_at BEFORE UPDATE ON subreddits
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_posts_updated_at BEFORE UPDATE ON posts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_comments_updated_at BEFORE UPDATE ON comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_votes_updated_at BEFORE UPDATE ON votes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_file_uploads_updated_at BEFORE UPDATE ON file_uploads
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_free_tier_usage_updated_at BEFORE UPDATE ON free_tier_usage
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- TABLE STATISTICS
-- ============================================================
-- Total tables: 11
-- Total indexes: ~50+
-- Total triggers: 8
-- Foreign key relationships: 20+
