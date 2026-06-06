# 🏛️ Senior Architecture Overview: The Jukwa Ecosystem

This document provides a definitive, high-level architectural view of the Jukwa platform, specifically detailing how your mobile front-end interacts with your containerized backend infrastructure.

## 1. The Core Separation of Concerns

Jukwa is built on a modern **Client-Server Architecture**. It is crucial to understand the absolute separation between these two layers:

### The Client (Frontend) -> Android Native App
- **Location:** `D:\Github Local\Baraza-la-Jukwa\android\`
- **Stack:** Kotlin, Jetpack Compose, Room (Offline Cache), Retrofit (Networking).
- **Role:** This is the *only* thing your end-users will ever see or interact with. It provides the UI/UX. It handles offline caching, geographic mapping, and user input.
- **Execution:** Runs on an Android device (or an Emulator). It does **not** run in Docker.

### The Server (Backend) -> Docker Microservices
- **Location:** `D:\Github Local\Baraza-la-Jukwa\infra\` and `services\`
- **Stack:** Node.js, Python, PostgreSQL, Redis, MongoDB, Nginx.
- **Role:** The invisible engine. It processes data, authenticates users, stores records securely, and coordinates emergencies.
- **Execution:** Runs entirely inside Docker Desktop (the 7 processes you see in your PowerShell terminal).

---

## 2. The Networking Bridge: How Android Talks to Docker

A common point of confusion is how the Android app (running in an emulator) talks to the backend (running in Docker).

**The Magic IP Address: `10.0.2.2`**
If you look inside `android/app/src/main/kotlin/ke/jukwa/di/NetworkModule.kt`, you will see code like this:
```kotlin
private const val BASE_URL = "http://10.0.2.2:3001"
```

**Why `10.0.2.2` and not `localhost`?**
- The Android Emulator runs as its own isolated Virtual Machine.
- If the Android code called `localhost:3001`, it would be talking to *itself* (the virtual phone), where nothing is listening!
- Android sets aside the special IP `10.0.2.2` as a bridge. When the app calls `10.0.2.2`, it routes the request OUT of the emulator and hits your Windows Host machine's `localhost`.
- Your Windows Host then routes that request directly into your Docker containers.

### Data Flow Example (Reporting an Incident):
1. **User (Android App):** Taps "Submit Incident" on the Compose UI.
2. **Retrofit (Android Network Library):** Sends a POST request to `http://10.0.2.2:3001/incidents`.
3. **Android Emulator:** Routes `10.0.2.2` out to Windows `localhost`.
4. **Docker Desktop:** Catches `localhost:3001` and forwards it to the `incident-service` container.
5. **Node.js (incident-service):** Processes the request and saves it to the PostgreSQL volume.
6. **Response:** Travels back up the chain to update the Android UI.

---

## 3. The 7 Backend Pillars (Microservices)

Your Docker environment is running a distributed microservice architecture. Each service has a single, focused responsibility:

| Service | Port | Primary Responsibility | Data Store |
|---------|------|------------------------|------------|
| **Incident Service** | 3001 | Processes and routes citizen reports | PostgreSQL |
| **Commitment Service**| 3002 | Tracks government & citizen pledges | PostgreSQL |
| **Traffic Service** | 3003 | Aggregates high-frequency telemetry | MongoDB |
| **Emergency Service** | 3004 | Coordinates rapid response actions | Mosquitto (MQTT) |
| **Identity Service** | 3006 | Manages auth, JWTs, and profiles | Redis / PostgreSQL|
| **Notification Service**| 3007 | Dispatches push/SMS alerts via FCM | Mosquitto (MQTT) |
| **Citizen Vault** | 3011 | Encrypts highly sensitive user data | Local Encrypted Vol|
| **AI Agent Service** | 3010 | Processes natural language queries | None (Stateless)|

### Infrastructure Support Services
- **Nginx (Ports 80/443):** Acts as your API Gateway in production.
- **Dozzle (Port 8888):** Your visual log monitoring dashboard. **This is not your app UI.** It is purely for you, the developer, to watch the services communicate in real-time.
- **Tor:** Ensures the Citizen Vault can transmit highly sensitive whistleblower data completely anonymously.

---

## 4. Summary & Best Practices

1. **Never look for the UI in a web browser.** Your UI is the Jetpack Compose code in Android Studio.
2. **If the Android app fails to fetch data, check Dozzle (`http://localhost:8888`).** The logs will tell you if `incident-service` or `identity-service` crashed or received a bad request.
3. **When developing locally, ensure Docker is running FIRST.** Run `.\docker-setup.bat start` before pressing "Play" in Android Studio. If the backend is down, the Android app will show network errors.
