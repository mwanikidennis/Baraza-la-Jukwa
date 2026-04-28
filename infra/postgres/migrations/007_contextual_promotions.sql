-- Contextual geofenced promotions (sustainability revenue)
-- Target: infra/postgres/migrations/007_contextual_promotions.sql

CREATE TABLE IF NOT EXISTS contextual_promotions (
    promo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_name VARCHAR(100) NOT NULL,
    street_target VARCHAR(100),
    geofence GEOMETRY(Polygon, 4326) NOT NULL,
    display_content JSONB NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    impressions INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_promos_geofence ON contextual_promotions USING GIST (geofence);
