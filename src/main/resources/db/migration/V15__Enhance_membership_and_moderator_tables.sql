-- ============================================================
-- V15: Enhance user_community_memberships and user_community_moderators
-- Add history tracking columns and proper entity support
-- ============================================================

-- -----------------------------------------------------------
-- user_community_memberships enhancements
-- -----------------------------------------------------------

-- Add new columns
ALTER TABLE user_community_memberships
    ADD COLUMN IF NOT EXISTS id UUID,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    ADD COLUMN IF NOT EXISTS left_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Generate UUIDs for existing rows
UPDATE user_community_memberships SET id = gen_random_uuid() WHERE id IS NULL;

-- Make id NOT NULL after population
ALTER TABLE user_community_memberships ALTER COLUMN id SET NOT NULL;

-- Drop old composite primary key and add new one on id
ALTER TABLE user_community_memberships DROP CONSTRAINT IF EXISTS user_community_memberships_pkey;
ALTER TABLE user_community_memberships ADD PRIMARY KEY (id);

-- Add unique constraint to prevent duplicate memberships
ALTER TABLE user_community_memberships ADD CONSTRAINT uniq_user_community_membership UNIQUE (user_id, community_id);

-- Add check constraints
ALTER TABLE user_community_memberships ADD CONSTRAINT chk_membership_status CHECK (status IN ('ACTIVE', 'LEFT', 'BANNED', 'PENDING'));
ALTER TABLE user_community_memberships ADD CONSTRAINT chk_membership_role CHECK (role IN ('MEMBER', 'CONTRIBUTOR'));

-- Add index on status for fast filtering
CREATE INDEX IF NOT EXISTS idx_membership_status ON user_community_memberships(status);
CREATE INDEX IF NOT EXISTS idx_membership_community ON user_community_memberships(community_id);

-- -----------------------------------------------------------
-- user_community_moderators enhancements
-- -----------------------------------------------------------

-- Add new columns
ALTER TABLE user_community_moderators
    ADD COLUMN IF NOT EXISTS id UUID,
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'MODERATOR',
    ADD COLUMN IF NOT EXISTS assigned_by UUID REFERENCES users(id),
    ADD COLUMN IF NOT EXISTS removed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Rename added_at to assigned_at for consistency
ALTER TABLE user_community_moderators RENAME COLUMN added_at TO assigned_at;

-- Generate UUIDs for existing rows
UPDATE user_community_moderators SET id = gen_random_uuid() WHERE id IS NULL;

-- Make id NOT NULL after population
ALTER TABLE user_community_moderators ALTER COLUMN id SET NOT NULL;

-- Drop old composite primary key and add new one on id
ALTER TABLE user_community_moderators DROP CONSTRAINT IF EXISTS user_community_moderators_pkey;
ALTER TABLE user_community_moderators ADD PRIMARY KEY (id);

-- Add unique constraint to prevent duplicate moderator assignments
ALTER TABLE user_community_moderators ADD CONSTRAINT uniq_user_community_moderator UNIQUE (user_id, community_id);

-- Add check constraints
ALTER TABLE user_community_moderators ADD CONSTRAINT chk_moderator_role CHECK (role IN ('MODERATOR', 'ADMIN', 'OWNER'));

-- Add index on is_active for fast filtering
CREATE INDEX IF NOT EXISTS idx_moderator_active ON user_community_moderators(is_active);
CREATE INDEX IF NOT EXISTS idx_moderator_community ON user_community_moderators(community_id);
