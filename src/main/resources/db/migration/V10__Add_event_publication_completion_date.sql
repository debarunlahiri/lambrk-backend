-- Add missing completion_date column to event_publication table for Hibernate validation
ALTER TABLE event_publication ADD COLUMN IF NOT EXISTS completion_date TIMESTAMP;
