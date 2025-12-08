-- Video Qualities Table
-- Stores multiple quality versions of the same video (like YouTube)

CREATE TABLE IF NOT EXISTS video_qualities (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    video_id UUID NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    quality VARCHAR(20) NOT NULL CHECK (quality IN ('144p', '240p', '360p', '480p', '720p', '1080p', '1440p', '2160p', 'original')),
    url TEXT NOT NULL,
    file_size BIGINT,
    bitrate INTEGER,
    resolution_width INTEGER,
    resolution_height INTEGER,
    codec VARCHAR(50),
    container VARCHAR(20),
    duration INTEGER,
    is_default BOOLEAN DEFAULT false,
    status VARCHAR(20) DEFAULT 'processing' CHECK (status IN ('processing', 'ready', 'failed')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_video_qualities_video_id ON video_qualities(video_id);
CREATE INDEX IF NOT EXISTS idx_video_qualities_quality ON video_qualities(quality);
CREATE INDEX IF NOT EXISTS idx_video_qualities_status ON video_qualities(status);
CREATE INDEX IF NOT EXISTS idx_video_qualities_default ON video_qualities(video_id, is_default) WHERE is_default = true;

-- Unique constraint: one default quality per video
CREATE UNIQUE INDEX IF NOT EXISTS idx_video_qualities_one_default 
    ON video_qualities(video_id) 
    WHERE is_default = true;

-- Trigger for updated_at
DROP TRIGGER IF EXISTS update_video_qualities_updated_at ON video_qualities;
CREATE TRIGGER update_video_qualities_updated_at 
    BEFORE UPDATE ON video_qualities
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

