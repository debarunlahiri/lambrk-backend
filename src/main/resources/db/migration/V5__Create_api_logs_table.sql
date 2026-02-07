-- ============================================================
-- V5: Create api_logs table for comprehensive request/response logging
-- ============================================================

-- API Logs table for request/response logging
CREATE TABLE api_logs (
    id BIGSERIAL PRIMARY KEY,
    correlation_id VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    log_level VARCHAR(20) NOT NULL DEFAULT 'INFO',
    method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    full_url VARCHAR(2000),
    query_string VARCHAR(2000),
    request_headers TEXT,
    request_body TEXT,
    response_headers TEXT,
    response_body TEXT,
    status_code INTEGER,
    response_time_ms BIGINT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    user_id BIGINT,
    username VARCHAR(100),
    is_authenticated BOOLEAN NOT NULL DEFAULT FALSE,
    exception_message VARCHAR(1000),
    exception_stack_trace TEXT,
    source VARCHAR(50) DEFAULT 'API',
    service_name VARCHAR(100) DEFAULT 'lambrk-backend',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for api_logs queries
CREATE INDEX idx_api_logs_timestamp ON api_logs(timestamp);
CREATE INDEX idx_api_logs_user_id ON api_logs(user_id);
CREATE INDEX idx_api_logs_method ON api_logs(method);
CREATE INDEX idx_api_logs_endpoint ON api_logs(endpoint);
CREATE INDEX idx_api_logs_status_code ON api_logs(status_code);
CREATE INDEX idx_api_logs_ip_address ON api_logs(ip_address);
CREATE INDEX idx_api_logs_log_level ON api_logs(log_level);
CREATE INDEX idx_api_logs_correlation_id ON api_logs(correlation_id);
-- Composite: Query by timestamp range and endpoint
CREATE INDEX idx_api_logs_timestamp_endpoint ON api_logs(timestamp, endpoint);
-- Partial: Only error logs (status >= 400)
CREATE INDEX idx_api_logs_errors ON api_logs(status_code, timestamp) WHERE status_code >= 400;
-- Partial: Only authenticated requests
CREATE INDEX idx_api_logs_authenticated ON api_logs(user_id, timestamp) WHERE is_authenticated = TRUE;
-- Partial: Anonymous requests only
CREATE INDEX idx_api_logs_anonymous ON api_logs(timestamp) WHERE is_authenticated = FALSE;

-- Partitioning for large log tables (optional - for high traffic)
-- Consider partitioning by timestamp (monthly) if you expect millions of logs per month
-- CREATE TABLE api_logs_2024_01 PARTITION OF api_logs
--     FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
