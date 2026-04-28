-- BARAZA: Aggregated Civic Insights (Knowledge is Power)
-- This strips all individual reports into macro-data to present to GoK.
-- Target: infra/postgres/migrations/008_civic_insights_aggregated.sql

CREATE TABLE IF NOT EXISTS civic_insights_aggregated (
    insight_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ward_id INTEGER REFERENCES wards(ward_id),
    ward_name VARCHAR(100),
    issue_category VARCHAR(50), -- e.g., 'INFRASTRUCTURE_DECAY', 'POLICE_EXTORTION'
    incident_count INTEGER DEFAULT 0,
    severity_heatmap GEOMETRY(MultiPoint, 4326),
    generated_for_gok_report BOOLEAN DEFAULT FALSE,
    time_window TSTZRANGE NOT NULL, -- The week or month the data represents
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_civic_insights_agg_ward ON civic_insights_aggregated (ward_id);
CREATE INDEX IF NOT EXISTS idx_civic_insights_agg_heatmap ON civic_insights_aggregated USING GIST (severity_heatmap);
