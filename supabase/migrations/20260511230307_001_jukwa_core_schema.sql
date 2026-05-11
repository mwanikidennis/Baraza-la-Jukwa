/*
  # JUKWA Core Database Schema

  1. Purpose
    Initialize the complete JUKWA database on Supabase with PostGIS
    spatial support, all core tables, BARAZA accountability tables,
    and analytics/sustainability tables.

  2. Extensions
    - postgis  (spatial types and queries)
    - pgcrypto (gen_random_uuid, cryptographic functions)

  3. Core Tables
    - `wards`            — Kenyan ward boundaries (PostGIS MultiPolygon)
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
    - RLS enabled on ALL tables
    - Policies restrict data access to authenticated users
    - Citizens can only read/update their own data
    - Public read access on wards and agency scorecards

  7. Important Notes
    1) Tables created in dependency order
    2) All geometry uses SRID 4326 (WGS 84)
    3) GiST indexes on all geometry columns
*/

-- Extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Core: Wards
CREATE TABLE IF NOT EXISTS wards (
    ward_id INTEGER PRIMARY KEY,
    ward_name VARCHAR(100) NOT NULL,
    county_name VARCHAR(50) NOT NULL,
    sub_county_name VARCHAR(100),
    boundary GEOMETRY(MultiPolygon, 4326) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_wards_boundary ON wards USING GIST (boundary);

ALTER TABLE wards ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Wards are publicly readable"
  ON wards FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Wards are publicly readable anon"
  ON wards FOR SELECT
  TO anon
  USING (true);

-- Core: Citizens
CREATE TABLE IF NOT EXISTS citizens (
    citizen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_token_hash VARCHAR(64) UNIQUE NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
    anonymity_preference VARCHAR(20) DEFAULT 'STANDARD',
    gamification_points INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE citizens ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Citizens can read own data"
  ON citizens FOR SELECT
  TO authenticated
  USING (auth.uid() = citizen_id);

CREATE POLICY "Citizens can update own data"
  ON citizens FOR UPDATE
  TO authenticated
  USING (auth.uid() = citizen_id)
  WITH CHECK (auth.uid() = citizen_id);

-- Core: Incidents
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

ALTER TABLE incidents ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read incidents"
  ON incidents FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Authenticated users can create incidents"
  ON incidents FOR INSERT
  TO authenticated
  WITH CHECK (true);

CREATE POLICY "Citizens can update own incidents"
  ON incidents FOR UPDATE
  TO authenticated
  USING (reporter_id = auth.uid())
  WITH CHECK (reporter_id = auth.uid());

-- BARAZA: Government Agencies
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

ALTER TABLE government_agencies ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Agencies are publicly readable"
  ON government_agencies FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Agencies are publicly readable anon"
  ON government_agencies FOR SELECT
  TO anon
  USING (true);

-- BARAZA: Sessions
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

ALTER TABLE baraza_sessions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read baraza sessions"
  ON baraza_sessions FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Authenticated users can create baraza sessions"
  ON baraza_sessions FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- BARAZA: Commitments
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

ALTER TABLE commitments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read commitments"
  ON commitments FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Authenticated users can create commitments"
  ON commitments FOR INSERT
  TO authenticated
  WITH CHECK (true);

CREATE POLICY "Authenticated users can update commitments"
  ON commitments FOR UPDATE
  TO authenticated
  USING (true)
  WITH CHECK (true);

-- BARAZA: Evidence
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

ALTER TABLE commitment_evidence ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read evidence"
  ON commitment_evidence FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Authenticated users can create evidence"
  ON commitment_evidence FOR INSERT
  TO authenticated
  WITH CHECK (true);

-- BARAZA: Verifications
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

ALTER TABLE commitment_verifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Authenticated users can read verifications"
  ON commitment_verifications FOR SELECT
  TO authenticated
  USING (true);

CREATE POLICY "Citizens can create verifications"
  ON commitment_verifications FOR INSERT
  TO authenticated
  WITH CHECK (citizen_id = auth.uid());

-- Analytics: Agency Scorecards
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

-- Sustainability: Contextual Promotions
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

ALTER TABLE contextual_promotions ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Active promotions are publicly readable"
  ON contextual_promotions FOR SELECT
  TO authenticated
  USING (active = true);

-- Analytics: Civic Insights
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

ALTER TABLE civic_insights_aggregated ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Civic insights are publicly readable"
  ON civic_insights_aggregated FOR SELECT
  TO authenticated
  USING (true);
