-- BARAZA: Government Commitments (core BARAZA entity)
-- Target: infra/postgres/migrations/004_commitments.sql

CREATE TABLE IF NOT EXISTS commitments (
    commitment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    origin_type VARCHAR(30) NOT NULL,
        -- 'JIM_BARAZA', 'CITIZEN_CAPTURE', 'DIGITAL_SUBMISSION',
        -- 'OFFICIAL_ANNOUNCEMENT', 'JIM_TICKET_IMPORT'
    baraza_session_id UUID REFERENCES baraza_sessions(session_id),
    originating_citizen_id UUID REFERENCES citizens(citizen_id),
    jim_ticket_id VARCHAR(50),

    -- The Promise
    sector VARCHAR(50) NOT NULL,
    promise_summary TEXT NOT NULL,
    fulfillment_criteria TEXT,
    affected_ward_id INTEGER REFERENCES wards(ward_id),
    affected_location GEOMETRY(Point, 4326),
    affected_facility_name VARCHAR(200),

    -- Responsibility
    responsible_agency_id UUID REFERENCES government_agencies(agency_id),
    responsible_official_name VARCHAR(200),
    responsible_official_designation VARCHAR(200),

    -- Lifecycle
    status VARCHAR(30) DEFAULT 'CAPTURED',
    sla_deadline TIMESTAMPTZ,
    explicit_deadline TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    fulfilled_claimed_at TIMESTAMPTZ,
    citizen_verified_at TIMESTAMPTZ,
    escalation_level SMALLINT DEFAULT 0,

    -- Metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_commitments_ward ON commitments (affected_ward_id, status);
CREATE INDEX IF NOT EXISTS idx_commitments_agency ON commitments (responsible_agency_id, status);
CREATE INDEX IF NOT EXISTS idx_commitments_status ON commitments (status, sla_deadline);
CREATE INDEX IF NOT EXISTS idx_commitments_location ON commitments USING GIST (affected_location);
