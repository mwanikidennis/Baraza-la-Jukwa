# JUKWA Architecture Overview

This document describes the high-level architecture of the Jukwa platform.

## System Layers

1. **Gateway Layer**: Nginx (OpenResty) providing reverse proxy and TLS termination.
2. **Application Tier**: Microservice cluster (Fastify/Node.js and Python).
3. **Data Tier** (Hybrid Supabase + Docker):
   - **Supabase** (PostgreSQL 15 + PostGIS 3.3) -- Primary relational/spatial database
   - **MongoDB 7** -- Time-series telemetry (traffic sensors)
   - **Redis 7** -- Caching/sessions (optional for MVP)
4. **Messaging Tier**: Mosquitto MQTT for real-time pub/sub.

## Database Architecture: Hybrid Supabase + Docker

JUKWA uses a hybrid database architecture where Supabase serves as the primary
database and Docker containers provide specialized data stores that Supabase
cannot replace.

### What Supabase Provides (Primary Database)
- PostgreSQL 15 with PostGIS 3.3 spatial extensions
- Row Level Security (RLS) for data access control
- Auto-generated REST API via PostgREST
- Built-in authentication (GoTrue)
- Real-time WebSocket subscriptions
- 500MB free tier (sufficient for MVP)

### What Supabase Does NOT Replace
| Component | Why It Stays | Alternative Considered |
|-----------|-------------|------------------------|
| MongoDB | High-write time-series telemetry (traffic sensors). Supabase is relational, not optimized for thousands of sensor readings per minute. | Supabase Realtime -- rejected (WebSocket, not time-series) |
| Redis | Sub-millisecond caching for token lookups and API responses. Supabase has no in-memory cache. | Supabase PostgREST HTTP cache headers -- insufficient for session caching |
| Mosquitto MQTT | Low-bandwidth pub/sub with QoS guarantees and topic-level ACL. Critical for Safaricom 3G connectivity. | Supabase Realtime -- rejected (WebSocket uses more bandwidth, no QoS, no topic ACL) |

### DATABASE_MODE Configuration
- `DATABASE_MODE=supabase` (default) -- Services connect to Supabase cloud PostgreSQL
- `DATABASE_MODE=local` -- Services connect to Docker PostgreSQL (offline development)

### Migration Path
- **Phase 1 (MVP)**: Supabase Cloud free tier + Docker for MongoDB/Redis/Mosquitto
- **Phase 2 (Scale)**: Self-host Supabase on Safaricom Cloud for Kenyan data sovereignty
- **Phase 3 (Enterprise)**: Multi-region PostgreSQL replication for high availability

## Service Map

Refer to `shared/constants/service-ports.ts` for the official port mapping.

| Service | Port | Database | MQTT | Redis |
|---------|------|----------|------|-------|
| Incident Service | 3001 | Supabase/PostgreSQL | Publish (QoS 1) | Cache |
| Commitment Service | 3002 | Supabase/PostgreSQL | Publish (QoS 1) | -- |
| Traffic Service | 3003 | MongoDB + Supabase | Subscribe/Publish | -- |
| Emergency Service | 3004 | Supabase/PostgreSQL | Publish (QoS 2) | -- |
| Identity Service | 3006 | Supabase/PostgreSQL | -- | Token cache |
| Notification Service | 3007 | -- | Subscribe | -- |
| AI Agent | 3010 | -- | -- | -- |
| Citizen Vault | 3011 | Encrypted local | -- | -- |

## MQTT Topic Hierarchy

Refer to `shared/constants/mqtt-topics.ts` for the full topic map.

```
jukwa/
  alerts/{county}/{ward}/{category}     -- Citizen-facing alerts (QoS 1)
  traffic/
    sensors/{junction_id}               -- ITS sensor telemetry (QoS 0)
    alerts/{corridor}                   -- Processed traffic alerts (QoS 1)
  incidents/{incident_id}/status        -- Individual incident updates (QoS 1)
  emergency/{county}/dispatch           -- Emergency dispatch (QoS 2)
  baraza/
    {ward_id}/commitments               -- Ward-level commitment updates (QoS 1)
    {ward_id}/verifications             -- Citizen verification requests (QoS 1)
    agencies/{agency_id}                -- Agency-specific updates (QoS 1)
    national/scorecards                 -- Aggregate scorecard updates (QoS 1)
  civic/{ward}/polls                    -- Civic participation events (QoS 1)
```

## Android Architecture (Offline-First)

```
+---------------------------------------------+
|                  Compose UI                  |
|  HomeScreen | ReportScreen | MyReportsScreen|
+---------------------------------------------+
|              ViewModels (MVVM)               |
+---------------------------------------------+
|           Use Cases (Domain Layer)           |
+---------------------------------------------+
|         Repositories (Offline-First)        |
|    Room (local) --sync--> Ktor (remote)     |
+---------------------------------------------+
|  Room DB | Ktor Client | WorkManager Sync   |
|  (local) |  (network)   |  (background)     |
+---------------------------------------------+
```

Sync priority levels (from `shared/constants/sync-priority.ts`):
- EMERGENCY (0) -- Immediate sync, any network
- SECURITY (1) -- Sync on any available network
- TRAFFIC (2) -- Sync on WiFi or 4G
- CIVIC (3) -- Sync on WiFi only
- GENERAL (4) -- Sync on WiFi + charging

---
*For implementation details, see [SETUP.md](SETUP.md).*
*For database architecture rationale, see [Bolt_AI_Assessment_and_Plan_2026_05_11](Start%20Build/Bolt_AI_Assessment_and_Plan_2026_05_11) Section 2.*
