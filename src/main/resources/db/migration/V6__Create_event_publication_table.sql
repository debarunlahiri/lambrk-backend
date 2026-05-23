-- Spring Modulith event publication table
CREATE TABLE IF NOT EXISTS event_publication (
    id UUID PRIMARY KEY,
    application_name VARCHAR(255) NOT NULL,
    module VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_id VARCHAR(255),
    published_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    correlation_key VARCHAR(255),
    sequence_number BIGINT DEFAULT 0,
    content VARCHAR(4000),
    published BOOLEAN DEFAULT FALSE
);