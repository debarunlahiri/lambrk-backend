-- ============================================================
-- V3: Add notifications, admin_actions, file_uploads tables
--     and missing columns for new features
-- ============================================================

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    related_post_id UUID,
    related_comment_id UUID,
    related_user_id UUID,
    action_url VARCHAR(500),
    action_text VARCHAR(100),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX idx_notification_recipient ON notifications(recipient_id);
CREATE INDEX idx_notification_type ON notifications(type);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
CREATE INDEX idx_notification_recipient_unread ON notifications(recipient_id, is_read) WHERE is_read = FALSE;

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Admin actions audit table
CREATE TABLE admin_actions (
    id UUID PRIMARY KEY,
    type VARCHAR(30) NOT NULL,
    target_id UUID NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    notes TEXT,
    performed_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    result VARCHAR(500)
);

CREATE INDEX idx_admin_action_type ON admin_actions(type);
CREATE INDEX idx_admin_action_target ON admin_actions(target_id);
CREATE INDEX idx_admin_action_performed_by ON admin_actions(performed_by);
CREATE INDEX idx_admin_action_created_at ON admin_actions(created_at);
CREATE INDEX idx_admin_action_is_active ON admin_actions(is_active);
CREATE INDEX idx_admin_action_expires_at ON admin_actions(expires_at) WHERE expires_at IS NOT NULL;

-- File uploads table
CREATE TABLE file_uploads (
    id UUID PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL UNIQUE,
    original_file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    type VARCHAR(30) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    description TEXT,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    is_nsfw BOOLEAN NOT NULL DEFAULT FALSE,
    alt_text VARCHAR(500),
    uploaded_by UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64) NOT NULL
);

CREATE INDEX idx_file_upload_type ON file_uploads(type);
CREATE INDEX idx_file_upload_uploaded_by ON file_uploads(uploaded_by);
CREATE INDEX idx_file_upload_uploaded_at ON file_uploads(uploaded_at);
CREATE INDEX idx_file_upload_is_public ON file_uploads(is_public);
CREATE INDEX idx_file_upload_is_nsfw ON file_uploads(is_nsfw);
CREATE INDEX idx_file_upload_checksum ON file_uploads(checksum);

CREATE TRIGGER update_file_uploads_updated_at BEFORE UPDATE ON file_uploads
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add role column to users table for RBAC
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
CREATE INDEX idx_user_role ON users(role);

-- Update seed users with roles
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
UPDATE users SET role = 'USER' WHERE username IN ('john_doe', 'jane_smith');

-- Add sample posts for search testing
INSERT INTO posts (id, title, content, post_type, author_id, community_id) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Getting Started with Spring Boot 3.5', 'Spring Boot 3.5 introduces virtual threads, structured concurrency, and many new features for modern Java development.', 'TEXT', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Virtual Threads in Production', 'After running virtual threads in production for 3 months, here are our findings and best practices.', 'TEXT', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Java 25 Pattern Matching Deep Dive', 'Pattern matching in Java 25 has reached its final form. Let us explore all the features.', 'TEXT', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Best Gaming Monitors 2026', 'A comprehensive guide to the best gaming monitors available this year.', 'TEXT', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'AI in Software Development', 'How AI is transforming the way we write, test, and deploy software.', 'TEXT', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13');

-- Add sample comments
INSERT INTO comments (id, content, author_id, post_id) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Great article! Virtual threads have been a game changer for our team.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Can you share some benchmarks comparing virtual threads vs platform threads?', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Pattern matching makes switch expressions so much cleaner.', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13');

-- Update post comment counts
UPDATE posts SET comment_count = (
    SELECT COUNT(*) FROM comments WHERE post_id = posts.id
);

-- Add sample votes
INSERT INTO votes (id, vote_type, user_id, post_id) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'), ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15'), ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'UPVOTE', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15');

-- Update post scores
UPDATE posts SET score = (
    SELECT COALESCE(SUM(CASE WHEN vote_type = 'UPVOTE' THEN 1 ELSE -1 END), 0)
    FROM votes WHERE post_id = posts.id
), like_count = (
    SELECT COUNT(*) FROM votes WHERE post_id = posts.id AND vote_type = 'UPVOTE'
), dislike_count = (
    SELECT COUNT(*) FROM votes WHERE post_id = posts.id AND vote_type = 'DOWNVOTE'
);

-- Update user karma
UPDATE users SET karma = (
    SELECT COALESCE(SUM(p.score), 0) FROM posts p WHERE p.author_id = users.id
) + (
    SELECT COALESCE(SUM(c.score), 0) FROM comments c WHERE c.author_id = users.id
);
