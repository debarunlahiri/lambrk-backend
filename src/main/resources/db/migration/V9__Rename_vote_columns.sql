-- Rename vote columns in posts and comments tables to match Java entity naming
-- This migration is safe to run even if columns were already renamed manually

DO $$
BEGIN
    -- Posts table
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'posts' AND column_name = 'upvote_count'
    ) THEN
        ALTER TABLE posts RENAME COLUMN upvote_count TO like_count;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'posts' AND column_name = 'downvote_count'
    ) THEN
        ALTER TABLE posts RENAME COLUMN downvote_count TO dislike_count;
    END IF;

    -- Comments table
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'comments' AND column_name = 'upvote_count'
    ) THEN
        ALTER TABLE comments RENAME COLUMN upvote_count TO like_count;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'comments' AND column_name = 'downvote_count'
    ) THEN
        ALTER TABLE comments RENAME COLUMN downvote_count TO dislike_count;
    END IF;
END $$;
