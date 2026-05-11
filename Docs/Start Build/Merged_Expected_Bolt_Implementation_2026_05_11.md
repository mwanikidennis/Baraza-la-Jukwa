# JUKWA  Roadmap
This document outlines the strategic priorities of the Jukwa platform development.

## 1. Privacy & Security (Hardened)
- [x] **Citizen Vault Relay**: NGO-aligned infrastructure for anonymous reporting.
- [x] **Tor Routing**: SOCKS5 integration for IP obfuscation.
- [ ] **Onion Service**: Expose the Jukwa API as a `.onion` address for end-to-end encryption and metadata protection.
- [ ] **Zero-Knowledge Proofs (ZKP)**: For anonymous reward claiming (Gamification).

## 2. Intelligence & Automation
- [x] **AI Triage Engine**: Gemini 3.1 Pro integration for incident classification.
- [ ] **Automated Emergency Dispatch**: Logic to automatically alert NARS/Police based on AI severity confidence > 90%.
- [ ] **Predictive Traffic Routing**: Using time-series data to predict congestion before it happens.

## 3. Civic Engagement & BARAZA
- [ ] **M-Pesa Integration**: Payment gateway for tolls, fees, and micro-donations to community projects.
- [ ] **Gamification Engine**: Reward system for verified civic actions (e.g., reporting potholes that get fixed).
- [ ] **Evidence Immutable Ledger**: Storing hashes of commitment evidence on a public ledger for total transparency.

## 4. Operational Infrastructure
- [ ] **TUI Command Center (Hardened)**: Interactive terminal dashboard for TMC operators.
- [ ] **Kong API Gateway**: Transition from Nginx to Kong for advanced rate limiting, plugins, and key management.
- [ ] **High-Availability (HA) DB**: Multi-region PostgreSQL replication for disaster recovery.

# JUKWA — Local & CI Setup

---
Architecture Maps, Flows & Logic

### 1.1 The Master Data Flow — From Citizen to Resolution

Every interaction with Jukwa, regardless of channel, follows a single unified data pipeline. This is the architectural spine that makes the platform coherent. Understanding this flow end-to-end is the first requirement before writing a single line of code.

```
╔══════════════════════════════════════════════════════════════════════╗
║                    CITIZEN INPUT CHANNELS                           ║
║                                                                      ║
║   📱 Android App    🌐 PWA    📞 USSD/SMS    💬 WhatsApp Bot       ║
║        │              │           │                │                 ║
║        └──────────────┴───────────┴────────────────┘                 ║
║                              │                                       ║
║                    ┌─────────▼──────────┐                            ║
║                    │   INPUT NORMALIZER  │                            ║
║                    │   Every channel     │                            ║
║                    │   produces the same │                            ║
║                    │   JSON payload      │                            ║
║                    └─────────┬──────────┘                            ║
╚══════════════════════════════╪════════════════════════════════════════╝
                               │
                    ┌──────────▼──────────┐
                    │   PRIVACY GATE       │
                    │                      │
                    │  Standard? → minimal │
                    │    pseudonym ID      │
                    │                      │
                    │  Incognito? → route  │
                    │    via Citizen Vault │
                    │    strip ALL PII     │
                    │                      │
                    │  Verified? → attach  │
                    │    eCitizen OAuth    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │   AI CLASSIFICATION  │
                    │   ENGINE             │
                    │                      │
                    │  1. What type?       │
                    │     (40 categories)  │
                    │                      │
                    │  2. How severe?      │
                    │     (1-5 score)      │
                    │                      │
                    │  3. Who handles it?  │
                    │     (Agency routing) │
                    │                      │
                    │  4. Is it a report   │
                    │     or a commitment? │
                    │     (BARAZA triage)  │
                    └──────────┬──────────┘
                               │
              ┌────────────────┼────────────────┐
              ▼                ▼                ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │   INCIDENT   │  │  COMMITMENT  │  │  EMERGENCY   │
   │   PIPELINE   │  │  PIPELINE    │  │  PIPELINE    │
   │              │  │  (BARAZA)    │  │  (< 10 sec)  │
   │  Pothole     │  │              │  │              │
   │  Crime       │  │  PS promise  │  │  Accident    │
   │  Congestion  │  │  Road pledge │  │  Assault     │
   │  Noise       │  │  Drug supply │  │  Fire        │
   └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
          │                 │                 │
          └────────┬────────┘                 │
                   │                          │
          ┌────────▼────────┐        ┌────────▼────────┐
          │  AGENCY ROUTER  │        │  EMERGENCY       │
          │                 │        │  DISPATCH        │
          │  NPS, NTSA,     │        │  999/112/114     │
          │  KURA, KEMSA,   │        │  NARS, Police    │
          │  County Govts,  │        │  Fire Brigade    │
          │  NEMA, KPLC...  │        └────────┬────────┘
          └────────┬────────┘                 │
                   │                          │
          ┌────────▼──────────────────────────▼────────┐
          │           TRACKING & ACCOUNTABILITY         │
          │                                             │
          │  SLA Clock Started → Progress Monitored →   │
          │  Updates Pushed → Citizen Notified →        │
          │  Fulfillment Claimed → Citizen Verifies →   │
          │  RESOLVED  or  ESCALATED                    │
          └────────────────────┬────────────────────────┘
                               │
          ┌────────────────────▼────────────────────────┐
          │           PUBLIC ACCOUNTABILITY LAYER        │
          │                                             │
          │  Ward Dashboards │ Agency Scorecards │      │
          │  National Heatmap │ Civic Insights │        │
          │  Quarterly Reports │ Media API              │
          └─────────────────────────────────────────────┘
```

### 1.2 The Offline-First Sync Logic

This is the flow that makes Jukwa work on Kenyan infrastructure — where a matatu passenger on Thika Road might have 4G one minute and zero bars the next.

```
CITIZEN CREATES REPORT (offline-capable)
         │
         ▼
┌─────────────────────────────────────────┐
│         LOCAL DEVICE STORAGE             │
│         (Room / SQLite)                  │
│                                          │
│  Report saved with:                      │
│    → UUID generated locally              │
│    → GPS timestamp captured              │
│    → Media compressed to WebP            │
│    → EXIF metadata stripped              │
│    → Priority level assigned             │
│    → Status: QUEUED_FOR_SYNC             │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│      CONNECTIVITY MONITOR                │
│      (WorkManager + ConnectivityManager) │
│                                          │
│  Checks network state continuously:      │
│                                          │
│  WiFi detected?                          │
│    → Sync ALL queued items               │
│    → Upload full-res media               │
│                                          │
│  4G/LTE detected?                        │
│    → Sync EMERGENCY + SECURITY first     │
│    → Sync TRAFFIC + CIVIC next           │
│    → Upload compressed media only        │
│                                          │
│  3G/2G detected?                         │
│    → Sync EMERGENCY only (text + GPS)    │
│    → Queue everything else               │
│    → SMS fallback for critical alerts    │
│                                          │
│  No connection?                          │
│    → Everything queues locally            │
│    → User sees "Will sync when online"   │
└────────────────┬────────────────────────┘
                 │
                 ▼ (when connected)
┌─────────────────────────────────────────┐
│      SYNC ENGINE                         │
│                                          │
│  1. Send queued reports (priority order) │
│  2. Pull new alerts for user's ward      │
│  3. Pull commitment status updates       │
│  4. Pull SDUI layout updates             │
│  5. Update offline map tile cache        │
│  6. Rotate device token (if Incognito)   │
│                                          │
│  Conflict resolution:                    │
│    Reports → append-only, never conflict │
│    Alerts → server wins (latest = truth) │
│    Settings → client wins (user intent)  │
└─────────────────────────────────────────┘
```

### 1.3 The BARAZA Commitment Lifecycle Flow

This is the accountability engine — the flow that turns a government promise into a tracked, verified, publicly visible obligation.

```
  ┌─────────────────────┐      ┌──────────────────────┐
  │  JIM BARAZA FORUM   │      │  CITIZEN DIGITAL      │
  │  (Field Digitizer   │      │  SUBMISSION           │
  │   captures promise) │      │  (App/USSD/WhatsApp)  │
  └──────────┬──────────┘      └───────────┬───────────┘
             │                              │
             └──────────┬───────────────────┘
                        │
             ┌──────────▼──────────┐
             │      CAPTURED       │     ← Clock starts
             │  AI classifies:     │
             │  Sector + Agency    │
             └──────────┬──────────┘
                        │
             ┌──────────▼──────────┐
             │     CLASSIFIED      │     ← Public visibility begins
             │  Agency notified    │        72-hour ACK deadline
             └──────────┬──────────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
    Agency ACKs    No response   Needs clarity
          │         (72 hrs)          │
          ▼             ▼             ▼
  ┌─────────────┐ ┌──────────┐ ┌───────────┐
  │ ACKNOWLEDGED│ │ SILENCE  │ │CLARIFY    │
  │ Agency sets │ │ Auto-    │ │REQUIRED   │
  │ timeline    │ │ escalate │ │           │
  └──────┬──────┘ │ to head  │ └─────┬─────┘
         │        └─────┬────┘       │
         ▼              ▼            │
  ┌─────────────┐ ┌──────────┐      │
  │ IN PROGRESS │ │ESCALATED │◄─────┘
  │ Updates due │ │ PS/CS    │
  │ weekly or   │ │ notified │
  │ biweekly    │ └─────┬────┘
  └──────┬──────┘       │
         │              │
         ▼              ▼
  ┌─────────────┐ ┌──────────────┐
  │ FULFILLED   │ │   OVERDUE    │
  │ (Agency     │ │ Public alert │
  │  claims     │ │ → MCA notify │
  │  done)      │ │ → Media feed │
  └──────┬──────┘ └──────┬───────┘
         │               │
         ▼               ▼
  ┌─────────────────┐ ┌────────────┐
  │CITIZEN VERIFIES │ │  FAILED    │
  │ 5 confirms =    │ │ Documented │
  │   RESOLVED ✓    │ │ permanent  │
  │ 5 disputes =    │ │ civic      │
  │   back to       │ │ record     │
  │   IN PROGRESS   │ └────────────┘
  └─────────────────┘
```

---

## Part II: The Complete Tech Stack

### 2.1 Client Layer

**Native Android Application.** Language: Kotlin 1.9+. UI framework: Jetpack Compose with Material 3. Minimum SDK: API 23 (Android 6.0). Target SDK: API 35 (Android 15). Architecture pattern: MVVM with Clean Architecture layers (Presentation → Domain → Data). Dependency injection: Hilt. Navigation: Compose Navigation with type-safe arguments. The APK target is under 15MB using Android App Bundle with on-demand feature modules (the map module loads post-install). Testing uses JUnit 5 for unit tests, Turbine for Flow testing, and Espresso for UI integration tests.

**Local Storage.** Room 2.6+ provides the offline-first SQLite persistence layer. Room was chosen over raw SQLite for compile-time query verification, coroutine/Flow integration, and migration support. The database schema mirrors the server schema for incidents, commitments, alerts, and cached ward data, enabling full offline operation. DataStore (Preferences) handles user settings, anonymity mode, and cached SDUI layouts. WorkManager 2.9+ manages background sync with exponential backoff and network-type constraints. Coil handles image loading with automatic WebP transcoding, memory/disk caching, and placeholder generation.

**Mapping.** MapLibre Native SDK for Android provides the map rendering, chosen over Google Maps for three reasons: offline tile caching with pre-loaded Nairobi metro tiles (~50MB covering the entire metro at zoom levels 10–16), zero API key costs at scale, and full customization of map styling (Jukwa's safety heatmap overlay requires custom layer rendering that Google Maps restricts). OpenStreetMap serves as the base tile source, with Kenya's exceptionally detailed OSM data (the Digital Matatus project mapped all 130+ matatu routes, Map Kibera produced one of the densest datasets globally). Turf.js handles client-side geospatial calculations (point-in-polygon for ward detection, distance calculations for proximity alerts).

**Networking.** Ktor Client 2.3+ handles HTTP communication, chosen over Retrofit for multiplatform potential (shared networking code if iOS is ever pursued) and native coroutine support. Protocol Buffers (protobuf-lite) serialize real-time payloads at 30–50% smaller than JSON, critical on 3G connections. HiveMQ MQTT Client for Android maintains the persistent MQTT connection for real-time alerts. Firebase Cloud Messaging SDK handles push notification receipt and topic subscriptions.

**Privacy Engine.** AndroidX ExifInterface strips JPEG metadata. A custom MP4 atom parser strips video metadata (moov/udta atoms containing GPS and device signatures). AndroidX Security Crypto provides EncryptedSharedPreferences for sensitive local data. NaCl (libsodium via Lazysodium-android) handles end-to-end encryption for Incognito Mode submissions before network transmission.

**Progressive Web App.** Framework: Next.js 14 with App Router. Service worker: Workbox 7 for precaching and runtime caching strategies (CacheFirst for map tiles, NetworkFirst for API data, StaleWhileRevalidate for SDUI layouts). Map rendering: MapLibre GL JS. State management: Zustand (lightweight, no boilerplate). The PWA targets a sub-3MB initial payload with offline reporting capability via IndexedDB and Background Sync API.

### 2.2 Gateway Layer

**API Gateway.** NGINX 1.25+ with OpenResty (LuaJIT). Chosen over Kong (no African deployments found, heavy database dependency, enterprise pricing) and Traefik (slightly lower throughput). NGINX delivers approximately 33,591 requests per second on modest hardware with zero database dependency. OpenResty's Lua scripting handles JWT validation, rate limiting (sliding window per device token stored in shared memory), request routing to backend services, and response caching for public dashboard data. TLS 1.3 termination at the gateway with Let's Encrypt certificates auto-renewed via Certbot.

**CDN.** Cloudflare (free tier for MVP, Pro at $20/month for WAF and image optimization). Cloudflare's Nairobi Point of Presence delivers 5–10ms median latency for cached assets: map tiles, ward boundary GeoJSON, SDUI layout JSON, PWA shell, and static media. Cloudflare Workers handle edge logic for USSD webhook validation and geographic request routing.

**SMS/USSD Gateway.** Africa's Talking (AT). SDKs available in Python, Node.js, Java, PHP, Ruby, and Go. Supports all Kenyan carriers (Safaricom, Airtel, Telkom). USSD sessions cost approximately KSh 1 per session, SMS approximately KSh 0.50–0.80. AT provides the USSD session management, SMS send/receive, shortcode hosting, and airtime disbursement API (for gamification rewards). The AT Simulator enables local development and testing without live carrier integration.

### 2.3 Application Services Layer

**Runtime.** Node.js 20 LTS with TypeScript 5.3+. Chosen over Go (Dennis's existing JavaScript/TypeScript familiarity from Firebase/React work reduces learning curve) and Python (Node's event loop model handles the concurrent I/O patterns of a civic platform more efficiently than Python's threading model, and TypeScript's type safety prevents the class of bugs that plague large Python codebases). Framework: Fastify 4 (2× faster than Express, built-in JSON schema validation, native TypeScript support). Each microservice runs as a standalone Fastify application in its own Docker container.

**Service Mesh.** For the MVP, direct service-to-service HTTP calls over the Docker internal network. At scale (Phase 4+), migration to a lightweight service mesh using Linkerd 2 (lower resource footprint than Istio, automatic mTLS, traffic observability).

**AI Classification.** The rule-based classifier runs as a TypeScript module within the Incident Service and Commitment Service — no separate AI infrastructure needed for MVP. Rules map keyword patterns, location context, and category signals to the 40 incident categories and agency routing table. A lightweight ML validation layer uses ONNX Runtime (Node.js bindings) with a text classification model trained on Ushahidi's Kenyan deployment data (~200K labeled reports) and Mulika Uhalifu's 176K report corpus. The ML model flags disagreements with the rule engine for human review, progressively improving rules. No GPU required — ONNX inference runs on CPU at sub-100ms per classification.

**Task Queue.** BullMQ on Redis handles asynchronous processing: media transcoding (sharp for images, FFmpeg for video), EXIF stripping (server-side defense-in-depth), notification fan-out, escalation timer management, and scheduled jobs (hourly scorecard refresh, daily traffic predictions, quarterly report generation). BullMQ's priority queues ensure emergency processing jumps the queue ahead of routine tasks.

### 2.4 Messaging & Real-Time Layer

**MQTT Broker.** MVP: Eclipse Mosquitto 2 (~200KB footprint, handles 50K concurrent connections on a $20/month VPS). Scale: EMQX 5 (100M+ connections in cluster, MQTT-over-QUIC for 0-RTT mobile reconnection, webhook integration). Topic hierarchy:

```
jukwa/alerts/{county}/{ward}/{category}
jukwa/traffic/sensors/{junction_id}
jukwa/traffic/alerts/{corridor}
jukwa/incidents/{id}/status
jukwa/emergency/{county}/dispatch
jukwa/baraza/{ward_id}/commitments
jukwa/baraza/{ward_id}/verifications
jukwa/baraza/agencies/{agency_id}
jukwa/civic/{ward}/polls
```

QoS levels: 0 for high-frequency sensor telemetry (individual message loss acceptable), 1 for citizen-facing alerts and commitment updates (at-least-once delivery guaranteed), 2 for emergency dispatch (exactly-once, no duplicates).

**Push Notifications.** Firebase Cloud Messaging (FCM). Free, native Android integration, handles device wake from Doze mode. Used exclusively for notification delivery (title, category, location summary, incident/commitment ID). Full payload loads from MQTT session or REST API when the user opens the app. Topic subscriptions mirror MQTT hierarchy for server-side geographic targeting.

### 2.5 Data & Storage Layer

**Primary Datastore.** PostgreSQL 15 with PostGIS 3.3. Hosting: Supabase (managed PostgreSQL with PostGIS, built-in PostgREST for auto-generated REST APIs, real-time subscriptions via WebSocket, Row Level Security for fine-grained access control). Supabase was chosen over raw PostgreSQL for dramatically reduced backend code — PostgREST auto-generates CRUD endpoints for every table, and Supabase's real-time engine pushes database changes to connected clients without custom WebSocket code. Self-hosted alternative: PostgreSQL on Safaricom Cloud or iXAfrica for maximum data sovereignty.

**Telemetry Datastore.** MongoDB 7 with time-series collections. Handles high-velocity IoT data from Samsung ITS sensors, Waze CCP feeds, and MQTT telemetry logs. Time-series collections provide automatic bucketing, compression, and efficient range queries. Hosting: MongoDB Atlas (Singapore or Mumbai region for MVP, migrating to local hosting when AWS Nairobi launches in late 2026).

**Cache & Real-Time State.** Redis 7. Functions: API response caching (TTL-based), rate limiting counters (sliding window), session tokens, geospatial proximity queries (GEOADD/GEORADIUS for "nearest police station"), BullMQ job queue backend, and MQTT session state persistence.

**Object Storage.** MinIO (self-hosted S3-compatible) for MVP. All citizen-uploaded media (photos, videos, audio recordings, baraza session recordings) is stored encrypted at rest (AES-256). Media goes through the scrubbing pipeline (EXIF removal, video transcoding to H.264 720p, thumbnail generation) before storage. Lifecycle policies auto-archive media older than 12 months to cold storage, auto-delete non-evidentiary media after 24 months.

### 2.6 Privacy & Anonymity Layer

**Citizen Vault Relay.** GlobaLeaks-based architecture hosted by civil society consortium on independent infrastructure. End-to-end encryption (NaCl/libsodium), no IP logging, ChaCha20-encrypted filesystem, optional Tor access via obfs4 bridges and Snowflake pluggable transports. Only activated for Incognito Mode submissions.

**On-Device Privacy.** EXIF stripping via AndroidX ExifInterface + custom MP4 atom parser. GPS fuzzing to ward centroid (±500m random noise). Device token rotation every 72 hours in Incognito Mode via one-way hash chain. Local report encryption before any network transmission.

### 2.7 External Integrations

```
┌─────────────────────────────────────────────────────────┐
│                  EXTERNAL API INTEGRATIONS               │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Safaricom    │  │ Africa's     │  │ eCitizen     │  │
│  │ Daraja 3.0   │  │ Talking      │  │ OAuth 2.0    │  │
│  │              │  │              │  │              │  │
│  │ M-Pesa STK   │  │ USSD Gateway │  │ Identity     │  │
│  │ Push, B2C,   │  │ SMS Send/    │  │ Verification │  │
│  │ Lipa Na      │  │ Receive,     │  │ (Verified    │  │
│  │ M-Pesa       │  │ Airtime      │  │  Mode only)  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Waze CCP     │  │ Samsung ITS  │  │ WhatsApp     │  │
│  │              │  │              │  │ Business API │  │
│  │ Bidirectional│  │ MQTT Sensor  │  │              │  │
│  │ Traffic Data │  │ Feeds from   │  │ Chatbot NLP  │  │
│  │ Exchange     │  │ 25 Junctions │  │ Channel      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ NPSIMS       │  │ NTSA TIMS    │  │ jamiiimara   │  │
│  │ Police API   │  │ Transport    │  │ .org API     │  │
│  │ (Phase 2)    │  │ API          │  │ JIM Ticket   │  │
│  │              │  │              │  │ Import       │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 2.8 Infrastructure & DevOps

**Containerization.** Docker with Docker Compose for MVP. Migration to K3s (lightweight Kubernetes) at scale. Each microservice has its own Dockerfile, health check endpoint, and graceful shutdown handler.

**CI/CD.** GitHub Actions. Pipeline: lint (ESLint + ktlint) → unit tests → build Docker images → push to container registry → deploy to staging → integration tests → deploy to production (manual approval gate). Android builds use Gradle with GitHub-hosted runners, producing signed AAB files for Play Store deployment.

**Monitoring.** Grafana + Prometheus for infrastructure metrics (CPU, memory, network, database connections). Loki for centralized log aggregation. Uptime Kuma for endpoint health monitoring (self-hosted, free). Sentry for client-side error tracking (Android and PWA). Custom Grafana dashboards for civic metrics (reports per hour, average response time, active MQTT connections, sync queue depth).

**Hosting.** Primary: Kenyan data center (Safaricom Cloud or iXAfrica/Digital Realty) for DPA compliance. CDN: Cloudflare with Kenya PoPs. DNS: Cloudflare DNS. SSL: Let's Encrypt via Certbot with auto-renewal. Domain: jukwa.ke (or jukwa.co.ke).

---

# JUKWA MQTT Topic Hierarchy
This document defines the standardized topic structure and Quality of Service (QoS) levels for the Jukwa platform.

## Topic Structure
Official constants are defined in `shared/constants/mqtt-topics.ts`.

### 1. Traffic Telemetry (`jukwa/traffic/sensors/#`)
- **QoS**: 0 (Best effort)
- **Payload**: JSON with vehicle count and speed.
- **Why**: High-volume sensor data where single message loss is acceptable.

### 2. Emergency Alerts (`jukwa/emergency/#`)
- **QoS**: 2 (Exactly Once)
- **Payload**: SOS location and media stream metadata.
- **Why**: Critical for life-safety; delivery must be guaranteed.

### 3. Civic Incidents (`jukwa/incidents/#`)
- **QoS**: 1 (At least once)
- **Payload**: Incident category and coordinates.

## Access Control (ACL)
Refer to `infra/mosquitto/config/acl.conf` for service-level read/write permissions.
- **Services**: Write to relevant alert topics.
- **Clients**: Read-only access to public alert topics.

---

# BARAZA: Public Accountability Module
This document describes the BARAZA module for government commitment tracking.

## Lifecycle of a Commitment
1. **Capture**: A public promise is recorded (from speech, news, or session).
2. **Assignment**: The promise is linked to a `government_agency` and `affected_ward`.
3. **Evidence**: Citizens or agencies upload evidence (photos/docs) of progress.
4. **Verification**: Peer-to-peer or admin verification of the evidence.
5. **Scorecard**: Real-time fulfillment rates calculated in the `agency_scorecards` view.

## Status FSM
Official states and transitions are defined in `shared/constants/commitment-status.ts`.
- `PROPOSED`
- `VERIFIED_CLAIM`
- `IN_PROGRESS`
- `STALLED`
- `COMPLETED`
- `VERIFIED_RESOLUTION`

---


## 1. Prerequisites

| Tool | Version | Notes |
| --- | --- | --- |
| Docker Desktop | latest, WSL2 backend | Required for `infra/docker-compose.yml` |
| Node.js | 20 LTS | Backend services |
| Git | 2.40+ | |
| Android Studio | latest | (Phase 2) |

> If `docker --version` fails, install Docker Desktop for Windows and reboot. Verify with `docker compose version`.

## 2. First-time clone

```bash
git clone https://github.com/kenyawebs/Baraza-la-Jukwa.git
cd Baraza-la-Jukwa
cp .env.example .env
# edit .env — at minimum set JWT_SECRET to a long random string
```

## 3. Bring up infra

```bash
cd infra
docker compose up -d --build
docker compose ps          # all services should reach "healthy"
```

### Smoke tests

```bash
curl -fsS http://localhost:3001/health                      # incident-service
curl -fsS http://localhost:3002/health                      # commitment-service
curl -fsS http://localhost:3003/health                      # traffic-service
curl -fsS http://localhost:3010/health                      # ai-agent-service
curl -fsS http://localhost/healthz                          # nginx edge
docker exec jukwa-postgres psql -U jukwaa -d jukwaa \
  -c "SELECT PostGIS_Version();"                            # PostGIS
docker exec jukwa-mosquitto mosquitto_sub -t '$SYS/#' -C 1  # MQTT
docker exec jukwa-mongodb mongosh --eval "db.adminCommand('ping')" # MongoDB
```

### Tear down

```bash
docker compose down       # keep volumes
docker compose down -v    # wipe DB / MinIO / MQTT data too
```

## 4. CI / GitHub backend

Every PR runs three jobs from [`.github/workflows/ci.yml`](../.github/workflows/ci.yml):

- `lint-test-backend` — typecheck + build + test for all Node services
- `build-images` — multi-arch Docker build for all services (Node + Python)
- `compose-smoke` — runs `docker compose up` in CI and validates `/health` + PostGIS

Plus:

- [`codeql.yml`](../.github/workflows/codeql.yml) — JS/TS security scanning, weekly + on PR
- [`dependabot.yml`](../.github/dependabot.yml) — weekly npm / docker / actions updates

### Container images (GHCR)

Images publish under `ghcr.io/<your-gh-username>/jukwa-<service>` with tags:

- `latest` (main only)
- `main`
- `sha-<short>`

Auth uses the built-in `GITHUB_TOKEN` — no secrets to configure.

After the **first successful push to `main`**, make the package public (or invite collaborators):
GitHub → your profile → Packages → `jukwa-incident` → Package settings → Change visibility.

### Make CI required on `main`

Once the workflows have run green at least once on `main`:

1. GitHub → repo → **Settings** → **Branches** → **Add branch ruleset** (or edit the existing rule for `main`).
2. Enable **Require status checks to pass before merging**.
3. Search and add these required checks:
   - `Lint & test backend`
   - `docker compose smoke test`
   - `Build & push images (GHCR) (incident)`
4. Enable **Require branches to be up to date before merging**.
5. Save.

## 5. Adding a new backend service (monorepo workflow)

When you scaffold `services/<name>/`:

1. **Service folder**: include `package.json` (with `typecheck`, `build`, `test` scripts), `tsconfig.json`, `Dockerfile` (multi-stage, non-root `USER node`, `HEALTHCHECK`), `src/index.ts` exposing `GET /health`.
2. **Compose entry**: add a service block in [`infra/docker-compose.yml`](../infra/docker-compose.yml) — assign a unique port (incident=3001, commitment=3002, traffic=3003, emergency=3004, civic=3005, identity=3006, notification=3007, payment=3008, accountability=3009, ai-classifier=3010, media=3011, ussd=3012, whatsapp=3013) per the Master Prompt port map.
3. **NGINX route**: add an `upstream` and `location /api/<resource>` block in [`infra/nginx/conf.d/default.conf`](../infra/nginx/conf.d/default.conf).
4. **CI** ([`.github/workflows/ci.yml`](../.github/workflows/ci.yml)): append `<name>` to **both** `matrix.service` arrays (`lint-test-backend` and `build-images`) and add a `<name>: 'services/<name>/**'` entry under the `changes` job's `paths-filter`.
5. **Dependabot** ([`.github/dependabot.yml`](../.github/dependabot.yml)): add `npm` and `docker` blocks for `/services/<name>`.
6. **CODEOWNERS** ([`.github/CODEOWNERS`](../.github/CODEOWNERS)): add an owner line if it differs from the default.

## 6. Troubleshooting

| Symptom | Fix |
| --- | --- |
| `docker: command not found` | Docker Desktop not installed or not on PATH. Install + reboot. |
| `incident-service` unhealthy | `docker logs jukwa-incident-service` — usually a missing env var or DB not yet ready. |
| Port 5432/6379/1883 already in use | Stop the local Postgres/Redis/Mosquitto, or change the host port mapping in `infra/docker-compose.yml`. |
| `compose-smoke` CI job fails on healthcheck | Check the job logs for the failed container's `docker logs` output (last step). |

---

## THE MONOREPO STRUCTURE

The project uses a single GitHub monorepo:

```
jukwa/
├── android/                    ← Native Android app (Kotlin/Compose)
│   ├── app/src/main/kotlin/ke/jukwa/
│   │   ├── core/               ← Shared utilities, extensions, constants
│   │   ├── data/               ← Repository implementations
│   │   │   ├── local/          ← Room DAOs, entities, database
│   │   │   ├── remote/         ← Ktor API clients, DTOs
│   │   │   └── sync/           ← SyncEngine, WorkManager workers
│   │   ├── domain/             ← Use cases, domain models
│   │   │   ├── incident/
│   │   │   ├── commitment/
│   │   │   ├── traffic/
│   │   │   ├── emergency/
│   │   │   └── civic/
│   │   ├── presentation/       ← ViewModels, Compose screens, UI state
│   │   │   ├── home/
│   │   │   ├── report/
│   │   │   ├── baraza/
│   │   │   ├── traffic/
│   │   │   ├── dashboard/
│   │   │   └── settings/
│   │   ├── privacy/            ← EXIF scrubber, encryption, GPS fuzzing
│   │   ├── mqtt/               ← MQTT client manager
│   │   └── sdui/               ← Server-driven UI renderer
│   └── build.gradle.kts
├── services/                   ← Backend microservices (TypeScript/Fastify)
│   ├── incident/               ← Port 3001
│   ├── commitment/             ← Port 3002 (BARAZA)
│   ├── traffic/                ← Port 3003
│   ├── emergency/              ← Port 3004
│   ├── civic/                  ← Port 3005
│   ├── identity/               ← Port 3006
│   ├── notification/           ← Port 3007
│   ├── payment/                ← Port 3008
│   ├── accountability/         ← Port 3009
│   ├── ai-classifier/          ← Port 3010
│   ├── media/                  ← Port 3011
│   └── [each has: src/, Dockerfile, package.json, tsconfig.json]
├── pwa/                        ← Progressive Web App (Next.js 14)
├── ussd/                       ← USSD/SMS handler (Fastify, Port 3012)
├── whatsapp/                   ← WhatsApp Bot (Fastify, Port 3013)
├── infra/                      ← Docker Compose, NGINX configs, Mosquitto configs
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   ├── nginx/conf.d/
│   ├── mosquitto/config/
│   └── postgres/
│       ├── init.sql            ← Full schema with PostGIS
│       └── seed/               ← Ward boundaries, agency directory
├── shared/                     ← Shared TypeScript types, constants, validators
├── data/                       ← Static data: wards.geojson, agencies.json, routing-rules.json
├── docs/                       ← Project documentation
│   ├── ARCHITECTURE.md
│   ├── API.md
│   ├── BARAZA.md
│   ├── SETUP.md
│   └── CONTRIBUTING.md
├── .github/workflows/          ← CI/CD pipelines
├── .env.example
└── README.md
```

## DATABASE SCHEMA (CORE TABLES)

PostgreSQL 15 + PostGIS 3.3. This is the authoritative schema — generate all Room entities and Fastify models from this:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Citizens (pseudonymous by default)
CREATE TABLE citizens (
    citizen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_token_hash VARCHAR(64) UNIQUE NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
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
```

## MQTT TOPIC HIERARCHY

```
jukwa/alerts/{county}/{ward}/{category}
jukwa/traffic/sensors/{junction_id}
jukwa/traffic/alerts/{corridor}
jukwa/incidents/{incident_id}/status
jukwa/emergency/{county}/dispatch
jukwa/baraza/{ward_id}/commitments
jukwa/baraza/{ward_id}/verifications
jukwa/baraza/agencies/{agency_id}
jukwa/civic/{ward}/polls
jukwa/baraza/national/scorecards
```

## THREE-TIER CITIZEN ACCESS

Every critical function works across three access tiers:
1. Native Android app (primary, full features)
2. Progressive Web App (Next.js, lightweight, no install)
3. USSD/SMS via Africa's Talking (universal, works on any phone without internet)

## THREE-MODE ANONYMITY

Citizens choose per-session, per-report:
1. Standard Mode: pseudonymous device token, ward-level location
2. Incognito Mode: zero PII, EXIF stripped, GPS fuzzed to ward centroid, token rotated, routes through Citizen Vault relay
3. Verified Mode: optional eCitizen OAuth for accountability credit and direct response tracking

## OFFLINE-FIRST SYNC PRIORITY

All reports save to Room (SQLite) first. Sync uses WorkManager with priority:
EMERGENCY (immediate, even SMS fallback) → SECURITY (60s) → TRAFFIC (5min) → CIVIC (30min) → GENERAL (WiFi or 24hr)

## UI/UX DESIGN PRINCIPLES

The app targets the median Kenyan smartphone: Samsung Galaxy A05 (3GB RAM, Unisoc SC9863A). Design for:
- Three device tiers: ≤2GB RAM (simplified, no animations), 3-4GB (standard), 4GB+ (full)
- Detect via ActivityManager.isLowRamDevice()
- Minimum touch targets: 48dp
- Bilingual: English and Kiswahili with runtime switching
- High-contrast outdoor readability
- One-hand operation for 5.5-6.5" screens
- Dark theme default (saves battery on AMOLED, dominant in Kenya)
- Color system: Kenyan earth tones — deep green (#1B5E20) primary, warm amber (#FF8F00) accent, red (#D32F2F) for emergencies, grey (#424242) for neutral
- Typography: system default (Roboto), no custom fonts (saves APK size)
- Map-first home screen: the map IS the primary interface, everything else layers on top

## YOUR BEHAVIOR RULES

1. Generate COMPLETE, RUNNABLE code. No placeholders, no "implement this later," no TODO comments unless I specifically ask for a skeleton.

2. Every Kotlin file includes proper package declarations, all imports, and follows the project package structure (ke.jukwa.*).

3. Every TypeScript file includes proper type definitions — no `any` types unless interfacing with an untyped external library.

4. When I ask you to create a feature, generate ALL the files needed: Room entities + DAOs, Repository interface + implementation, Use case, ViewModel, Compose UI, Fastify routes + service + model, database migration, and tests.

5. All database operations are offline-first: write to Room, then sync via WorkManager.

6. All media handling strips EXIF metadata before any network operation.

7. Always respect the three anonymity modes — never store PII in Standard Mode, never store anything identifiable in Incognito Mode.

8. All Compose UI uses Material 3 components, follows the color system above, and works on API 23+.

9. Provide clear file paths for every generated file so I know exactly where it goes in the monorepo.

10. When creating Docker services, include the Dockerfile, the health check endpoint, and the docker-compose.yml additions.


---

## 9. External Integrations Tier

### 9.1 Government Systems

Integration with Kenya's government digital infrastructure is phased. Phase 1 (MVP) integrates with publicly accessible endpoints: NTSA TIMS for vehicle verification lookups, eCitizen OAuth for optional Verified Mode identity confirmation, and emergency service dispatch numbers (999, 112, 114) via telephony intent on the client side. Phase 2 targets NPSIMS (the new unified police information system), which is expected to expose APIs for external platforms as it matures. Phase 3 targets county government systems, which vary wildly in digital maturity — Nairobi County's e-services are API-capable; most other counties require SMS or email integration as interim solutions.

### 9.2 Traffic Data Partners

The Waze Connected Citizens Program provides free bidirectional traffic data exchange. Jukwa shares anonymized, aggregated incident data (road closures, accidents, construction); Waze shares real-time speed data and user-reported incidents from its existing Kenyan user base. This integration provides immediate traffic intelligence at zero infrastructure cost, supplementing the Samsung ITS sensor data as it comes online across the 25 initial junctions.

### 9.3 M-Pesa (Daraja 3.0)

M-Pesa integration powers three revenue and engagement flows. STK Push initiates payment prompts on the user's phone for community crowdfunding contributions — the user confirms with their M-Pesa PIN without leaving the Jukwa app. B2C disbursement sends airtime rewards for gamification milestones via Africa's Talking's airtime API. Lipa Na M-Pesa handles business payments for the contextual promotions module (local businesses paying for geofenced visibility).

The Daraja 3.0 Mini Programs capability offers a future path: Jukwa's core reporting interface could be accessible as a Mini Program within the M-Pesa Super App itself, reaching M-Pesa's approximately 30 million Kenyan users without requiring a separate app download.

---

## 10. Infrastructure & Deployment

### 10.1 Container Orchestration

The entire backend runs in Docker containers orchestrated initially by Docker Compose (MVP) and migrating to Kubernetes (K3s, the lightweight distribution suitable for single-node or small-cluster deployment) as scale demands. The Gemini contribution's `docker-compose.yml` is adopted and extended with the validated component selections:

```yaml
version: '3.8'
services:
  api_gateway:
    image: openresty/openresty:alpine
    ports:
      - "443:443"
      - "80:80"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d
      - ./certs:/etc/ssl/certs

  incident_service:
    build: ./services/incident
    environment:
      - DATABASE_URL=postgresql://jukwaa:${DB_PASS}@spatial_db/jukwaa
      - MQTT_BROKER=mqtt://mqtt_broker:1883
      - REDIS_URL=redis://cache:6379

  traffic_service:
    build: ./services/traffic
    environment:
      - MONGODB_URI=mongodb://telemetry_db:27017/jukwaa_telemetry
      - MQTT_BROKER=mqtt://mqtt_broker:1883
      - WAZE_CCP_TOKEN=${WAZE_TOKEN}

  civic_service:
    build: ./services/civic
    environment:
      - DATABASE_URL=postgresql://jukwaa:${DB_PASS}@spatial_db/jukwaa
      - MPESA_CONSUMER_KEY=${MPESA_KEY}
      - MPESA_CONSUMER_SECRET=${MPESA_SECRET}

  emergency_service:
    build: ./services/emergency
    environment:
      - DATABASE_URL=postgresql://jukwaa:${DB_PASS}@spatial_db/jukwaa
      - MQTT_BROKER=mqtt://mqtt_broker:1883
      - FCM_SERVER_KEY=${FCM_KEY}

  ai_classifier:
    build: ./services/ai_classifier
    environment:
      - MODEL_PATH=/models/incident_classifier_v1.onnx

  mqtt_broker:
    image: eclipse-mosquitto:2
    ports:
      - "1883:1883"
      - "8883:8883"
    volumes:
      - ./mosquitto/config:/mosquitto/config
      - mqtt_data:/mosquitto/data

  spatial_db:
    image: postgis/postgis:15-3.3
    environment:
      - POSTGRES_USER=jukwaa
      - POSTGRES_PASSWORD=${DB_PASS}
      - POSTGRES_DB=jukwaa
    volumes:
      - pg_data:/var/lib/postgresql/data

  telemetry_db:
    image: mongo:7
    ports:
      - "27017:27017"
    volumes:
      - mongo_data:/data/db

  cache:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  object_storage:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=${MINIO_USER}
      - MINIO_ROOT_PASSWORD=${MINIO_PASS}
    volumes:
      - minio_data:/data

volumes:
  pg_data:
  mongo_data:
  mqtt_data:
  minio_data:
```
---

## 8. Technical Integration with Existing Jukwa Architecture

### 8.1 Database Extensions

The BARAZA module extends Jukwa's existing PostgreSQL/PostGIS schema with the following additions:

```sql
-- Government Commitments (core BARAZA entity)
CREATE TABLE commitments (
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

CREATE INDEX idx_commitments_ward ON commitments (affected_ward_id, status);
CREATE INDEX idx_commitments_agency ON commitments (responsible_agency_id, status);
CREATE INDEX idx_commitments_status ON commitments (status, sla_deadline);
CREATE INDEX idx_commitments_location ON commitments USING GIST (affected_location);

-- Baraza Sessions (physical JIM forums + citizen-captured events)
CREATE TABLE baraza_sessions (
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

-- Government Agency Directory
CREATE TABLE government_agencies (
    agency_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agency_name VARCHAR(200) NOT NULL,
    agency_type VARCHAR(50) NOT NULL,
        -- 'MINISTRY', 'STATE_DEPARTMENT', 'PARASTATAL',
        -- 'COUNTY_GOVT', 'COUNTY_DEPT'
    parent_agency_id UUID REFERENCES government_agencies(agency_id),
    contact_email VARCHAR(200),
    contact_phone VARCHAR(20),
    api_endpoint TEXT,
    default_sla_days JSONB
);

-- Commitment Evidence Chain
CREATE TABLE commitment_evidence (
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

-- Citizen Verification Votes
CREATE TABLE commitment_verifications (
    verification_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    commitment_id UUID REFERENCES commitments(commitment_id),
    citizen_id UUID REFERENCES citizens(citizen_id),
    vote VARCHAR(10) NOT NULL, -- 'CONFIRMED', 'DISPUTED'
    evidence_media_url TEXT,
    comment TEXT,
    voted_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(commitment_id, citizen_id)
);

-- Accountability Scorecard (materialized, refreshed hourly)
CREATE MATERIALIZED VIEW agency_scorecards AS
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
```

### 8.2 Service Integration

The BARAZA module adds two new microservices to Jukwa's Application Services Tier:

The **Commitment Service** manages the full commitment lifecycle: creation, classification, routing, state transitions, escalation timers, and citizen verification. It publishes events to the MQTT broker on `jukwa/baraza/{ward_id}/commitments` topics, enabling real-time dashboard updates and push notifications. It integrates with the existing AI Classification Engine for sector categorization and agency routing — the same intelligence that routes incident reports to NPS or NTSA now routes government commitments to KEMSA or KURA.

The **Accountability Service** generates the public dashboards, scorecards, and longitudinal reports. It refreshes materialized views hourly, computes ward-level civic health scores, triggers automated escalation actions on overdue commitments, and generates the quarterly "State of Accountability" reports. It exposes a public read-only API for journalists, researchers, and civil society organizations to query accountability data programmatically.

---

Part III: Complete Feature Matrix

### 3.1 Macro Features (Platform-Level Capabilities)

**USALAMA — Security & Safety Reporting.** The incident reporting engine covering crime reporting (robbery, assault, theft, suspicious activity, gang activity, drug trafficking), emergency SOS with accelerometer impact detection and voice activation, anonymous whistleblowing via Citizen Vault relay, community safety mapping with crowdsourced street-level safety scores (lighting, visibility, security presence), and geofenced security alerts pushed to all users within affected areas. This module directly extends the Jukwaa la Usalama forums' mandate into a permanent digital channel.

**TRAFIKI — Traffic Intelligence.** Real-time traffic monitoring aggregating crowdsourced reports from Jukwa users, Waze CCP bidirectional data feed, Samsung ITS sensor telemetry from 25 Nairobi junctions, and historical pattern analysis. Features include corridor congestion scoring (1–10 index updated every 60 seconds during peak hours), predictive rerouting suggestions based on historical patterns, matatu route tracking and arrival estimation, Nairobi Expressway toll integration information, NaMATA BRT real-time status (when operational), and NTSA violation reporting with photo/video evidence.

**BARAZA — Governance Accountability.** The JIM-integrated government commitment tracking engine covering baraza session capture (Field Digitizer rapid-capture mode for live forums), citizen self-capture of official promises anywhere, commitment lifecycle management with SLA clocks and automated escalation, citizen verification network for crowd-confirming government claims of fulfillment, public Accountability Dashboard with ward/agency/official scorecards, national governance heatmap, and quarterly State of Accountability report generation. Integration with jamiiimara.org's ticketing system ensures no duplication between physical and digital channels.

**JAMII — Civic Action & Participation.** Community-driven solutions covering M-Pesa-powered crowdfunding for local issues (neighborhood security lighting, road repairs, drainage), geofenced ward-level polling following Seoul mVoting's model, gamification engine with points for verified reports (50 pts), confirmed resolutions (100 pts), fund contributions (25 pts per KSh 100), and poll participation (10 pts), neighborhood leaderboards and airtime rewards, and the civic education engine ("Nani Anashughulikia Nini?" directory explaining government structures contextually).

**DHARURA — Emergency Dispatch.** Sub-10-second emergency processing covering one-tap SOS with simultaneous GPS capture and audio/video recording, accelerometer-based crash detection (4G+ threshold), voice-activated emergency command ("Jukwa msaada"), AI-powered emergency classification (medical, security, fire, road accident), simultaneous multi-agency dispatch (NARS ambulance, nearest police OCS, county fire services, NTSA traffic unit), 120-second acknowledgment countdown with automatic escalation to County Commander, and real-time GPS sharing with responding units (verified users only, with explicit consent).

### 3.2 Micro Features (Component-Level Capabilities)

**Identity & Access.** Three-tier anonymity model (Standard pseudonymous device token, Incognito zero-PII with token rotation, Verified via eCitizen OAuth). Anonymity mode selectable per-session and per-report. Device token hash as primary identifier (never the raw token). Automatic ward detection from GPS with manual override.

**Notifications.** Dual-channel delivery (MQTT for connected devices, FCM for sleeping/backgrounded devices). Topic-based geographic targeting (subscribe to your ward, your commute corridors, your county). Notification frequency caps to prevent alert fatigue (maximum 10 per day unless emergency). Quiet hours respect (no non-emergency notifications between 22:00 and 06:00 unless user overrides). Sound/vibration patterns differentiated by category (distinct emergency tone).

**Media Handling.** On-device EXIF stripping before upload (JPEG via ExifInterface, MP4 via custom atom parser). Compression pipeline: photos transcoded to WebP at 80% quality (typically 60–70% size reduction), video transcoded to H.264 720p 30fps (FFmpeg server-side), audio recordings compressed to Opus codec. Upload size limits: 50MB video, 10MB photo, 5MB audio. Thumbnail generation for all media (200×200px WebP). Server-side defense-in-depth re-scrubbing of all metadata regardless of client processing.

**Maps & Geospatial.** MapLibre rendering with OSM base tiles. Offline tile cache for Nairobi metro (pre-loaded, ~50MB). Dynamic overlay layers: incident heatmap (PostGIS density clustering), safety score choropleth (ward-level shading), traffic congestion corridors (color-coded polylines), commitment locations (pin markers with status colors). Ward boundary polygons for all 1,450 Kenyan wards loaded from PostGIS. Point-in-polygon ward detection (Turf.js client-side, ST_Contains server-side).

**Search & Discovery.** Full-text search across incidents and commitments (PostgreSQL tsvector with Kiswahili and English dictionaries). Geographic search (incidents/commitments within X km of a point, within a named ward, along a named road). Category filtering, status filtering, date range filtering. "Near Me" default view showing relevant activity within 5km radius. Agency and official search within the government directory.

**Payments.** M-Pesa STK Push for community crowdfunding contributions (citizen's phone receives payment prompt, confirmed with M-Pesa PIN). M-Pesa B2C for airtime reward disbursements. Lipa Na M-Pesa for business payments from contextual promotion sponsors. All transactions logged with transparent accounting visible to community fund participants. Safaricom Daraja 3.0 API with sandbox environment for development testing.

**Accessibility.** Bilingual interface (English and Kiswahili) with runtime language switching. TalkBack (Android screen reader) full compatibility. Minimum touch target 48dp. High-contrast mode for outdoor visibility. Font scaling support (sp units throughout). RTL layout support (future Arabic/Somali localization). WCAG 2.1 AA color contrast ratios.

---

## Part IV: Build Integration — How Everything Connects

### 4.1 The Monorepo Structure

The entire Jukwa codebase lives in a single GitHub monorepo, organized for clarity and independent deployability:

```
jukwa/
├── android/                          ← Native Android app
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── kotlin/ke/jukwa/
│   │   │   │   ├── core/             ← Shared utilities, extensions
│   │   │   │   ├── data/             ← Repository implementations
│   │   │   │   │   ├── local/        ← Room DAOs, entities
│   │   │   │   │   ├── remote/       ← API clients, DTOs
│   │   │   │   │   └── sync/         ← Sync engine, WorkManager
│   │   │   │   ├── domain/           ← Use cases, domain models
│   │   │   │   │   ├── incident/
│   │   │   │   │   ├── commitment/
│   │   │   │   │   ├── traffic/
│   │   │   │   │   ├── emergency/
│   │   │   │   │   └── civic/
│   │   │   │   ├── presentation/     ← ViewModels, UI state
│   │   │   │   │   ├── home/
│   │   │   │   │   ├── report/
│   │   │   │   │   ├── baraza/
│   │   │   │   │   ├── traffic/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   └── settings/
│   │   │   │   ├── privacy/          ← EXIF scrubber, encryption
│   │   │   │   ├── mqtt/             ← MQTT client manager
│   │   │   │   └── sdui/             ← Server-driven UI renderer
│   │   │   └── res/                  ← Layouts, strings, drawables
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
├── services/                         ← Backend microservices
│   ├── incident/                     ← Incident Service
│   │   ├── src/
│   │   │   ├── routes/
│   │   │   ├── services/
│   │   │   ├── models/
│   │   │   └── index.ts
│   │   ├── Dockerfile
│   │   └── package.json
│   ├── commitment/                   ← Commitment Service (BARAZA)
│   ├── traffic/                      ← Traffic Intelligence Service
│   ├── emergency/                    ← Emergency Dispatch Service
│   ├── civic/                        ← Civic Action Service
│   ├── identity/                     ← Identity & Auth Service
│   ├── notification/                 ← Notification Service
│   ├── payment/                      ← M-Pesa Integration Service
│   ├── accountability/               ← Accountability Dashboard Service
│   ├── ai-classifier/                ← AI Classification Engine
│   └── media/                        ← Media Processing Service
│
├── pwa/                              ← Progressive Web App
│   ├── src/
│   │   ├── app/                      ← Next.js App Router pages
│   │   ├── components/
│   │   ├── lib/
│   │   └── workers/                  ← Service worker, sync
│   └── package.json
│
├── ussd/                             ← USSD/SMS handler
│   ├── src/
│   │   ├── sessions/                 ← USSD session trees
│   │   ├── sms/                      ← SMS parser + NLP
│   │   └── index.ts
│   └── Dockerfile
│
├── whatsapp/                         ← WhatsApp Bot
│   ├── src/
│   │   ├── handlers/
│   │   ├── nlp/
│   │   └── index.ts
│   └── Dockerfile
│
├── infra/                            ← Infrastructure configs
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   ├── nginx/
│   │   └── conf.d/
│   ├── mosquitto/
│   │   └── config/
│   ├── postgres/
│   │   ├── init.sql                  ← Full schema
│   │   └── seed/                     ← Ward boundaries, agencies
│   └── monitoring/
│       ├── prometheus.yml
│       └── grafana/
│
├── shared/                           ← Shared TypeScript types/utils
│   ├── types/                        ← Shared interfaces
│   ├── constants/                    ← Category codes, status enums
│   └── validators/                   ← Shared validation schemas
│
├── data/                             ← Static data files
│   ├── wards.geojson                 ← All 1,450 ward boundaries
│   ├── agencies.json                 ← Government agency directory
│   ├── routing-rules.json            ← Category-to-agency mapping
│   └── sla-defaults.json             ← Default SLA per category
│
├── .github/
│   └── workflows/
│       ├── ci.yml                    ← Lint + test on PR
│       ├── deploy-backend.yml        ← Backend deployment
│       ├── deploy-android.yml        ← Android build + Play Store
│       └── deploy-pwa.yml            ← PWA deployment
│
└── README.md
```

### 4.2 The Service Communication Map

Services communicate through three patterns, chosen based on latency requirements and coupling tolerance:

**Synchronous REST (Ktor/Fastify HTTP)** handles request-response interactions where the caller needs an immediate answer. The Android app calls the Incident Service to submit a report and receives a case reference UUID in the response. The Commitment Service calls the AI Classifier to categorize a new commitment before persisting it. The Accountability Service calls PostgREST (Supabase) to query aggregate scorecard data.

**Asynchronous Events (MQTT publish/subscribe)** handles fire-and-forget notifications where the producer does not need to know who consumes the event or when. The Incident Service publishes a new-incident event; the Notification Service subscribes and sends push notifications; the Analytics Service subscribes and updates dashboards; the Traffic Service subscribes to filter traffic-related incidents into its processing pipeline. Decoupling through MQTT means adding a new consumer (say, a media monitoring service) requires zero changes to any existing producer.

**Asynchronous Jobs (BullMQ on Redis)** handles deferred processing where work must be done reliably but not immediately. Media transcoding jobs, scheduled escalation checks (every hour, BullMQ scans for commitments past their SLA deadline), report generation jobs (quarterly accountability reports), and bulk notification fan-out (a ward-level alert goes to thousands of subscribers in batches).

```
┌────────────┐     REST      ┌────────────┐
│  Android   │──────────────►│  NGINX     │
│  App       │◄──────────────│  Gateway   │
└────────────┘               └─────┬──────┘
                                   │ routes to:
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
             ┌───────────┐ ┌───────────┐ ┌───────────┐
             │ Incident  │ │ Commit-   │ │ Traffic   │  ... etc
             │ Service   │ │ ment Svc  │ │ Service   │
             └─────┬─────┘ └─────┬─────┘ └─────┬─────┘
                   │             │             │
          publishes│    publishes│    publishes│
                   ▼             ▼             ▼
             ┌─────────────────────────────────────┐
             │         MQTT BROKER (Mosquitto)      │
             │                                      │
             │  Topics:  alerts/*, baraza/*,         │
             │           traffic/*, incidents/*      │
             └──────┬──────────────┬────────────────┘
                    │              │
           subscribes│    subscribes│
                    ▼              ▼
             ┌───────────┐ ┌───────────────┐
             │Notification│ │ Accountability │
             │ Service    │ │ Service        │
             │ (FCM push) │ │ (Dashboards)   │
             └────────────┘ └────────────────┘

             ┌─────────────────────────────────────┐
             │       BullMQ JOB QUEUE (Redis)       │
             │                                      │
             │  Queues: media-processing,            │
             │          escalation-checks,           │
             │          report-generation,            │
             │          notification-fanout           │
             └─────────────────────────────────────┘
```

---

## Part V: Micro & Macro Instance Definitions

### 5.1 Macro Instances (System-Level Operational Units)

A "macro instance" is a complete, independently deployable unit of the Jukwa platform that can serve a defined user population. Understanding macro instances is critical for scaling strategy and disaster recovery.

**The Nairobi Instance** is the MVP deployment. It consists of one complete backend stack (all microservices, databases, MQTT broker, Redis, and object storage) running on a single Docker Compose deployment on a 4-vCPU, 16GB RAM server hosted at a Kenyan data center. It serves Nairobi County's 17 sub-counties and approximately 100 wards, handling an estimated 10,000–50,000 active users in the first 6 months. The Nairobi instance includes pre-loaded ward boundaries, agency routing rules for Nairobi-relevant agencies, and pre-cached map tiles for the metro area. This is the "prove it works" instance.

**The National Instance** is the scaled deployment serving all 47 counties. It replaces the single-server Docker Compose with a K3s cluster across 2–3 servers, separates PostgreSQL and MongoDB onto dedicated database servers (or managed services), upgrades Mosquitto to EMQX for horizontal MQTT scaling, and adds read replicas for the PostgreSQL database to handle dashboard query load without impacting write performance. Ward boundaries for all 1,450 wards are loaded, agency routing covers all national and county government entities, and map tiles are pre-cached for all major urban centers.

**The Citizen Vault Instance** runs on completely separate infrastructure — different data center, different operator, different administrative domain. It consists of a GlobaLeaks installation with Tor bridge support, encrypted evidence storage, and a unidirectional API to push sanitized reports to the main Jukwa instance. The Vault instance has no read access to Jukwa's databases and Jukwa has no read access to the Vault's evidence store. This architectural separation is what makes the anonymity guarantee credible.

### 5.2 Micro Instances (Component-Level Operational Units)

A "micro instance" is a single service container or functional module within a macro instance. Each micro instance has a defined responsibility boundary, health check endpoint, independent logging, and can be restarted without affecting other micro instances.

**incident-service** (Port 3001). Responsibility: CRUD for incident reports, state machine management, media attachment handling. Dependencies: PostgreSQL (read/write), Redis (caching), MQTT (publish), BullMQ (enqueue media jobs). Health check: `GET /health` returns 200 with database connection status. Resource allocation: 512MB RAM, 0.5 CPU.

**commitment-service** (Port 3002). Responsibility: BARAZA commitment lifecycle, baraza session management, SLA clock management, escalation scheduling. Dependencies: PostgreSQL, Redis, MQTT, BullMQ. Unique: runs a BullMQ worker that checks for SLA breaches every 15 minutes. Resource allocation: 512MB RAM, 0.5 CPU.

**traffic-service** (Port 3003). Responsibility: traffic data aggregation, congestion scoring, predictive routing. Dependencies: MongoDB (telemetry read/write), PostgreSQL (incident correlation), MQTT (subscribe to sensor feeds, publish alerts), Redis (corridor score caching). Resource allocation: 1GB RAM, 1 CPU (heavier due to continuous stream processing).

**emergency-service** (Port 3004). Responsibility: emergency classification, multi-agency dispatch, acknowledgment tracking. Dependencies: PostgreSQL, MQTT (high-priority publish), FCM (direct device messaging). Unique: this service has the highest availability requirement — it runs with a health check interval of 5 seconds (vs. 30 seconds for other services) and auto-restarts within 10 seconds of failure. Resource allocation: 512MB RAM, 0.5 CPU.

**civic-service** (Port 3005). Responsibility: crowdfunding campaigns, poll management, gamification scoring, leaderboards. Dependencies: PostgreSQL, M-Pesa Daraja API, Africa's Talking (airtime rewards), MQTT. Resource allocation: 512MB RAM, 0.5 CPU.

**identity-service** (Port 3006). Responsibility: device token registration, pseudonym management, token rotation for Incognito Mode, eCitizen OAuth flow for Verified Mode. Dependencies: PostgreSQL, Redis (token caching). Resource allocation: 256MB RAM, 0.25 CPU.

**notification-service** (Port 3007). Responsibility: MQTT-to-FCM bridge, notification batching, quiet hours enforcement, frequency capping. Dependencies: MQTT (subscribe to all alert topics), FCM API, Redis (frequency counters). Resource allocation: 512MB RAM, 0.5 CPU.

**payment-service** (Port 3008). Responsibility: M-Pesa STK Push initiation, callback handling, transaction logging, B2C airtime disbursement. Dependencies: PostgreSQL (transaction log), Daraja 3.0 API, Africa's Talking (airtime). Resource allocation: 256MB RAM, 0.25 CPU.

**accountability-service** (Port 3009). Responsibility: dashboard data generation, materialized view refresh, report generation, public API for media and researchers. Dependencies: PostgreSQL (read-heavy), Redis (dashboard caching). Unique: runs scheduled jobs — hourly scorecard refresh, daily trend calculations, quarterly PDF report generation. Resource allocation: 1GB RAM, 0.5 CPU.

**ai-classifier** (Port 3010). Responsibility: incident and commitment classification, severity scoring, agency routing resolution. Dependencies: ONNX Runtime (CPU inference), routing rules JSON. Stateless — can be horizontally scaled by simply running more container instances. Resource allocation: 1GB RAM, 1 CPU (CPU-bound inference).

**media-service** (Port 3011). Responsibility: upload handling, EXIF stripping, image transcoding (sharp), video transcoding (FFmpeg), thumbnail generation, object storage management. Dependencies: MinIO/S3, BullMQ (consumes media processing jobs). Resource allocation: 2GB RAM, 2 CPU (media processing is the most resource-intensive workload).

**ussd-handler** (Port 3012). Responsibility: Africa's Talking USSD webhook processing, session tree management, SMS parsing and NLP classification. Dependencies: Africa's Talking API, Incident Service (REST), Commitment Service (REST). Resource allocation: 256MB RAM, 0.25 CPU.

**whatsapp-bot** (Port 3013). Responsibility: WhatsApp Business API webhook processing, NLP conversation management, media receipt and forwarding. Dependencies: WhatsApp Business API, UlizaLlama/Claude API (NLP), Incident Service, Commitment Service. Resource allocation: 512MB RAM, 0.5 CPU.

---