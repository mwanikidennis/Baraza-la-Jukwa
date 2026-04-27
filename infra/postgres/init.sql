CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Citizens (pseudonymous by default)
CREATE TABLE citizens (
    citizen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_token_hash VARCHAR(64) UNIQUE NOT NULL,
    ward_id INTEGER,
    anonymity_preference VARCHAR(20) DEFAULT 'STANDARD',
    gamification_points INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Ward boundaries (all 1,450 Kenyan wards)
CREATE TABLE wards (
    ward_id INTEGER PRIMARY KEY,
    ward_name VARCHAR(100) NOT NULL,
    county_name VARCHAR(50) NOT NULL,
    sub_county_name VARCHAR(100),
    boundary GEOMETRY(MultiPolygon, 4326) NOT NULL
);
CREATE INDEX idx_wards_boundary ON wards USING GIST (boundary);

-- Add foreign key back to wards for citizens
ALTER TABLE citizens ADD CONSTRAINT fk_citizen_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id);

-- Incident reports
CREATE TABLE incidents (
    incident_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES citizens(citizen_id),
    incident_category VARCHAR(50) NOT NULL,
    severity_score SMALLINT CHECK (severity_score BETWEEN 1 AND 5),
    location GEOMETRY(Point, 4326) NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
    description TEXT,
    media_urls TEXT[],
    anonymity_mode VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'SUBMITTED',
    routed_agencies TEXT[],
    reported_at TIMESTAMPTZ DEFAULT NOW(),
    acknowledged_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ
);
CREATE INDEX idx_incidents_location ON incidents USING GIST (location);
CREATE INDEX idx_incidents_ward ON incidents (ward_id, reported_at DESC);
CREATE INDEX idx_incidents_status ON incidents (status);

-- Government agencies directory
CREATE TABLE government_agencies (
    agency_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agency_name VARCHAR(200) NOT NULL,
    agency_type VARCHAR(50) NOT NULL,
    parent_agency_id UUID REFERENCES government_agencies(agency_id),
    contact_email VARCHAR(200),
    contact_phone VARCHAR(20),
    default_sla_days JSONB
);

-- Baraza sessions (physical forums)
CREATE TABLE baraza_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_type VARCHAR(30) NOT NULL,
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

-- BARAZA: Government commitments
CREATE TABLE commitments (
    commitment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_type VARCHAR(30) NOT NULL,
    baraza_session_id UUID REFERENCES baraza_sessions(session_id),
    originating_citizen_id UUID REFERENCES citizens(citizen_id),
    jim_ticket_id VARCHAR(50),
    sector VARCHAR(50) NOT NULL,
    promise_summary TEXT NOT NULL,
    fulfillment_criteria TEXT,
    affected_ward_id INTEGER REFERENCES wards(ward_id),
    affected_location GEOMETRY(Point, 4326),
    affected_facility_name VARCHAR(200),
    responsible_agency_id UUID REFERENCES government_agencies(agency_id),
    responsible_official_name VARCHAR(200),
    responsible_official_designation VARCHAR(200),
    status VARCHAR(30) DEFAULT 'CAPTURED',
    sla_deadline TIMESTAMPTZ,
    explicit_deadline TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    fulfilled_claimed_at TIMESTAMPTZ,
    citizen_verified_at TIMESTAMPTZ,
    escalation_level SMALLINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_commitments_ward ON commitments (affected_ward_id, status);
CREATE INDEX idx_commitments_agency ON commitments (responsible_agency_id, status);
CREATE INDEX idx_commitments_location ON commitments USING GIST (affected_location);

-- Commitment evidence chain
CREATE TABLE commitment_evidence (
    evidence_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    evidence_type VARCHAR(30) NOT NULL,
    submitted_by_type VARCHAR(20) NOT NULL,
    submitted_by_id UUID,
    content TEXT,
    media_urls TEXT[],
    submitted_at TIMESTAMPTZ DEFAULT NOW()
);

-- Citizen verification votes
CREATE TABLE commitment_verifications (
    verification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    citizen_id UUID REFERENCES citizens(citizen_id),
    vote VARCHAR(10) NOT NULL,
    evidence_media_url TEXT,
    comment TEXT,
    voted_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(commitment_id, citizen_id)
);

-- Civic insights (anonymized aggregates)
CREATE TABLE civic_insights (
    insight_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ward_id INTEGER REFERENCES wards(ward_id),
    issue_category VARCHAR(50) NOT NULL,
    incident_count INTEGER NOT NULL,
    avg_severity NUMERIC(3,2),
    avg_resolution_hours NUMERIC(8,2),
    heatmap_centroid GEOMETRY(Point, 4326),
    time_window TSTZRANGE NOT NULL,
    generated_at TIMESTAMPTZ DEFAULT NOW()
);
