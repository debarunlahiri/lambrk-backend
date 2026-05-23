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

-- Seed categories
INSERT INTO categories (id, name, description, slug, sort_order, color, image_url) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Technology', 'All things tech, software, and hardware', 'technology', 1, '#2563EB', 'https://picsum.photos/seed/tech/400/200'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Gaming', 'Video games, consoles, and esports', 'gaming', 2, '#7C3AED', 'https://picsum.photos/seed/gaming/400/200'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Entertainment', 'Movies, TV shows, music, and pop culture', 'entertainment', 3, '#DB2777', 'https://picsum.photos/seed/entertainment/400/200'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'Science', 'Scientific discoveries, space, and research', 'science', 4, '#059669', 'https://picsum.photos/seed/science/400/200'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Lifestyle', 'Health, fitness, hobbies, and daily life', 'lifestyle', 5, '#D97706', 'https://picsum.photos/seed/lifestyle/400/200');

-- Assign communities to categories
INSERT INTO community_categories (community_id, category_id) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'), -- programming -> Technology
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12'), -- gaming -> Gaming
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'); -- technology -> Technology
