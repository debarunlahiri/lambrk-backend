-- User follows are directional: follower -> following.
CREATE TABLE user_follows (
    id UUID PRIMARY KEY,
    follower_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    source VARCHAR(50),
    last_interaction_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMP,
    CONSTRAINT user_follow_no_self CHECK (follower_id <> following_id),
    CONSTRAINT unique_user_follow_pair UNIQUE (follower_id, following_id),
    CONSTRAINT user_follow_status_check CHECK (status IN ('ACTIVE', 'REMOVED'))
);

-- Friendships are undirected but preserve requester/addressee and every state transition.
-- user_one_id/user_two_id store the canonical pair, requester_id/addressee_id store the active request direction.
CREATE TABLE user_friendships (
    id UUID PRIMARY KEY,
    user_one_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_two_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    addressee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_action_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source VARCHAR(50),
    request_message VARCHAR(280),
    accepted_at TIMESTAMP,
    responded_at TIMESTAMP,
    removed_at TIMESTAMP,
    blocked_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_friendship_no_self CHECK (user_one_id <> user_two_id),
    CONSTRAINT user_friendship_request_no_self CHECK (requester_id <> addressee_id),
    CONSTRAINT unique_user_friendship_pair UNIQUE (user_one_id, user_two_id),
    CONSTRAINT user_friendship_status_check CHECK (
        status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED', 'REMOVED', 'BLOCKED')
    )
);

CREATE INDEX idx_user_follow_follower ON user_follows(follower_id);
CREATE INDEX idx_user_follow_following ON user_follows(following_id);
CREATE INDEX idx_user_follow_status ON user_follows(status);
CREATE INDEX idx_user_follow_created_at ON user_follows(created_at);
CREATE INDEX idx_user_follow_active_followers ON user_follows(following_id, created_at DESC) WHERE status = 'ACTIVE';
CREATE INDEX idx_user_follow_active_following ON user_follows(follower_id, created_at DESC) WHERE status = 'ACTIVE';

CREATE INDEX idx_user_friendship_user_one ON user_friendships(user_one_id);
CREATE INDEX idx_user_friendship_user_two ON user_friendships(user_two_id);
CREATE INDEX idx_user_friendship_requester ON user_friendships(requester_id);
CREATE INDEX idx_user_friendship_addressee ON user_friendships(addressee_id);
CREATE INDEX idx_user_friendship_status ON user_friendships(status);
CREATE INDEX idx_user_friendship_incoming_pending ON user_friendships(addressee_id, created_at DESC) WHERE status = 'PENDING';
CREATE INDEX idx_user_friendship_outgoing_pending ON user_friendships(requester_id, created_at DESC) WHERE status = 'PENDING';
CREATE INDEX idx_user_friendship_accepted_one ON user_friendships(user_one_id) WHERE status = 'ACCEPTED';
CREATE INDEX idx_user_friendship_accepted_two ON user_friendships(user_two_id) WHERE status = 'ACCEPTED';

CREATE TRIGGER update_user_follows_updated_at BEFORE UPDATE ON user_follows
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_friendships_updated_at BEFORE UPDATE ON user_friendships
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
