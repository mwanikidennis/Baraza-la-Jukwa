-- BARAZA: Baraza Sessions (physical JIM forums + citizen-captured events)
-- Target: infra/postgres/migrations/003_baraza_sessions.sql

CREATE TABLE IF NOT EXISTS baraza_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_type VARCHAR(30) NOT NULL,
        -- 'JIM_OFFICIAL', 'COUNTY_FORUM', 'WARD_BARAZA', 'CITIZEN_CAPTURED'
    county_name VARCHAR(50) NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
    location GEOMETRY(Point, 4326),
    venue_name VARCHAR(200),
    session_date DATE NOT NULL,
    attending_officials JSONB,
    audio_recording_url TEXT,
    total_commitments_captured INTEGER DEFAULT 0,
    digitizer_citizen_id UUID REFERENCES citizens(citizen_id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_baraza_sessions_ward ON baraza_sessions (ward_id);
CREATE INDEX IF NOT EXISTS idx_baraza_sessions_location ON baraza_sessions USING GIST (location);
