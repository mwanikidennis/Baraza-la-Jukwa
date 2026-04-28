-- BARAZA: Government Agency Directory
-- Target: infra/postgres/migrations/002_government_agencies.sql

CREATE TABLE IF NOT EXISTS government_agencies (
    agency_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agency_name VARCHAR(200) NOT NULL,
    agency_type VARCHAR(50) NOT NULL,
        -- 'MINISTRY', 'STATE_DEPARTMENT', 'PARASTATAL',
        -- 'COUNTY_GOVT', 'COUNTY_DEPT'
    parent_agency_id UUID REFERENCES government_agencies(agency_id),
    contact_email VARCHAR(200),
    contact_phone VARCHAR(20),
    api_endpoint TEXT,
    default_sla_days JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
