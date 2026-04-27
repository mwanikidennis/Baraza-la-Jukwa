# JUKWA — Architectural & Structural Integration Framework

**Version 1.0 | March 2026**
**Classification: Confidential — Development Reference**

---

## Preamble

This document constitutes the definitive technical architecture, component specification, and build integration framework for Jukwa — Kenya's unified citizen engagement platform. It synthesizes original research into Kenya's security engagement landscape (Jukwaa la Usalama forums, NPSIMS, Fichua, Nyumba Kumi), Nairobi's traffic management ecosystem (Samsung ITS, Huawei Safe City, Ma3Route, NaMATA BRT), global civic platform benchmarks (NYC 311, Singapore OneService, Seoul mVoting, Ushahidi, GlobaLeaks), and rigorous technical validation of every proposed component against Kenyan infrastructure realities — mobile network latency, device demographics, cloud availability, and the Data Protection Act 2019.

Every architectural decision documented here has been stress-tested against three non-negotiable constraints: it must work on a Samsung Galaxy A05 running Android 10 on Safaricom 3G in Kibera; it must comply with the Kenya Data Protection Act 2019 and the Computer Misuse and Cybercrimes Act 2018; and it must be buildable by a small team shipping an MVP within six months.

---

## 1. System Philosophy & Design Principles

Jukwa operates under five foundational principles that govern every component, service, and user flow described in this document.

**Progressive Access, Not Progressive Exclusion.** Kenya has 41.5 million smartphone users, 30.6 million feature phone users, and internet penetration ranging from 56.6% in urban areas down to 25% in rural counties. No single access channel can serve this population. Jukwa therefore implements a tri-tier access model — native Android app as the primary experience, a Progressive Web App as the lightweight alternative, and USSD/SMS via Africa's Talking APIs as the universal fallback. Every critical function (incident reporting, traffic alerts, emergency contacts) must be operable from all three tiers. A farmer in Garissa reporting cattle theft via USSD and a developer in Westlands uploading dashcam footage via the app must both reach the same backend pipeline.

**Anonymity as Spectrum, Not Binary.** The Gemini contribution proposed a "zero-PII" architecture with mandatory Tor routing. Research reveals this is legally elegant but operationally impractical — Tor adds 3× latency on Safaricom 4G, is unusable on 3G, and increases data costs by 30–50%. More critically, total anonymity undermines report verification: Ushahidi's 2022 Kenya elections deployment published only 13.3% of anonymous submissions because unverified reports are unreliable. Jukwa therefore implements a graduated anonymity model. Standard Mode collects minimal pseudonymous identifiers (device token, approximate ward-level location) for everyday reporting. Incognito Mode strips all metadata client-side, routes through encrypted relay infrastructure, and stores zero server-side identifiers — designed for corruption reports, whistleblowing, and high-risk submissions. Verified Mode allows optional identity verification via eCitizen OAuth for users who want accountability credit, gamification points, or direct government response tracking. Users choose their mode per-session, per-report. The system never forces identification.

**Intelligent Routing Over User Navigation.** Singapore's OneService app routes 85% of citizen reports to the correct agency without human intervention. Kenya's fragmented institutional landscape — NPS, DCI, NTSA, 47 county governments, NaMATA, NEMA, Kenya Power — makes this even more critical. Citizens should never need to know whether a broken traffic light goes to NTSA, Kenya Urban Roads Authority, or the Nairobi County Government. Jukwa's AI classification layer handles routing automatically based on incident type, location, and severity.

**Transparency by Default.** Every report submitted through Jukwa generates a trackable case reference. Government response times are publicly visible in aggregate. Ward-level resolution rates become civic data. This follows FixMyStreet's model, where 50% of users were first-time civic reporters specifically because they could see outcomes. Transparency builds the trust that a security-adjacent platform requires.

**Offline-First, Sync-Later.** Kenya experiences frequent network interruptions — from routine congestion on Safaricom towers in high-density areas to deliberate shutdowns during political crises (June 2024 #RejectFinanceBill, June 2025 protests). Jukwa's core functions must operate fully offline, with intelligent sync when connectivity returns. Reports queue locally with GPS timestamps, media compresses to WebP, and the sync engine prioritizes by severity (emergencies sync first on any available connection including SMS fallback).

---

## 2. High-Level System Architecture

Jukwa's architecture follows a hub-and-spoke model with six distinct layers, each independently deployable and horizontally scalable.

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CLIENT TIER                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐   │
│  │ Android  │  │   PWA    │  │  USSD /  │  │   WhatsApp Bot   │   │
│  │ Native   │  │ (Lite)   │  │   SMS    │  │  (Chatbot NLP)   │   │
│  │ Kotlin   │  │ Next.js  │  │  AT API  │  │  UlizaLlama/     │   │
│  │ Jetpack  │  │ Workbox  │  │          │  │  Claude/Gemini    │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └───────┬──────────┘   │
│       │              │              │                │              │
└───────┼──────────────┼──────────────┼────────────────┼──────────────┘
        │              │              │                │
        ▼              ▼              ▼                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     GATEWAY & ROUTING TIER                          │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │            NGINX / OpenResty API Gateway                     │   │
│  │    Rate Limiting │ JWT Auth │ Request Routing │ TLS 1.3      │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────┐  ┌────────────────────────────────────┐   │
│  │   Cloudflare CDN     │  │  Africa's Talking SMS/USSD Router  │   │
│  │   (Kenya PoPs)       │  │  (Safaricom, Airtel, Telkom)       │   │
│  └──────────────────────┘  └────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     APPLICATION SERVICES TIER                       │
│                                                                     │
│  ┌────────────┐ ┌──────────────┐ ┌────────────┐ ┌──────────────┐  │
│  │  Incident  │ │   Traffic    │ │   Civic    │ │  Emergency   │  │
│  │  Service   │ │  Intelligence│ │  Action    │ │  Dispatch    │  │
│  │            │ │   Service    │ │  Service   │ │  Service     │  │
│  └─────┬──────┘ └──────┬───────┘ └─────┬──────┘ └──────┬───────┘  │
│        │               │               │               │           │
│  ┌─────┴───────────────┴───────────────┴───────────────┴────────┐  │
│  │              AI Classification & Routing Engine               │  │
│  │    Incident Triage │ Agency Routing │ Severity Scoring        │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                     │
│  ┌────────────┐ ┌──────────────┐ ┌────────────┐ ┌──────────────┐  │
│  │  Identity  │ │ Notification │ │  Payment   │ │  Analytics   │  │
│  │  Service   │ │   Service    │ │  Service   │ │  & Insights  │  │
│  │ (Pseudonym)│ │ (FCM+MQTT)   │ │ (M-Pesa)   │ │  Service     │  │
│  └────────────┘ └──────────────┘ └────────────┘ └──────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  MESSAGING & REAL-TIME TIER                         │
│  ┌──────────────────────┐  ┌────────────────────────────────────┐   │
│  │  MQTT Broker          │  │  Firebase Cloud Messaging (FCM)    │   │
│  │  Mosquitto (MVP)      │  │  Push Notifications + Topic Sub    │   │
│  │  → EMQX (Scale)       │  │  Device Wake + Background Alerts   │   │
│  │  QoS 1 Civic Alerts   │  │                                    │   │
│  └──────────────────────┘  └────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     DATA & STORAGE TIER                             │
│                                                                     │
│  ┌───────────────────┐  ┌──────────────────┐  ┌─────────────────┐  │
│  │  PostgreSQL 15 +  │  │    MongoDB 7     │  │   Redis 7       │  │
│  │  PostGIS 3.3      │  │  (Telemetry TS)  │  │  (Session Cache │  │
│  │                   │  │                  │  │   Rate Limits   │  │
│  │  Incidents        │  │  Traffic Sensors  │  │   Geofence      │  │
│  │  Citizens (pseudo)│  │  MQTT Telemetry   │  │   Pub/Sub)      │  │
│  │  Civic Insights   │  │  IoT Streams      │  │                 │  │
│  │  Spatial Indexes  │  │                  │  │                 │  │
│  └───────────────────┘  └──────────────────┘  └─────────────────┘  │
│                                                                     │
│  ┌───────────────────┐  ┌──────────────────────────────────────┐   │
│  │  Object Storage   │  │  Offline Sync Queue (BullMQ/Redis)   │   │
│  │  (MinIO / S3)     │  │  Priority: EMERGENCY > SECURITY >    │   │
│  │  Scrubbed Media   │  │  TRAFFIC > CIVIC > GENERAL            │   │
│  └───────────────────┘  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  PRIVACY & ANONYMITY TIER                           │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                 Citizen Vault Relay                           │   │
│  │  GlobaLeaks-based │ NGO-hosted │ Evidence Backup             │   │
│  │  Optional Tor via Bridges │ ChaCha20 Encrypted FS            │   │
│  └──────────────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │              On-Device Privacy Engine                         │   │
│  │  EXIF Strip │ GPS Fuzzing │ Device Token Rotation             │   │
│  │  Local Report Encryption │ Metadata Destruction               │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
        │
        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                EXTERNAL INTEGRATIONS TIER                           │
│                                                                     │
│  ┌───────────┐ ┌────────────┐ ┌───────────┐ ┌──────────────────┐  │
│  │  NPSIMS   │ │ NTSA TIMS  │ │ GavaConnect│ │  Samsung ITS     │  │
│  │  Police   │ │ Transport  │ │ eCitizen   │ │  Traffic Mgmt    │  │
│  │  API      │ │ API        │ │ OAuth      │ │  Centre Feed     │  │
│  └───────────┘ └────────────┘ └───────────┘ └──────────────────┘  │
│  ┌───────────┐ ┌────────────┐ ┌───────────┐ ┌──────────────────┐  │
│  │ M-Pesa    │ │  Waze CCP  │ │ County    │ │  Emergency Svcs  │  │
│  │ Daraja 3.0│ │ Traffic    │ │ Govt APIs │ │  999/112/114     │  │
│  │ STK Push  │ │ Data Feed  │ │           │ │  NARS Ambulance   │  │
│  └───────────┘ └────────────┘ └───────────┘ └──────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Client Tier — Detailed Component Specifications

### 3.1 Native Android Application (Primary Experience)

The native Android app serves as Jukwa's flagship interface, targeting the 91–94% Android market share in Kenya. The build specification is designed around the device reality: Samsung Galaxy A05 (Unisoc SC9863A, 3GB RAM, KSh 13,000) and Tecno Spark 20 (MediaTek Helio G85, 4GB RAM) represent the median Kenyan smartphone.

**Build Configuration**

The app is written in Kotlin using Jetpack Compose for declarative UI. The minimum SDK is API 23 (Android 6.0), covering 99%+ of active Kenyan devices. The target SDK tracks the latest stable release. The APK size target is under 15MB using Android App Bundle with on-demand feature delivery — the map module (largest component, ~8MB of offline tiles) downloads post-install. R8 full-mode shrinking and resource optimization bring the initial install footprint below 10MB.

**Offline-First Data Architecture**

Room (SQLite) serves as the local persistence layer, holding all user-created reports, cached traffic data, ward boundaries, emergency contacts, and notification history. The sync engine uses WorkManager with exponential backoff, prioritizing queued items by a five-level severity classification: EMERGENCY (immediate sync on any connection including SMS), SECURITY (sync within 60 seconds of connectivity), TRAFFIC (sync within 5 minutes), CIVIC (sync within 30 minutes), and GENERAL (sync on WiFi or within 24 hours). Conflict resolution follows a last-write-wins model with server-side timestamps, except for incident reports which use append-only semantics — no report is ever overwritten, only status-updated.

**Device-Adaptive Rendering (Server-Driven UI)**

Rather than Gemini's proposed AI-generated dynamic UI (which requires GPU-class inference impractical on budget devices), Jukwa implements Server-Driven UI (SDUI) following the patterns proven at Airbnb (Ghost Platform), Flipkart, and Facebook Lite.

The server delivers JSON layout descriptors specifying which pre-built native components to render, in what order, with what data bindings. The client app contains a library of composable UI blocks — map cards, alert banners, incident forms, traffic widgets, SOS screens — that the server assembles contextually. During a standard commute, the server sends a layout emphasizing the traffic map, route alerts, and toll payment shortcuts. During a detected emergency (accelerometer impact detection or manual SOS trigger), the server pushes a high-contrast single-action layout with one-tap video streaming and emergency contact calling.

Three device tiers govern rendering complexity. Devices with 2GB RAM or less (detected via `ActivityManager.isLowRamDevice()`) receive simplified layouts with no animations, smaller WebP thumbnails (max 200KB), and text-priority rendering. Devices with 3–4GB RAM receive standard layouts with moderate animations and compressed media. Devices with 4GB+ RAM receive the full experience with interactive maps, smooth transitions, and high-resolution media previews. The tier detection runs once at startup and caches the result.

**On-Device Privacy Engine**

Every media file captured or selected for upload passes through a local privacy pipeline before leaving the device. The EXIF scrubber strips all metadata tags (GPS coordinates, device model, timestamps, camera settings) using the Android `ExifInterface` API, operating entirely in-memory without writing intermediate files to disk. For Incognito Mode reports, GPS coordinates are fuzzed to ward-level centroids (approximately 1–2 km precision) before any network transmission. The device token (used for push notifications) rotates every 72 hours in Incognito Mode, with the server mapping old tokens to new ones via a one-way hash chain that preserves notification delivery without enabling longitudinal tracking.

### 3.2 Progressive Web App (Lite Experience)

The PWA targets users who cannot or prefer not to install a native app. Built with Next.js and Workbox for service worker management, it delivers core functionality — incident reporting, traffic map viewing, alert subscriptions, and emergency contacts — in a sub-3MB initial payload. Workbox precaches the reporting form, ward boundary GeoJSON, and emergency contact directory for offline access. The traffic map uses MapLibre GL JS with vector tiles served from the Jukwa CDN, falling back to a static image for extremely slow connections.

### 3.3 USSD/SMS Access (Universal Tier)

Africa's Talking provides the USSD gateway, accessible on every phone sold in Kenya without internet connectivity. The USSD session tree follows a maximum three-step depth to accommodate the 182-character session limit and 120-second timeout.

The session flow begins with language selection (English or Kiswahili), then presents a main menu with four options: Report Incident, Traffic Updates, Emergency Contacts, and My Reports. Report Incident branches into category selection (Security, Traffic, Infrastructure, Other), then accepts a text description and optional GPS consent (via cell tower triangulation provided by Africa's Talking). Traffic Updates delivers the three most congested corridors in the user's approximate area (determined by the serving cell tower).

SMS serves as the async fallback. Users text a report to a registered shortcode (e.g., *384*247#) in natural language. A lightweight NLP classifier (running server-side, not on-device) extracts incident type, approximate location from text cues, and urgency indicators, then creates a report in the same pipeline as app-submitted incidents. Confirmation is sent back via SMS with a case reference number.

### 3.4 WhatsApp Chatbot Integration

Given that WhatsApp reaches approximately 97% of Kenyan internet users, a WhatsApp Business API integration provides the highest-reach supplementary channel. The chatbot uses a dual-model NLP approach: UlizaLlama (the Nairobi-developed open-source Swahili-capable LLM) handles Kiswahili input processing and initial classification, while a cloud-hosted model (Claude or Gemini) handles complex triage, multi-turn emergency conversations, and English-language interactions.

The WhatsApp channel supports text reports, photo/video uploads (metadata-stripped server-side before storage), location sharing via WhatsApp's native location pin, and status tracking via case reference number. It does not support Incognito Mode (WhatsApp inherently identifies users via phone number) — users requiring anonymity are directed to the native app or PWA.

---

## 4. Gateway & Routing Tier

### 4.1 API Gateway — NGINX with OpenResty

Kong API Gateway, proposed in the Gemini architecture, was rejected after validation research found zero documented African deployments, heavy PostgreSQL/Cassandra dependency, and enterprise pricing inappropriate for a civic platform. NGINX with the OpenResty Lua extension provides equivalent functionality at approximately 33,591 requests per second with no database dependency, TLS 1.3 termination, JWT validation, rate limiting, and request routing — all on a single $20/month VPS.

The gateway enforces rate limits per device token (100 requests/minute for standard mode, 300/minute for verified users, unlimited for emergency endpoints). JWT tokens issued by the Identity Service authenticate app and PWA requests. USSD and SMS requests authenticate via Africa's Talking webhook signatures. The WhatsApp channel authenticates via Meta's webhook verification.

All traffic terminates TLS at the gateway. Internal service-to-service communication uses mTLS on a private network. No internal service is directly exposed to the internet.

### 4.2 CDN — Cloudflare

Cloudflare provides DDoS protection, edge caching, and low-latency static content delivery via its Nairobi presence (5–10ms median latency from Kenyan devices). Cached assets include map tiles, ward boundary GeoJSON, the PWA shell, and SDUI layout JSON. Dynamic API responses pass through Cloudflare but are not cached. Cloudflare's free tier is sufficient for MVP; Pro tier ($20/month) adds WAF rules and image optimization.

### 4.3 SMS/USSD Router — Africa's Talking

Africa's Talking provides unified access to Safaricom, Airtel, and Telkom Kenya networks via a single API. SDKs are available in Python, Node.js, Java, PHP, Ruby, and Go. USSD sessions cost approximately KSh 1 per session. Bulk SMS for traffic alerts costs KSh 0.50–0.80 per message. The platform supports shortcodes, two-way SMS, and airtime disbursement (relevant for gamification rewards). All traffic logs are retained for 90 days on Africa's Talking infrastructure, then purged.

---

## 5. Application Services Tier

### 5.1 Incident Service

The Incident Service is the platform's core, handling the full lifecycle of citizen reports from submission through resolution. It exposes a RESTful API consumed by all client tiers and internally publishes events to the MQTT broker for real-time distribution.

When a report arrives, the service assigns a UUID, timestamps it with server time (overriding potentially manipulated client timestamps), geotags it using the provided coordinates or cell tower approximation, and passes it to the AI Classification Engine for categorization and routing. The report then enters a state machine: SUBMITTED → CLASSIFIED → ROUTED → ACKNOWLEDGED → IN_PROGRESS → RESOLVED → VERIFIED (optional citizen confirmation) or ESCALATED (if no government response within SLA). Each state transition generates an MQTT event that triggers push notifications to the reporting citizen and subscribers to that geographic area.

Media attachments (photos, video, audio) are processed asynchronously. The upload endpoint accepts multipart/form-data, validates file types and sizes (max 50MB video, 10MB photo, 5MB audio), strips all EXIF metadata server-side (defense in depth — the client also strips, but the server never trusts client-side processing), transcodes video to H.264 720p for bandwidth efficiency, generates thumbnails, and stores originals in encrypted object storage (MinIO self-hosted or S3-compatible).

For Incognito Mode reports, the service receives requests through the Citizen Vault Relay (detailed in Section 8). The relay strips all transport-layer identifiers before the report reaches the Incident Service, meaning the service genuinely has no sender information to store.

### 5.2 Traffic Intelligence Service

This service aggregates, processes, and distributes real-time traffic data from four sources: crowdsourced citizen reports via Jukwa, Waze Connected Citizens Program data feed (free, bidirectional), Samsung ITS sensor data from the KSh 7.88 billion intelligent traffic system being installed across 25 Nairobi junctions, and historical pattern analysis from MongoDB time-series data.

The MQTT broker receives sensor telemetry on `jukwa/traffic/sensors/{junction_id}` topics at QoS 0 (at-most-once, acceptable for high-frequency sensor data where individual message loss is tolerable). The service subscribes to these topics, runs congestion scoring algorithms (weighted average speed, vehicle density, historical comparison), and publishes processed alerts on `jukwa/traffic/alerts/{corridor}` topics at QoS 1 (at-least-once, ensuring citizens receive every alert).

Corridor-level traffic scores (1–10 congestion index) update every 60 seconds during peak hours (6:00–9:00, 16:30–19:30) and every 5 minutes off-peak. Citizens subscribe to corridor topics matching their commute routes; the app manages subscriptions automatically based on location and saved routes. Push notifications fire only when a subscribed corridor's congestion index changes by 2+ points, preventing alert fatigue.

The service also generates predictive routing suggestions by analyzing historical congestion patterns. If Mombasa Road consistently gridlocks at 17:15 on Tuesdays, Jukwa proactively notifies subscribed users at 16:45 to consider the Southern Bypass or Expressway alternatives. This prediction engine runs as a batch job on MongoDB's aggregation pipeline, producing next-day predictions at midnight daily.

### 5.3 Civic Action Service

This service transforms Jukwa from a passive reporting tool into an active civic platform. It manages three functions: community crowdfunding for local solutions, participatory polling on neighborhood issues, and civic gamification.

The crowdfunding module integrates M-Pesa via Safaricom's Daraja 3.0 API (STK Push for collections, B2C for disbursements). When a report accumulates sufficient community support (measured by upvotes from geographically proximate users), the Civic Action Service can launch a community fund attached to the issue. A persistent pothole on Ngong Road with 200 citizen reports might generate a community fund for temporary repairs, with M-Pesa contributions and transparent expenditure tracking. The M-Changa model validates this approach — entirely M-Pesa-based community fundraising with 360-degree transaction visibility.

The polling module follows Seoul's mVoting architecture. Polls are geofenced to affected wards using PostGIS containment queries, ensuring only residents of Ward X vote on Ward X issues. Results are published transparently with response rates. Polls can be government-initiated (the Ward Administrator asks residents about speed bump placement) or citizen-initiated (a petition reaching a threshold triggers an official poll).

The gamification engine awards points for verified reports (50 points), confirmed resolutions (100 points), community fund contributions (25 points per KSh 100), and poll participation (10 points). Points accumulate into neighborhood leaderboards. Top-contributing wards receive public recognition and, where government partnerships exist, priority service delivery. Airtime rewards (disbursed via Africa's Talking) incentivize sustained engagement — research confirms gamified civic apps generate significantly higher participation than non-gamified versions.

### 5.4 Emergency Dispatch Service

The most time-sensitive service, designed for sub-10-second end-to-end processing from citizen SOS to agency notification. When the native app detects an emergency trigger (manual SOS button press, accelerometer impact exceeding 4G threshold, or voice-activated "Jukwa msaada" command), it immediately captures the device's GPS coordinates at maximum precision, begins audio/video recording, and sends a priority-flagged payload to the Emergency Dispatch Service via a dedicated API endpoint that bypasses standard rate limiting.

The service classifies the emergency (medical, security, fire, road accident) using the AI engine's analysis of any accompanying media and text, then simultaneously dispatches notifications to the nearest relevant responders: NARS ambulance dispatch (for medical), nearest police station OCS (for security), county fire services (for fire), and NTSA plus nearest traffic police unit (for road accidents). The notification includes GPS coordinates, a deep link to the live audio/video stream (if the citizen consented), and the citizen's anonymity preference (verified users share identity; anonymous users share only location).

A countdown timer starts at dispatch. If no agency acknowledges within 120 seconds, the service escalates to the County Commander's office and logs the non-response for accountability reporting. All emergency dispatch interactions are logged immutably for audit purposes.

### 5.5 AI Classification & Routing Engine

The intelligence layer that eliminates the need for citizens to navigate Kenya's institutional maze. The engine uses a multi-stage classification pipeline.

Stage one is category classification: the engine analyzes the report text, any attached media (using vision API for photo/video), and location context to assign one of approximately 40 incident categories spanning security (robbery, assault, suspicious activity, noise disturbance), traffic (accident, congestion, broken signal, illegal parking, matatu violation), infrastructure (pothole, burst pipe, electrical fault, garbage), and civic (land dispute, environmental, public health).

Stage two is severity scoring: each report receives a 1–5 severity score based on keywords, media analysis (fire detection, blood detection, crowd density), time of day, and historical patterns at that location (a report from a known hotspot scores higher).

Stage three is agency routing: a rule engine maps category + location to the responsible agency or agencies. A broken traffic light at Uhuru Highway/Kenyatta Avenue routes to both NTSA and the Samsung ITS Traffic Management Centre. A robbery in Eastleigh routes to the local OCS, the DCI Fichua system, and (if the area falls under Usalama Mitaani coverage) the Hatua App alert network. Multi-agency routing is common; the engine handles fan-out automatically.

The initial classification model is rule-based (fast, interpretable, no GPU required) with a supervised ML model trained on Ushahidi's Kenyan deployment data and Mulika Uhalifu's 176,000-report corpus as a validation layer. The ML model flags disagreements with the rule engine for human review, progressively improving rules over time.

---

## 6. Messaging & Real-Time Tier

### 6.1 MQTT Broker — Mosquitto (MVP) → EMQX (Scale)

MQTT is the backbone for real-time bidirectional communication. The protocol's 2-byte minimum header, persistent sessions (delivering queued messages when devices reconnect), and three QoS levels make it the optimal choice for Kenyan mobile networks where connections frequently drop and reconnect.

The MVP deployment uses Mosquitto on a single $20/month VPS, handling up to 50,000 concurrent connections — sufficient for a Nairobi-only beta. The topic hierarchy follows this structure:

```
jukwa/alerts/{county}/{ward}/{category}    — Citizen-facing alerts
jukwa/traffic/sensors/{junction_id}         — ITS sensor telemetry (inbound)
jukwa/traffic/alerts/{corridor}             — Processed traffic alerts
jukwa/incidents/{incident_id}/status        — Report status updates
jukwa/emergency/{county}/dispatch           — Emergency dispatch (restricted)
jukwa/civic/{ward}/polls                    — Civic participation events
```

Citizens subscribe to topics matching their location and interests. The Android app manages subscriptions automatically: when a user's location changes wards, the MQTT client unsubscribes from the old ward's topics and subscribes to the new ones. Topic-based filtering ensures users receive only geographically relevant alerts, preventing notification overload.

At scale (100,000+ concurrent connections), migration to EMQX provides MQTT-over-QUIC (0-RTT reconnection, critical for mobile), horizontal clustering, and webhook integration with the application services tier. The migration is transparent to clients — only the broker endpoint changes.

### 6.2 Firebase Cloud Messaging (Push Notifications)

FCM handles the "wake sleeping device" problem that MQTT cannot solve on modern Android. When the app is backgrounded or the device is in Doze mode, Android aggressively restricts background services. FCM's high-priority messages bypass these restrictions, waking the device to display an alert notification. Tapping the notification launches the app, which reconnects its MQTT session and syncs any queued messages.

FCM is used exclusively for notification delivery — the actual alert content is minimal (title, category, location summary, incident ID). Full details load from the MQTT session or REST API when the user opens the app. This keeps FCM payloads under 4KB (the limit for data messages) and avoids storing sensitive civic data in Google's infrastructure.

Topic-based FCM subscriptions mirror the MQTT topic hierarchy, allowing server-side targeting of push notifications to geographic segments without maintaining per-device subscription lists.

---

## 7. Data & Storage Tier

### 7.1 PostgreSQL 15 with PostGIS 3.3 (Primary Datastore)

PostgreSQL with PostGIS serves as the authoritative datastore for all structured data: incident reports, citizen pseudonyms, agency routing rules, civic action campaigns, gamification scores, and — critically — all geospatial queries. PostGIS was chosen over Firebase after validation research demonstrated Firebase's fundamental inability to perform polygon containment queries ("find all reports within Ward X boundaries"), combined spatial and attribute filtering, or density-based clustering for hotspot detection. PostGIS delivers all of these with millisecond performance on GiST-indexed spatial columns.

**Core Schema (Simplified)**

```sql
-- Enable spatial extensions
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Pseudonymous identity (Standard Mode)
CREATE TABLE citizens (
    citizen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_token_hash VARCHAR(64) UNIQUE NOT NULL,
    ward_id INTEGER REFERENCES wards(ward_id),
    anonymity_preference VARCHAR(20) DEFAULT 'STANDARD',
    gamification_points INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Incident reports with full spatial indexing
CREATE TABLE incidents (
    incident_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES citizens(citizen_id),  -- NULL for Incognito
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
CREATE INDEX idx_incidents_status ON incidents (status);
CREATE INDEX idx_incidents_ward ON incidents (ward_id, reported_at DESC);
CREATE INDEX idx_incidents_category ON incidents (incident_category, reported_at DESC);

-- Ward boundaries for geofenced queries and polls
CREATE TABLE wards (
    ward_id INTEGER PRIMARY KEY,
    ward_name VARCHAR(100) NOT NULL,
    county_name VARCHAR(50) NOT NULL,
    boundary GEOMETRY(MultiPolygon, 4326) NOT NULL
);

CREATE INDEX idx_wards_boundary ON wards USING GIST (boundary);

-- Aggregated civic insights (anonymized, shareable with GoK)
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

-- Contextual geofenced promotions (sustainability)
CREATE TABLE contextual_promotions (
    promo_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_name VARCHAR(100) NOT NULL,
    street_target VARCHAR(100),
    geofence GEOMETRY(Polygon, 4326) NOT NULL,
    display_content JSONB NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    impressions INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_promos_geofence ON contextual_promotions USING GIST (geofence);
```

The `civic_insights` table implements the Gemini contribution's aggregated data concept — stripped of all individual identifiers, these aggregate statistics become Jukwa's leverage for government engagement. A ward-level heatmap showing "247 potholes reported in Kilimani in February, average resolution time 45 days" is powerful civic data that is legally shareable under the DPA's research exemption (Section 51(3)) because no individual is identifiable.

The hosting recommendation is Supabase (managed PostgreSQL with PostGIS, built-in REST APIs via PostgREST, and real-time WebSocket subscriptions) deployed on a Kenyan data center or, for maximum control, self-hosted PostgreSQL on Safaricom Cloud or iXAfrica infrastructure.

### 7.2 MongoDB 7 (Telemetry Time-Series)

MongoDB handles the high-velocity, semi-structured data from IoT traffic sensors, GPS telemetry, and MQTT message logs. Its time-series collections (introduced in MongoDB 5.0) provide automatic bucketing, compression, and efficient range queries optimized for sensor data patterns.

The telemetry schema stores sensor readings with sub-second timestamps, vehicle counts, average speeds, and GeoJSON point locations. Queries like "average speed on Mombasa Road between 17:00 and 18:00 over the past 30 days" execute efficiently on time-series indexes without impacting the primary PostgreSQL database.

MongoDB Atlas (with a Singapore or Mumbai region, the nearest to East Africa) provides the managed hosting. Alternatively, self-hosted MongoDB on the same Kenyan infrastructure as PostgreSQL ensures data sovereignty.

### 7.3 Redis 7 (Cache, Sessions, Real-Time)

Redis serves three functions: session caching for API authentication tokens (TTL-based expiry, no persistent identity storage), rate limiting counters (sliding window per device token), and geofenced pub/sub for hyper-local alerts. Redis's native geospatial commands (`GEOADD`, `GEORADIUS`) enable fast proximity queries for "nearest police station" and "reports within 2km" without hitting PostgreSQL.

---

## 8. Privacy & Anonymity Tier — The Citizen Vault

### 8.1 Architecture

The Citizen Vault is the architectural element that transforms Jukwa from a government reporting tool into a genuinely independent civic infrastructure. Inspired by the Gemini contribution's relay concept but redesigned around GlobaLeaks' proven architecture (which powers PPLAAF, the platform behind the Luanda Leaks investigation), the Vault operates as an independent intermediary between citizens and state systems.

The Vault runs on infrastructure hosted by a consortium of trusted civil society organizations (Kenya Human Rights Commission, Article 19, Transparency International Kenya, or similar). It is not owned, operated, or accessible by the Jukwa development team or any government entity. Its sole function is to receive Incognito Mode submissions, create an encrypted evidence backup, strip all transport-layer identifiers, and forward the sanitized report to the main Jukwa API.

```
Citizen Device                    Citizen Vault                  Jukwa Main API
     │                                 │                              │
     │  1. EXIF stripped locally       │                              │
     │  2. Report encrypted (NaCl)     │                              │
     │  3. Transmitted via TLS 1.3     │                              │
     │────────────────────────────────►│                              │
     │  (Optional: Tor bridges)        │                              │
     │                                 │  4. Decrypt report           │
     │                                 │  5. Backup to encrypted FS   │
     │                                 │  6. Strip IP, headers        │
     │                                 │  7. Re-encrypt for Jukwa     │
     │                                 │  8. Forward via server-to-   │
     │                                 │     server TLS               │
     │                                 │────────────────────────────►│
     │                                 │                              │
     │                                 │  9. Jukwa receives report   │
     │                                 │     with NULL reporter_id    │
     │                                 │     and Vault's IP (not      │
     │                                 │     citizen's)               │
     │                                 │                              │
     │  10. Case reference returned    │                              │
     │◄────────────────────────────────│◄─────────────────────────────│
```

The Vault ensures two critical guarantees. First, even if the Jukwa main servers are compromised or compelled by a court order, Incognito reports cannot be traced to individual citizens because the main API genuinely never receives identifying information. Second, even if the government ignores or suppresses a report, the encrypted backup held by civil society organizations preserves the evidence independently — a check against institutional failure.

### 8.2 On-Device Privacy Pipeline

For all anonymity modes, the following processing occurs on the citizen's device before any network transmission:

The EXIF scrubber removes all metadata from photos and videos using Android's ExifInterface (for JPEG) and a custom byte-level scrubber for MP4 containers (removing `moov/udta` atoms containing GPS, device model, and creation time). The scrubbing operates in-memory using streaming I/O to avoid writing intermediate files to disk storage where they could be forensically recovered.

In Incognito Mode, additional protections activate. GPS coordinates are rounded to four decimal places (approximately 11-meter precision) then further fuzzed by adding random noise within a 500-meter radius, ensuring location is useful for routing to the correct ward but insufficient for identifying a specific building or household. The device token is replaced with a single-use ephemeral token generated per-submission. No persistent identifier links two Incognito submissions from the same device.

### 8.3 Legal Basis

The Kenya Data Protection Act 2019 defines personal data as information relating to an "identified or identifiable natural person." If Jukwa's Incognito Mode genuinely processes zero personal data — no IP logging, no persistent identifiers, metadata-stripped media, ward-level-only geolocation — the DPA's substantive obligations (consent, purpose limitation, data subject rights) arguably do not apply because there is no "data subject" to protect. This position is consistent with GDPR jurisprudence (from which Kenya's DPA draws heavily) but has not been tested before the ODPC.

Jukwa should voluntarily register with the ODPC, conduct a Data Protection Impact Assessment, and obtain a legal opinion documenting the anonymization methodology. Voluntary compliance demonstrates good faith and pre-empts regulatory challenge. The aggregate civic insights shared with government fall under Section 51(3)'s research exemption, provided data is published in non-identifiable form.

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

### 10.2 Hosting Strategy

Primary hosting resides on Kenyan infrastructure for DPA compliance. The recommended provider is iXAfrica/Digital Realty (Tier III, 22.5 MW capacity) or Safaricom Cloud, with Cloudflare CDN for edge delivery. When AWS launches its Nairobi region (expected late 2026, $2.5 billion investment, 3 Availability Zones), migration to AWS with local data residency becomes the growth path. Google Cloud's interconnect sites in Nairobi and Mombasa provide an alternative or multi-cloud option.

The Citizen Vault runs on separate infrastructure — ideally hosted by the civil society consortium on a physically distinct server in a different data center, ensuring no single point of seizure or compromise can access both the main platform data and the whistleblower evidence backups.

### 10.3 Cost Estimation (MVP — 12 Months)

The MVP targets Nairobi with expansion to Mombasa and Kisumu in Year 2. Estimated infrastructure costs for the first year:

Compute (2× 4-vCPU, 16GB VPS instances on Safaricom Cloud or iXAfrica): approximately KSh 360,000/year. PostgreSQL managed instance (or self-hosted on compute): included. MongoDB Atlas M10 (shared tier): approximately KSh 180,000/year. Cloudflare Pro: KSh 31,200/year. Africa's Talking (USSD sessions + SMS at estimated 50,000 interactions/month): approximately KSh 600,000/year. FCM: free. MQTT (Mosquitto self-hosted): free (included in compute). Mapbox/MapLibre tiles: approximately KSh 120,000/year. Domain, SSL, monitoring: approximately KSh 60,000/year.

Total estimated infrastructure: approximately KSh 1.35 million/year (roughly $10,400 USD). This excludes development labor, legal compliance costs, and civil society partnership costs for the Citizen Vault.

---

## 11. Build Roadmap

### Phase 1: Foundation (Months 1–3)

Set up the container infrastructure, deploy PostgreSQL/PostGIS with ward boundaries for all 1,450 wards, implement the Incident Service with basic text reporting, build the native Android app with offline-first reporting and a static map, integrate FCM for push notifications, deploy Mosquitto and establish the topic hierarchy, and implement Standard Mode identity (pseudonymous device tokens). Deliverable: internal alpha with text-based incident reporting and push alerts for a single Nairobi ward.

### Phase 2: Intelligence (Months 4–6)

Deploy the AI Classification Engine (rule-based with ML validation), implement the Traffic Intelligence Service with Waze CCP integration, build the USSD/SMS access tier via Africa's Talking, launch the PWA, implement Incognito Mode with client-side EXIF stripping, and begin the Citizen Vault partnership with civil society organizations. Deliverable: closed beta across 10 Nairobi wards with all three access tiers operational.

### Phase 3: Engagement (Months 7–9)

Launch M-Pesa integration for community crowdfunding, implement gamification engine with airtime rewards, deploy geofenced polling following Seoul mVoting patterns, build the WhatsApp chatbot channel, integrate Samsung ITS sensor data (as junctions come online), and launch the contextual promotions module for sustainability revenue. Deliverable: public beta across Nairobi County.

### Phase 4: Scale (Months 10–12)

Migrate Mosquitto to EMQX for horizontal scaling, implement the Emergency Dispatch Service with agency notification integrations, deploy the civic insights dashboard for government engagement, optimize for low-end devices (Android Go testing, 2G fallback), conduct ODPC voluntary registration and DPIA, and expand to Mombasa and Kisumu. Deliverable: public launch across three counties.

---

## 12. Governance & Sustainability

Jukwa's long-term independence depends on not being wholly dependent on government goodwill or donor funding. Three revenue streams ensure sustainability.

Contextual geofenced promotions generate revenue by allowing local businesses to sponsor map pins visible to users viewing their street or corridor — crucially, these ads target streets rather than users, meaning zero personal data is required for ad targeting. A restaurant on Moi Avenue sponsors a pin visible to anyone viewing Moi Avenue traffic, regardless of who they are. This model aligns perfectly with the zero-PII architecture.

Civic insights licensing provides aggregated, anonymized data products to urban planners, researchers, and international development organizations. Ward-level safety scores, infrastructure decay heatmaps, and traffic pattern analyses derived from millions of citizen reports represent valuable planning data that is fully anonymous and legally shareable.

Government partnership fees fund Jukwa's integration with county and national government systems. Counties that want direct API access to route reports into their service delivery workflows pay an annual platform fee covering integration development, SLA monitoring, and support.

The governance structure should be a trust or non-profit social enterprise with an independent board including civil society representatives, ensuring the platform cannot be captured by any single political or commercial interest. The Citizen Vault consortium holds veto power over any architectural changes that would weaken anonymity protections.

---

*This document is a living specification. Architecture decisions recorded here are binding unless superseded by a subsequent version approved by the technical steering committee.*
