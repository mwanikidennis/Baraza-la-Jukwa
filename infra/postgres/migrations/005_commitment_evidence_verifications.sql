-- BARAZA: Commitment Evidence Chain & Citizen Verification Votes
-- Target: infra/postgres/migrations/005_commitment_evidence_verifications.sql

-- Commitment Evidence Chain
CREATE TABLE IF NOT EXISTS commitment_evidence (
    evidence_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    evidence_type VARCHAR(30) NOT NULL,
        -- 'CREATION_AUDIO', 'CREATION_VIDEO', 'PROGRESS_UPDATE',
        -- 'AGENCY_REPORT', 'CITIZEN_VERIFICATION', 'CITIZEN_DISPUTE',
        -- 'ESCALATION_NOTICE'
    submitted_by_type VARCHAR(20) NOT NULL,
        -- 'FIELD_DIGITIZER', 'CITIZEN', 'AGENCY', 'SYSTEM'
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
    vote VARCHAR(10) NOT NULL, -- 'CONFIRMED', 'DISPUTED'
    evidence_media_url TEXT,
    comment TEXT,
    voted_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(commitment_id, citizen_id)
);

CREATE INDEX IF NOT EXISTS idx_commitment_verifications_commitment ON commitment_verifications (commitment_id);
