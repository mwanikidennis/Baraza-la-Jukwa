/*
  # JUKWA Complete Database Schema

  1. Purpose
    Single-command initialization of the entire JUKWA database.
    This file is mounted as `docker-entrypoint-initdb.d/init.sql` so
    PostgreSQL creates every table, index, extension, and materialized
    view on first `docker compose up`.

  2. Extensions
    - postgis  (spatial types and queries)
    - pgcrypto (gen_random_uuid, cryptographic functions)

  3. Core Tables
    - `wards`            — 1,450 Kenyan ward boundaries (PostGIS MultiPolygon)
    - `citizens`         — Pseudonymous citizen identities
    - `incidents`        — Citizen incident reports with PostGIS Point locations

  4. BARAZA Tables
    - `government_agencies`  — Structured directory of Kenyan government entities
    - `baraza_sessions`      — Physical JIM forums and citizen-captured events
    - `commitments`          — Government commitment tracking (core BARAZA entity)
    - `commitment_evidence`  — Evidence chain for commitments
    - `commitment_verifications` — Citizen verification votes

  5. Analytics & Sustainability
    - `agency_scorecards`        — Materialized view (refreshed hourly)
    - `contextual_promotions`    — Geofenced local business visibility
    - `civic_insights_aggregated` — Anonymized macro-data for GoK reporting

  6. Security
    - RLS is NOT enabled on these tables in the Docker dev environment.
      RLS policies will be applied when deploying to Supabase (production).
    - All UUIDs use gen_random_uuid() from pgcrypto.
    - All geometry uses SRID 4326 (WGS 84).
    - GiST indexes on all geometry columns for spatial query performance.

  7. Important Notes
    1) Tables must be created in dependency order (wards before citizens
       before incidents, government_agencies before commitments, etc.)
    2) The `wards` table is created first because it is referenced by
       nearly every other table via foreign key.
    3) Seed data (ward boundaries, agency directory) should be loaded
       AFTER this script runs, via separate seed scripts.
*/

-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- CORE TABLES
-- ============================================================

-- Ward boundaries (all 1,450 Kenyan wards)
CREATE TABLE IF NOT EXISTS wards (
    ward_id INTEGER PRIMARY KEY,
    ward_name VARCHAR(100) NOT NULL,
    county_name VARCHAR(50) NOT NULL,
    sub_county_name VARCHAR(100),
    boundary GEOMETRY(MultiPolygon, 4326) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_wards_boundary ON wards USING GIST (boundary);

-- Citizens (pseudonymous by default)
CREATE TABLE IF NOT EXISTS citizens (
    citizen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_token_hash VARCHAR(64) UNIQUE NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
    anonymity_preference VARCHAR(20) DEFAULT 'STANDARD',
    gamification_points INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Incident reports with full spatial indexing
CREATE TABLE IF NOT EXISTS incidents (
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

CREATE INDEX IF NOT EXISTS idx_incidents_location ON incidents USING GIST (location);
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents (status);
CREATE INDEX IF NOT EXISTS idx_incidents_ward ON incidents (ward_id, reported_at DESC);
CREATE INDEX IF NOT EXISTS idx_incidents_category ON incidents (incident_category, reported_at DESC);

-- ============================================================
-- BARAZA TABLES
-- ============================================================

-- Government Agency Directory
CREATE TABLE IF NOT EXISTS government_agencies (
    agency_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agency_name VARCHAR(200) NOT NULL,
    agency_type VARCHAR(50) NOT NULL,
    parent_agency_id UUID REFERENCES government_agencies(agency_id),
    contact_email VARCHAR(200),
    contact_phone VARCHAR(20),
    api_endpoint TEXT,
    default_sla_days JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Baraza Sessions (physical JIM forums + citizen-captured events)
CREATE TABLE IF NOT EXISTS baraza_sessions (
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

CREATE INDEX IF NOT EXISTS idx_baraza_sessions_ward ON baraza_sessions (ward_id);
CREATE INDEX IF NOT EXISTS idx_baraza_sessions_location ON baraza_sessions USING GIST (location);

-- Government Commitments (core BARAZA entity)
CREATE TABLE IF NOT EXISTS commitments (
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

CREATE INDEX IF NOT EXISTS idx_commitments_ward ON commitments (affected_ward_id, status);
CREATE INDEX IF NOT EXISTS idx_commitments_agency ON commitments (responsible_agency_id, status);
CREATE INDEX IF NOT EXISTS idx_commitments_status ON commitments (status, sla_deadline);
CREATE INDEX IF NOT EXISTS idx_commitments_location ON commitments USING GIST (affected_location);

-- Commitment Evidence Chain
CREATE TABLE IF NOT EXISTS commitment_evidence (
    evidence_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    evidence_type VARCHAR(30) NOT NULL,
    submitted_by_type VARCHAR(20) NOT NULL,
    submitted_by_id UUID,
    content TEXT,
    media_urls TEXT[],
    submitted_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_commitment_evidence_commitment ON commitment_evidence (commitment_id);

-- Citizen Verification Votes
CREATE TABLE IF NOT EXISTS commitment_verifications (
    verification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    citizen_id UUID REFERENCES citizens(citizen_id),
    vote VARCHAR(10) NOT NULL,
    evidence_media_url TEXT,
    comment TEXT,
    voted_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(commitment_id, citizen_id)
);

CREATE INDEX IF NOT EXISTS idx_commitment_verifications_commitment ON commitment_verifications (commitment_id);

-- ============================================================
-- ANALYTICS & SUSTAINABILITY
-- ============================================================

-- Accountability Scorecard (materialized, refreshed hourly)
CREATE MATERIALIZED VIEW IF NOT EXISTS agency_scorecards AS
SELECT
    a.agency_id,
    a.agency_name,
    COUNT(c.commitment_id) AS total_commitments,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'VERIFIED_RESOLVED') AS fulfilled,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'FAILED') AS failed,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'OVERDUE') AS overdue,
    COUNT(c.commitment_id) FILTER
        (WHERE c.status = 'SILENCE') AS silenced,
    ROUND(
        100.0 * COUNT(c.commitment_id)
            FILTER (WHERE c.status = 'VERIFIED_RESOLVED')
        / NULLIF(COUNT(c.commitment_id), 0), 1
    ) AS fulfillment_rate_pct,
    AVG(
        EXTRACT(EPOCH FROM (c.citizen_verified_at - c.created_at)) / 86400
    ) FILTER (WHERE c.status = 'VERIFIED_RESOLVED') AS avg_resolution_days
FROM government_agencies a
LEFT JOIN commitments c ON c.responsible_agency_id = a.agency_id
GROUP BY a.agency_id, a.agency_name;

CREATE INDEX IF NOT EXISTS idx_agency_scorecards_agency ON agency_scorecards (agency_id);

-- Contextual geofenced promotions (sustainability revenue)
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

-- Aggregated Civic Insights (anonymized macro-data for GoK reporting)
CREATE TABLE IF NOT EXISTS civic_insights_aggregated (
    insight_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ward_id INTEGER REFERENCES wards(ward_id),
    ward_name VARCHAR(100),
    issue_category VARCHAR(50),
    incident_count INTEGER DEFAULT 0,
    severity_heatmap GEOMETRY(MultiPoint, 4326),
    generated_for_gok_report BOOLEAN DEFAULT FALSE,
    time_window TSTZRANGE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_civic_insights_agg_ward ON civic_insights_aggregated (ward_id);
CREATE INDEX IF NOT EXISTS idx_civic_insights_agg_heatmap ON civic_insights_aggregated USING GIST (severity_heatmap);
