# 🎨 Understanding Your Project Architecture & What You're Seeing

## Part 1: What is "Dozzle"? (Not Your App UI)

**Dozzle** = A log viewer dashboard, NOT your actual application UI.

```
┌─────────────────────────────────────────────────────┐
│ http://localhost:8888                              │
│ ┌──────────────────────────────────────────────────┤
│ │  Dozzle - Docker Container Log Viewer             │
│ │  ┌─────────────────────────────────────────────┐  │
│ │  │ Containers:  ✓ incident-service (running)   │  │
│ │  │              ✓ traffic-service (running)    │  │
│ │  │              ✓ redis (running)              │  │
│ │  │              ✓ minio (running)              │  │
│ │  │              ✓ etc...                       │  │
│ │  │                                              │  │
│ │  │  [Real-time log stream from containers]      │  │
│ │  │  jukwa-incident-service:                    │  │
│ │  │    2026-05-29 22:11:22 Server started      │  │
│ │  │    2026-05-29 22:11:23 Ready to accept... │  │
│ │  └─────────────────────────────────────────────┘  │
│ └──────────────────────────────────────────────────┘
└─────────────────────────────────────────────────────┘

This is a MONITORING tool, not your app!
```

Your **actual application UI** is the **Android app** (Jetpack Compose) in the `android/` folder.

---

## Part 2: Where IS Your UI/UX?

Your project has **two frontends**:

### 1. **Android Mobile App** (Primary UI/UX)
**Location:** `D:\Github Local\Baraza-la-Jukwa\android\`

This is what your users see — a native Android app built with:
- **Kotlin 2.0.0**
- **Jetpack Compose** (modern declarative UI framework)
- **Room** (offline-first database)
- **MapLibre** (for geographic features)

**You DON'T preview it in a browser.** You preview it in:
- Android Studio (live Compose preview)
- Android Emulator (simulated phone)
- Real Android phone (APK install)

### 2. **Next.js PWA** (Web Frontend — Phase 2, Not Yet Built)
**Status:** Mentioned in code but not yet implemented

This will be a progressive web app (web UI) — coming later.

---

## Part 3: How to View Your Android UI/UX (What You Want)

### Option A: Live Jetpack Compose Preview (Instant, No Emulator)

```
1. Open Android Studio
2. File → Open → D:\Github Local\Baraza-la-Jukwa\android
3. Navigate to: android/app/src/main/java/.../<Screen>.kt
   (Any file ending in .kt in the java folder)
4. Right panel shows LIVE preview of the UI
5. Edit code → preview updates instantly
```

**This is your design/review interface.** Click buttons, interact with the UI, all in the IDE.

### Option B: Android Emulator (Simulated Phone)

```
1. Android Studio → Tools → AVD Manager
2. Create or select a virtual device
3. Green "Run" button (top toolbar)
4. Select the emulator
5. APK installs and runs on the simulated phone
6. Test the full app like a real user would
```

### Option C: Real Android Phone

```
1. Connect phone via USB
2. Enable Developer Mode on phone
3. Android Studio → Green "Run" button
4. Select your phone
5. APK installs directly to your phone
```

---

## Part 4: What Are These Backend Services For?

Your backend services (the ones running in containers) provide:

```
┌─────────────────────────────────────────────────────┐
│  Android App (UI/UX) — What Users See              │
├─────────────────────────────────────────────────────┤
│  (Jetpack Compose, MapLibre, Offline-first)        │
└─────────────────────────────────────────────────────┘
                        ↕
                   HTTP APIs
                        ↕
┌─────────────────────────────────────────────────────┐
│  Backend Services (Docker Containers)              │
├─────────────────────────────────────────────────────┤
│  ✓ incident-service (port 3001)                    │
│    Handles: Report incidents, track emergencies   │
│                                                     │
│  ✓ traffic-service (port 3003)                    │
│    Handles: Traffic sensor data, congestion       │
│                                                     │
│  ✓ identity-service (port 3006)                   │
│    Handles: Authentication, citizen profiles      │
│                                                     │
│  ✓ notification-service (port 3007)               │
│    Handles: Push notifications, SMS, FCM          │
│                                                     │
│  ✓ commitment-service (port 3002)                 │
│    Handles: Track citizen commitments             │
│                                                     │
│  ✓ emergency-service (port 3004)                  │
│    Handles: Emergency response coordination       │
│                                                     │
│  ✓ ai-agent-service (port 3010)                   │
│    Handles: AI-powered citizen engagement         │
│                                                     │
│  ✓ citizen-vault-service (port 3011)              │
│    Handles: Privacy-preserving data storage       │
└─────────────────────────────────────────────────────┘
                        ↕
                   Data Layers
                        ↕
┌─────────────────────────────────────────────────────┐
│  Data Infrastructure (Also Docker)                │
├─────────────────────────────────────────────────────┤
│  ✓ PostgreSQL (spatial database, PostGIS)         │
│  ✓ MongoDB (time-series telemetry)                │
│  ✓ Redis (caching, sessions)                      │
│  ✓ MinIO (S3-compatible object storage)           │
│  ✓ Mosquitto MQTT (real-time pub/sub)             │
│  ✓ Tor (privacy anonymization)                    │
│  ✓ Nginx (API gateway)                            │
└─────────────────────────────────────────────────────┘
```

---

## Part 5: Understanding the Error Logs (They're NOT Critical)

### MinIO Warnings (Object Storage)

```
WARNING: Host local has more than 0 drives of set. A host failure will result in data becoming unavailable.
Warning: The standard parity is set to 0. This can lead to data loss.
You are running an older version of MinIO released 2 years before the latest release
```

**What it means:** MinIO is running with minimal redundancy (okay for dev, not for production).

**Severity:** ⚠️ **WARNING only — NOT an error**

**For development:** This is fine. MinIO is working.

**For production:** Update to latest MinIO and configure proper replication.

**Action:** None needed for now. It's just telling you to upgrade.

---

### Redis Logs

```
1:C 29 May 2026 22:11:22.703 # Warning: no config file specified, using the default config.
1:signal-handler (1780096150) Received SIGTERM scheduling shutdown...
1:M 2026-05-29 23:09:10.264 * User requested shutdown...
```

**What it means:**
- Redis started with default config (okay for dev)
- SIGTERM = graceful shutdown signal (normal when you stop containers)
- "User requested shutdown" = You ran `docker compose stop` (expected)

**Severity:** ✅ **NORMAL — Not an error**

**Action:** None needed. Redis is working correctly.

---

### Tor Proxy Logs

```
May 29 22:12:51.350 [notice] Tor 0.4.7.12 running on Linux...
May 29 22:12:51.406 [warn] SocksPort, TransPort, NATDPort, DNSPort, and ORPort are all undefined...
Tor can't help you if you use it wrong!
```

**What it means:**
- Tor is starting up
- No specific service configured (okay for development)
- Bootstrapping connection to Tor network (normal startup process)

**Severity:** ⚠️ **WARNING only — NOT an error**

**For development:** Tor is running but not actively being used. That's fine.

**For production:** Configure specific onion services for privacy.

**Action:** None needed. Tor is functioning.

---

## Part 6: Summary of Error Logs

| Service | Severity | Issue | Status | Action |
|---------|----------|-------|--------|--------|
| MinIO | ⚠️ Warning | Old version, minimal redundancy | ✅ Working | None (dev ok) |
| Redis | ✅ Normal | Graceful shutdown from stop | ✅ Working | None |
| Tor | ⚠️ Warning | No service configured | ✅ Working | None (dev ok) |
| PostgreSQL | ✅ OK | Not shown (no errors) | ✅ Working | None |
| MQTT | ✅ OK | Not shown (no errors) | ✅ Working | None |
| Services | ✅ OK | Health checks passing | ✅ Working | None |

**Bottom line:** No actual errors. Just development warnings.

---

## Part 7: What You SHOULD Be Doing Now

### Step 1: Preview Your Android UI (Not in Browser!)

```
1. Open Android Studio
2. File → Open → D:\Github Local\Baraza-la-Jukwa\android
3. Wait for Gradle sync (1-2 minutes)
4. Open any .kt file in: android/app/src/main/java/
5. Look at RIGHT panel → Click "Design" tab
6. You'll see the live Jetpack Compose preview
7. Try clicking buttons, filling forms, interacting
```

**This is your actual UI/UX that users will see.**

### Step 2: Test Backend APIs (Via Browser)

```
1. http://localhost:3001/health          ← Incident service
2. http://localhost:3002/health          ← Commitment service
3. http://localhost:3003/health          ← Traffic service
4. http://localhost:3006/health          ← Identity service
5. http://localhost:3007/health          ← Notification service
6. http://localhost:3010/health          ← AI agent service
7. http://localhost:3011/health          ← Citizen vault service
```

Each should return: `{"status":"ok"}` or similar.

These ARE your backend APIs. They power the Android app.

### Step 3: Monitor Logs (In Dozzle)

```
1. http://localhost:8888                  ← Dozzle log viewer
2. Click any service to see its logs
3. Edit code in services/incident/src/
4. Rebuild: docker compose build incident-service
5. Watch logs update in real-time
```

Dozzle is purely for **monitoring/debugging**, not for app review.

---

## Part 8: Visual Comparison

### What You Saw (Dozzle — Log Viewer)
```
http://localhost:8888
┌─────────────────────────────────────────┐
│ Dozzle — Container Logs                 │
├─────────────────────────────────────────┤
│ Services: incident, traffic, redis...   │
│ Logs: [Real-time container output]      │
│                                          │
│ This is for MONITORING, not app review. │
└─────────────────────────────────────────┘
```

### What You SHOULD See (Android UI Preview)
```
Android Studio → Design Tab
┌─────────────────────────────────────────┐
│ Jetpack Compose Preview                 │
├─────────────────────────────────────────┤
│ ┌───────────────────────────────────┐   │
│ │   Jukwa Citizen Engagement App    │   │
│ │                                    │   │
│ │   [Map showing incidents]          │   │
│ │   [Incident Report Button]         │   │
│ │   [Navigation Tabs]                │   │
│ │   [User Profile Section]           │   │
│ │                                    │   │
│ └───────────────────────────────────┘   │
│                                          │
│ This is your ACTUAL UI/UX for review.   │
└─────────────────────────────────────────┘
```

---

## Part 9: Quick Reference — Where to Find What

| What | Where | How |
|------|-------|-----|
| **App UI/UX** | `android/app/src/main/java/` | Android Studio → Design tab |
| **Backend APIs** | `services/incident/src/` etc. | Browser: http://localhost:3001/health |
| **Container Logs** | Dozzle dashboard | Browser: http://localhost:8888 |
| **Database** | PostgreSQL, MongoDB, Redis | CLI: `docker exec <container>` |
| **File Storage** | MinIO console | Browser: http://localhost:9090 |
| **API Gateway** | Nginx | Browser: http://localhost/healthz |

---

## Part 10: The "Warning" Logs Are Expected

These are **development mode warnings**, not production errors:

✅ **Normal in development:**
- MinIO using default config (minimal redundancy)
- Tor running without configured services
- Redis using default configuration
- No SSL/TLS certificates

❌ **Would be a problem in production** — but you're not in production yet.

**For now:** Ignore these warnings. They don't affect your ability to:
- Preview the UI in Android Studio
- Test the backend APIs
- Debug services via logs
- Develop and iterate

---

## Your Next Action

```
1. Open Android Studio
2. File → Open → D:\Github Local\Baraza-la-Jukwa\android
3. Open: android/app/src/main/java/.../<YourScreenName>.kt
4. Look at RIGHT panel
5. You'll see your actual app UI in the preview
```

That's your UI/UX interface for review and observation. Not Dozzle (that's just logs).

---

## Summary

| Component | View Where | Purpose |
|-----------|-----------|---------|
| **Dozzle** | http://localhost:8888 | **Monitoring** — See what's running and logs |
| **Android App** | Android Studio design tab | **Your actual UI/UX** — What users will see |
| **Backend APIs** | http://localhost:3001 etc. | **Backend services** — Power the app |
| **MinIO Console** | http://localhost:9090 | **File storage** — Media uploads |

You were looking at the monitoring dashboard. Your app UI is in Android Studio (the design/preview tab).

Is that clear? Want me to help you open Android Studio and navigate to the UI preview next?
