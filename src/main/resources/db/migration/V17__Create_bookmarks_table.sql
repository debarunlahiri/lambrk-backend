CREATE TABLE IF NOT EXISTS bookmarks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    post_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookmark_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_bookmark_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT uk_bookmark_user_post UNIQUE (user_id, post_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmark_user ON bookmarks (user_id);
CREATE INDEX IF NOT EXISTS idx_bookmark_post ON bookmarks (post_id);
CREATE INDEX IF NOT EXISTS idx_bookmark_created_at ON bookmarks (created_at);
