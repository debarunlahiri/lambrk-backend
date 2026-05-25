-- Fix event_publication table to match Spring Modulith JPA entity expectations
-- Drop the old incompatible table and recreate with correct schema

DROP TABLE IF EXISTS event_publication;

CREATE TABLE event_publication (
    id UUID PRIMARY KEY,
    listener_id VARCHAR(255),
    event_type VARCHAR(255) NOT NULL,
    serialized_event TEXT,
    publication_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_date TIMESTAMP
);
