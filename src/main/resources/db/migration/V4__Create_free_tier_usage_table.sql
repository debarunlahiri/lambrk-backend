-- ============================================================
-- V4: Create free_tier_usage table for tracking user storage,
--     upload, and bandwidth limits
-- ============================================================

-- Free tier usage tracking table
CREATE TABLE free_tier_usage (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    period_year INTEGER NOT NULL,
    period_month INTEGER NOT NULL,
    storage_bytes_used BIGINT NOT NULL DEFAULT 0,
    uploads_count INTEGER NOT NULL DEFAULT 0,
    bandwidth_bytes BIGINT NOT NULL DEFAULT 0,
    is_free_tier BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_period UNIQUE (user_id, period_year, period_month)
);

-- Indexes for free tier usage queries
CREATE INDEX idx_free_tier_user_id ON free_tier_usage(user_id);
CREATE INDEX idx_free_tier_period ON free_tier_usage(period_year, period_month);
CREATE INDEX idx_free_tier_user_period ON free_tier_usage(user_id, period_year, period_month);
CREATE INDEX idx_free_tier_created_at ON free_tier_usage(created_at);
CREATE INDEX idx_free_tier_is_free_tier ON free_tier_usage(is_free_tier);

-- Trigger for updated_at timestamp
CREATE TRIGGER update_free_tier_usage_updated_at BEFORE UPDATE ON free_tier_usage
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
