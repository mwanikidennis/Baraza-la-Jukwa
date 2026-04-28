# JUKWA Architecture Overview
This document describes the high-level architecture of the Jukwa platform.

## System Layers
1. **Gateway Layer**: Nginx (OpenResty) providing reverse proxy and TLS termination.
2. **Application Tier**: Microservice cluster (Fastify/Node.js and Python).
3. **Data Tier**: 
   - PostgreSQL (Relational/Spatial)
   - MongoDB (Time-series Telemetry)
   - Redis (Caching/Sessions)
4. **Messaging Tier**: Mosquitto MQTT for real-time pub/sub.

## Service Map
Refer to `shared/constants/service-ports.ts` for the official port mapping.
- **Incident Service (3001)**: Core spatial reporting.
- **Commitment Service (3002)**: BARAZA accountability.
- **Traffic Service (3003)**: IoT data ingestion.
- **Emergency Service (3004)**: High-priority dispatch.
- **Identity Service (3006)**: Pseudonym management.
- **Notification Service (3007)**: FCM bridge.
- **AI Agent (3010)**: Gemini orchestrator.
- **Citizen Vault (3011)**: Privacy relay.

---
*For implementation details, see [SETUP.md](SETUP.md).*
