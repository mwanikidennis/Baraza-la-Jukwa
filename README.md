# JUKWA
Kenya's unified citizen engagement platform.

## Architecture
- **Android App**: Kotlin, Jetpack Compose, Room (Offline-First)
- **Backend Services**: Node.js, Fastify, TypeScript
- **Database**: PostgreSQL with PostGIS
- **Real-Time**: MQTT via Mosquitto, Firebase Cloud Messaging

## Setup Instructions
Please see [docs/Start Build/SETUP.md](docs/Start%20Build/SETUP.md) for full instructions.

1. Copy `.env.example` to `.env`
2. Run `docker-compose up -d` in the `infra` directory
3. Verify the services are running.
