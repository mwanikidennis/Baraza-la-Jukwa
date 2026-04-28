-- BARAZA: Accountability Scorecard (materialized, refreshed hourly)
-- Target: infra/postgres/migrations/006_agency_scorecards.sql

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
