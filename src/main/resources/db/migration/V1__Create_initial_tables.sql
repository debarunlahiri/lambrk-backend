-- Create Users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    bio TEXT,
    avatar_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    karma INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Communities table
CREATE TABLE communities (
    id UUID PRIMARY KEY,
    name VARCHAR(21) UNIQUE NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    sidebar_text TEXT,
    header_image_url VARCHAR(500),
    icon_image_url VARCHAR(500),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    is_over_18 BOOLEAN NOT NULL DEFAULT FALSE,
    member_count INTEGER NOT NULL DEFAULT 0,
    subscriber_count INTEGER NOT NULL DEFAULT 0,
    active_user_count INTEGER NOT NULL DEFAULT 0,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Posts table
CREATE TABLE posts (
    id UUID PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    content TEXT,
    url VARCHAR(2000),
    post_type VARCHAR(10) NOT NULL DEFAULT 'TEXT',
    thumbnail_url VARCHAR(500),
    flair_text VARCHAR(64),
    flair_css_class VARCHAR(64),
    is_spoiler BOOLEAN NOT NULL DEFAULT FALSE,
    is_stickied BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    is_over_18 BOOLEAN NOT NULL DEFAULT FALSE,
    score INTEGER NOT NULL DEFAULT 1,
    like_count INTEGER NOT NULL DEFAULT 1,
    dislike_count INTEGER NOT NULL DEFAULT 0,
    comment_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    award_count INTEGER NOT NULL DEFAULT 0,
    author_id UUID NOT NULL REFERENCES users(id),
    community_id UUID NOT NULL REFERENCES communities(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_at TIMESTAMP
);

-- Create Comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    flair_text VARCHAR(64),
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    is_removed BOOLEAN NOT NULL DEFAULT FALSE,
    is_collapsed BOOLEAN NOT NULL DEFAULT FALSE,
    is_stickied BOOLEAN NOT NULL DEFAULT FALSE,
    is_over_18 BOOLEAN NOT NULL DEFAULT FALSE,
    score INTEGER NOT NULL DEFAULT 1,
    like_count INTEGER NOT NULL DEFAULT 1,
    dislike_count INTEGER NOT NULL DEFAULT 0,
    reply_count INTEGER NOT NULL DEFAULT 0,
    award_count INTEGER NOT NULL DEFAULT 0,
    depth_level INTEGER NOT NULL DEFAULT 0,
    author_id UUID NOT NULL REFERENCES users(id),
    post_id UUID NOT NULL REFERENCES posts(id),
    parent_id UUID REFERENCES comments(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    edited_at TIMESTAMP,
    deleted_at TIMESTAMP,
    removed_at TIMESTAMP
);

-- Create Votes table
CREATE TABLE votes (
    id UUID PRIMARY KEY,
    vote_type VARCHAR(10) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    post_id UUID REFERENCES posts(id),
    comment_id UUID REFERENCES comments(id),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_post_vote UNIQUE (user_id, post_id),
    CONSTRAINT unique_user_comment_vote UNIQUE (user_id, comment_id),
    CONSTRAINT vote_target_check CHECK (
        (post_id IS NOT NULL AND comment_id IS NULL) OR 
        (post_id IS NULL AND comment_id IS NOT NULL)
    )
);

-- Create User-Community membership table
CREATE TABLE user_community_memberships (
    user_id UUID NOT NULL REFERENCES users(id),
    community_id UUID NOT NULL REFERENCES communities(id),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, community_id)
);

-- Create User-Community moderator table
CREATE TABLE user_community_moderators (
    user_id UUID NOT NULL REFERENCES users(id),
    community_id UUID NOT NULL REFERENCES communities(id),
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, community_id)
);

-- Create indexes for better performance
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_created_at ON users(created_at);

CREATE INDEX idx_community_name ON communities(name);
CREATE INDEX idx_community_created_at ON communities(created_at);
CREATE INDEX idx_community_member_count ON communities(member_count);

CREATE INDEX idx_post_author ON posts(author_id);
CREATE INDEX idx_post_community ON posts(community_id);
CREATE INDEX idx_post_created_at ON posts(created_at);
CREATE INDEX idx_post_score ON posts(score);
CREATE INDEX idx_post_title ON posts(title);

CREATE INDEX idx_comment_author ON comments(author_id);
CREATE INDEX idx_comment_post ON comments(post_id);
CREATE INDEX idx_comment_parent ON comments(parent_id);
CREATE INDEX idx_comment_created_at ON comments(created_at);
CREATE INDEX idx_comment_score ON comments(score);

CREATE INDEX idx_vote_user ON votes(user_id);
CREATE INDEX idx_vote_post ON votes(post_id);
CREATE INDEX idx_vote_comment ON votes(comment_id);
CREATE INDEX idx_vote_created_at ON votes(created_at);
CREATE INDEX idx_vote_user_post ON votes(user_id, post_id);
CREATE INDEX idx_vote_user_comment ON votes(user_id, comment_id);

-- Create triggers for updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_communities_updated_at BEFORE UPDATE ON communities
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_posts_updated_at BEFORE UPDATE ON posts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_comments_updated_at BEFORE UPDATE ON comments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_votes_updated_at BEFORE UPDATE ON votes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data
INSERT INTO users (id, username, email, password, display_name) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin', 'admin@lambrk.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Admin User'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'john_doe', 'john@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'John Doe'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'jane_smith', 'jane@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'Jane Smith');

INSERT INTO communities (id, name, title, description, created_by) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'programming', 'Programming', 'All things programming and software development', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'gaming', 'Gaming', 'Discussions about video games', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'technology', 'Technology', 'Latest tech news and discussions', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');

INSERT INTO user_community_moderators (user_id, community_id) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'), ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13');

INSERT INTO user_community_memberships (user_id, community_id) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'), ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13');

-- Update community member counts
UPDATE communities SET member_count = (
    SELECT COUNT(*) FROM user_community_memberships WHERE community_id = communities.id
);

UPDATE communities SET subscriber_count = member_count;
