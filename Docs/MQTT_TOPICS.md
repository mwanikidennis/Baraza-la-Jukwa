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
