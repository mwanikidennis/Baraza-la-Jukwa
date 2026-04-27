# JUKWA — Technical Build Bible

**Architecture Maps, Tech Stack, Feature Matrix, Build Integration & Solopreneur Vibecoder Strategy**

**Version 1.0 | March 2026**
**Document Type: Engineering Reference — Build-Ready Specification**

---

## Part I: Architecture Maps, Flows & Logic

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

## Part III: Complete Feature Matrix

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

## Part VI: The Solopreneur Vibecoder Build Strategy

### 6.1 The Reality Check

Dennis, this section is written directly for you. You are one person with deep Kenyan civic tech domain expertise, strong Android and Firebase skills, growing backend knowledge, and access to AI coding assistants (Claude, Gemini, Cursor, GitHub Copilot) that effectively multiply your output by 3–5×. The architecture described in Parts I through V is the full vision — the system that emerges at maturity. You do not build that on day one. You build a sequence of progressively more capable versions, each independently valuable and each funding or validating the next.

The vibecoder approach means you treat AI coding assistants as your engineering team. You are the architect, product manager, and domain expert. Claude writes your Kotlin, your TypeScript, your SQL, your Docker configs. You review, test, iterate, and deploy. This is not a shortcut — it is a legitimate, scalable development methodology used by thousands of solo builders shipping real products. The key discipline is knowing what to build first, what to defer, and what to skip entirely until the market demands it.

### 6.2 Sprint Zero: The Foundation (Weeks 1–2)

Before writing a single feature, you establish the development environment that will carry you through the entire build.

Set up the GitHub monorepo with the directory structure from Section 4.1. Initialize the Android project with Kotlin, Jetpack Compose, Hilt, Room, and the build configuration targeting API 23 minimum. Initialize the first backend service (incident-service) as a Fastify TypeScript project. Create the Docker Compose file with PostgreSQL/PostGIS, Redis, and Mosquitto. Write the database initialization script that creates the core tables (citizens, incidents, wards) and loads ward boundary GeoJSON for Nairobi's 85 wards (source: IEBC boundary data via Kenya Open Data). Set up GitHub Actions for basic CI (lint and test on every push).

The critical output of Sprint Zero is: `docker-compose up` launches the complete local development environment, and `./gradlew assembleDebug` produces an installable Android APK that connects to the local backend. No features yet — just the plumbing.

### 6.3 Phase 1: The "One Killer Feature" MVP (Weeks 3–8)

The MVP is not a miniature version of the full platform. It is one feature, executed exceptionally well, that proves the core value proposition to real users in a real ward. That feature is **incident reporting with public tracking**.

**What you build:** The Android app has three screens. The Home screen shows a map of the user's ward with colored pins for recent incidents. The Report screen lets the citizen place a pin on the map, select a category (security, traffic, infrastructure, other), type or voice-dictate a description, optionally attach a photo, and submit. The My Reports screen shows the citizen's own submissions with their current status (Submitted, Acknowledged, In Progress, Resolved). One backend service (incident-service) handles everything. PostGIS stores the reports. The app generates a device token on first launch (Standard Mode anonymity). No MQTT yet — the app polls the API every 5 minutes for status updates. No USSD, no PWA, no WhatsApp, no M-Pesa, no BARAZA, no traffic intelligence.

**What you skip (for now):** AI classification (manually route reports during MVP). Incognito Mode and Citizen Vault (add when you have users who need it). Media transcoding (accept raw uploads, transcode later). FCM push notifications (polling is fine for a small user base). Every integration except the core PostgreSQL database.

**What you test:** Deploy the APK to 20–50 people in a single Nairobi ward (choose a ward where you have connections — Kiambu or Nairobi's sub-counties where you operate professionally). Observe: do people actually submit reports? What categories dominate? Do they check back for status updates? What is the feedback on the interface? This user feedback is worth more than any amount of architectural planning.

**The vibecoder workflow for Phase 1:** Use Claude to generate the Room entity classes from your PostgreSQL schema. Use Claude to generate the Fastify route handlers for CRUD operations on incidents. Use Claude to generate the Jetpack Compose screens from wireframe descriptions. Use Claude to write the Ktor HTTP client that calls your API. You personally handle: the MapLibre integration (map rendering is finicky and needs human judgment for UX), the offline sync logic (this is Jukwa's core differentiator and needs careful testing), and the PostGIS spatial queries (these are performance-critical and need your understanding of the data patterns). Test on your own Samsung device on Safaricom 3G. If it works there, it works everywhere.

### 6.4 Phase 2: The Civic Power Layer (Weeks 9–16)

With a working MVP and real user feedback, you add the features that transform a simple reporting tool into a civic platform.

**Add FCM push notifications** so users get alerted about new incidents in their ward without opening the app. This is the single feature most likely to drive daily active usage. Use Firebase's free tier — it handles millions of messages at no cost.

**Add the BARAZA commitment capture.** This is your differentiation from every other civic app in Kenya. A second screen in the app lets citizens record government promises with the structured commitment object (who promised, what, to whom, by when). The commitment-service manages the lifecycle. You personally seed the government agency directory with the 50 most relevant national and Nairobi County agencies. The Accountability Dashboard starts as a simple list view in the app showing commitments per ward with status colors (green = fulfilled, yellow = in progress, red = overdue). No fancy heatmaps yet — just the data.

**Add USSD access via Africa's Talking.** This is your reach multiplier. Write the USSD session handler as a single Fastify service responding to AT webhooks. The USSD flow is simple: language selection, then "1. Report Issue, 2. Check Status, 3. Emergency." Use the AT Simulator for development — it is free and lets you test the full USSD flow without carrier costs. Deploy the USSD handler and apply for a shared shortcode through AT (approximately KSh 10,000 setup plus KSh 1/session).

**Add the AI Classification Engine.** Start with a rule-based classifier — a JSON mapping of keyword patterns to categories and agencies. "maji" or "water" → Infrastructure / County Water Department. "polisi" or "police" or "robbery" → Security / Local OCS. "barabara" or "road" or "pothole" → Infrastructure / KURA or County Roads. This rule engine handles 70–80% of reports correctly. Use Claude to generate the comprehensive rule set from your 16 years of probation service domain knowledge — you know every Kenyan government agency and its mandate.

**The vibecoder workflow for Phase 2:** USSD session tree and SMS parsing are perfect tasks for AI generation — the logic is well-defined and pattern-based. The BARAZA commitment lifecycle state machine is also well-suited to AI generation — you describe the states and transitions, Claude produces the TypeScript implementation with BullMQ scheduled escalation checks. You personally handle: the FCM integration testing (notification delivery is notoriously flaky and needs manual verification across device types), the Africa's Talking production setup (carrier integration involves real money and real SLAs), and the agency routing rules (your domain expertise is the moat here).

### 6.5 Phase 3: The Engagement Engine (Weeks 17–24)

With a functioning incident and commitment pipeline, you add the features that drive sustained engagement and set up the sustainability model.

**Add M-Pesa integration** for community crowdfunding. Use the Daraja 3.0 sandbox for development (free, simulates the full STK Push flow). When a community issue accumulates enough upvotes, any citizen can launch a fund. STK Push prompts contributors, and transparent accounting shows every contribution and expenditure. This is the feature that gets media attention — "Kenyans are crowdfunding their own road repairs through Jukwa" is a story every Nairobi newsroom wants to write.

**Add the gamification engine.** Points, ward leaderboards, airtime rewards. This is technically simple (a points table in PostgreSQL, a leaderboard materialized view, airtime disbursement via Africa's Talking API) but psychologically powerful. Design the point values to incentivize the behaviors that make the platform valuable: verified reports, commitment captures, verification votes, fund contributions.

**Add the PWA.** Next.js, Workbox, MapLibre GL JS. The PWA serves users who discover Jukwa via social media sharing (a ward's accountability scorecard shared on Twitter/X links to the PWA dashboard). It also serves the public Accountability Dashboard — journalists and researchers access ward scorecards and agency performance data through the PWA without installing anything.

**Add the WhatsApp chatbot.** WhatsApp Business API integration lets users report incidents and check commitment status via the messaging platform they already use daily. Use Claude's API or Gemini's API for the NLP layer — the chatbot receives a WhatsApp message, calls the LLM to extract intent and entities, and calls the appropriate Jukwa service. Start with structured flows (menu-based options like USSD) and add free-text NLP progressively.

### 6.6 Phase 4: The Platform Maturity (Weeks 25–36)

**Add MQTT for real-time alerts.** Replace the polling mechanism with a persistent MQTT connection. This is when Jukwa becomes truly real-time — incidents appear on the map within seconds of submission, traffic alerts push instantly, and commitment status changes notify citizens immediately. Start with Mosquitto (free, tiny footprint).

**Add the Citizen Vault.** Partner with a Kenyan civil society organization (KHRC, Article 19, TI Kenya) to host the GlobaLeaks-based relay on independent infrastructure. This enables Incognito Mode for whistleblowing and corruption reporting — the feature that makes Jukwa genuinely different from any government platform.

**Add traffic intelligence.** Integrate the Waze CCP data feed (free, requires application and approval). Add the traffic map layer with corridor congestion scores. Connect to the Samsung ITS sensor feeds as junctions come online. This is the feature that drives daily habitual usage — Nairobians check Jukwa every morning to plan their commute.

**Add the full Accountability Dashboard.** The national governance heatmap, agency scorecards, official performance records, and quarterly report generation. This is the feature that attracts development partner funding and media partnerships — the most granular governance performance data Kenya has ever produced.

### 6.7 The Vibecoder's Daily Workflow

A typical development day looks like this. Morning: review overnight Sentry error reports and user feedback. Identify the highest-priority bug or feature. Open Claude (or Cursor with Claude) and describe the task with full context — paste the relevant TypeScript interface, the Kotlin ViewModel, the SQL schema, and the user story. Claude generates the implementation. You review it critically — does it handle offline? Does it respect the anonymity mode? Does it work on Android 6.0? Does it handle Safaricom 3G latency? You iterate 2–3 times until the code meets your standards. Afternoon: manually test on a real device on a real Kenyan mobile network. Evening: commit, push, let CI validate, and deploy to staging.

The critical discipline is resisting the temptation to build everything simultaneously. Each phase is a complete, shippable product. Phase 1 alone — a simple incident reporting app for one Nairobi ward — is more useful to citizens than 90% of Kenyan civic tech projects that tried to build the full vision and shipped nothing. Ship Phase 1 in 8 weeks. Get real users. Learn from them. Then build Phase 2 with real knowledge instead of assumptions.

### 6.8 Cost Budget for the Solo Build

Phase 1 (Months 1–2): Domain registration KSh 2,000. Cloudflare free tier: KSh 0. VPS (4-vCPU, 16GB, Safaricom Cloud): KSh 30,000/month. Google Play Developer Account (one-time): KSh 3,250. Total: approximately KSh 65,250 for two months.

Phase 2 (Months 3–4): Africa's Talking shortcode setup: KSh 10,000. AT USSD sessions (~5,000/month): KSh 5,000/month. AT SMS (~2,000/month): KSh 1,600/month. Total: approximately KSh 83,450 cumulative through four months.

Phase 3 (Months 5–6): M-Pesa Daraja API (transaction-based, zero monthly fee): percentage per transaction. WhatsApp Business API (Meta hosting): $0.005–0.08 per conversation. Second VPS for growth: KSh 30,000/month. Total: approximately KSh 155,000 cumulative through six months.

Full Year 1 (including Phase 4 scaling): approximately KSh 400,000–500,000 total infrastructure cost (roughly $3,000–3,800 USD). This is a bootstrappable civic tech platform built to serve millions, running on infrastructure that costs less than a mid-range laptop per month.

---

## Part VII: Risk Register & Mitigations

**Government hostility risk.** Kenya's CMCA 2018 and 2024 amendments give broad content-blocking powers. Mitigation: anchor every feature in constitutional mandates (Articles 10, 35, 174), maintain relationship with the JIM team (BARAZA extends their mission), implement offline/SMS fallback that survives internet shutdowns, and host the Citizen Vault outside government reach.

**Adoption stall risk.** Citizens download the app but stop using it because nothing changes. Mitigation: launch in a ward where you have government contacts who will actually respond to reports (prove the loop works before scaling), gamification rewards create habitual engagement, traffic intelligence drives daily utility independent of government responsiveness.

**Data quality risk.** Anonymous reporting enables false or spam reports. Mitigation: graduated anonymity (Standard Mode provides minimal accountability), community corroboration for baraza captures (3 independent confirmations), AI classifier flags anomalous patterns, and human moderation queue for flagged reports.

**Technical debt risk.** Solo vibecoding accumulates shortcuts. Mitigation: TypeScript's type system catches errors at compile time, Fastify's schema validation rejects malformed data at the API boundary, Room's compile-time query verification catches database errors before runtime, and CI/CD runs tests on every push. Write tests for the three most critical paths first: incident submission, commitment lifecycle transitions, and offline sync.

**Legal risk.** The DPA's anonymization boundary is untested. Mitigation: voluntary ODPC registration, DPIA conducted before launch, legal opinion from a DPA-specialist Kenyan law firm (budget KSh 50,000–100,000), and design the anonymization to be genuinely robust rather than legally clever.

---

## Appendix: The Build Sequence Cheat Sheet

```
WEEK  1-2:   Sprint Zero — Repo, Docker, Database, CI
WEEK  3-4:   Android skeleton — Map, Report form, Room DB
WEEK  5-6:   Backend — Incident Service, PostGIS queries, REST API
WEEK  7-8:   Integration — App talks to API, offline sync, deploy beta
             ────── MVP SHIPS TO 50 USERS ──────
WEEK  9-10:  FCM push notifications
WEEK 11-12:  BARAZA commitment capture + lifecycle
WEEK 13-14:  USSD via Africa's Talking
WEEK 15-16:  AI rule-based classifier + agency routing
             ────── PHASE 2 SHIPS TO 500 USERS ──────
WEEK 17-18:  M-Pesa crowdfunding integration
WEEK 19-20:  Gamification engine + leaderboards
WEEK 21-22:  PWA + public Accountability Dashboard
WEEK 23-24:  WhatsApp chatbot
             ────── PHASE 3 SHIPS TO 5,000 USERS ──────
WEEK 25-28:  MQTT real-time + Citizen Vault partnership
WEEK 29-32:  Traffic intelligence + Waze CCP
WEEK 33-36:  Full dashboard + national scaling
             ────── PHASE 4: PUBLIC LAUNCH ──────
```

---

*This document supersedes all previous architectural drafts. It is the single source of truth for what Jukwa is, how it works, what technologies power it, and how one determined Kenyan builder ships it to the world.*
