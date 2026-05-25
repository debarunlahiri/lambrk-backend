-- ============================================================-- V7: Create categories table and community_categories join table-- ============================================================

-- Categories table
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    image_url VARCHAR(500),
    color VARCHAR(7),
    slug VARCHAR(50) UNIQUE NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Community-Category join table (many-to-many)
CREATE TABLE community_categories (
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    PRIMARY KEY (community_id, category_id)
);

-- Indexes
CREATE INDEX idx_category_name ON categories(name);
CREATE INDEX idx_category_slug ON categories(slug);
CREATE INDEX idx_category_sort_order ON categories(sort_order);
CREATE INDEX idx_community_categories_community ON community_categories(community_id);
CREATE INDEX idx_community_categories_category ON community_categories(category_id);

-- Trigger
CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Seed categories (images from Unsplash)
INSERT INTO categories (id, name, description, slug, sort_order, color, image_url) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Technology', 'All things tech, software, hardware, AI, and the digital world', 'technology', 1, '#2563EB', 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Gaming', 'Video games, consoles, esports, and gaming culture', 'gaming', 2, '#7C3AED', 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Entertainment', 'Movies, TV shows, music, and pop culture', 'entertainment', 3, '#DB2777', 'https://images.unsplash.com/photo-1522869635100-9f4c5e86aa37?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Science', 'Scientific discoveries, space exploration, and research breakthroughs', 'science', 4, '#059669', 'https://images.unsplash.com/photo-1507413245164-6160d8298b31?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Lifestyle', 'Health, fitness, food, travel, and daily inspiration', 'lifestyle', 5, '#D97706', 'https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'Sports', 'Football, basketball, cricket, and all major sports', 'sports', 6, '#DC2626', 'https://images.unsplash.com/photo-1461896836934-bf14e58a4583?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'Business', 'Startups, finance, markets, entrepreneurship, and economy', 'business', 7, '#0F766E', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'Art & Design', 'Digital art, painting, photography, and creative expression', 'art-design', 8, '#E11D48', 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', 'Music', 'Bands, genres, instruments, concerts, and audio production', 'music', 9, '#9333EA', 'https://images.unsplash.com/photo-1511735111819-9a3f7709049c?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', 'Food & Cooking', 'Recipes, restaurants, cuisines, and culinary adventures', 'food-cooking', 10, '#F97316', 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a21', 'History', 'Ancient civilizations, wars, people, and historical events', 'history', 11, '#92400E', 'https://images.unsplash.com/photo-1461360228754-6e81c478b882?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Nature & Wildlife', 'Animals, plants, conservation, and the natural world', 'nature-wildlife', 12, '#15803D', 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a23', 'Travel', 'Destinations, cultures, adventures, and travel tips', 'travel', 13, '#0891B2', 'https://images.unsplash.com/photo-1488646953014-85cb44e25828?w=800&h=400&fit=crop'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a24', 'Education', 'Learning resources, schools, online courses, and study tips', 'education', 14, '#CA8A04', 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800&h=400&fit=crop');

-- Assign communities to categories
INSERT INTO community_categories (community_id, category_id) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), -- programming -> Technology
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a17'), -- programming -> Business
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'), -- gaming -> Gaming
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'); -- technology -> Technology
