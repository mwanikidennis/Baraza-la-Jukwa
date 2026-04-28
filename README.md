# JUKWA

[![CI](https://github.com/kenyawebs/Baraza-la-Jukwa/actions/workflows/ci.yml/badge.svg)](https://github.com/kenyawebs/Baraza-la-Jukwa/actions/workflows/ci.yml)
[![CodeQL](https://github.com/kenyawebs/Baraza-la-Jukwa/actions/workflows/codeql.yml/badge.svg)](https://github.com/kenyawebs/Baraza-la-Jukwa/actions/workflows/codeql.yml)

Kenya's unified citizen engagement platform.

## Architecture

- **Android App**: Kotlin, Jetpack Compose, Room (offline-first)
- **Backend Services**: Node.js, Fastify, TypeScript
- **Database**: PostgreSQL with PostGIS
- **Real-Time**: MQTT via Mosquitto, Firebase Cloud Messaging
- **Object Storage**: MinIO (S3-compatible)
- **Edge**: OpenResty/NGINX

Reference docs: [`Docs/Start Build/`](Docs/Start%20Build/) — Master Prompt, Architecture Framework, Technical Build Bible, BARAZA spec.

## Monorepo layout

```text
android/         Kotlin / Jetpack Compose app (Phase 2)
services/        Independent backend microservices, one folder each
  incident/      Fastify — incident management (port 3001)
pwa/             Next.js progressive web app (Phase 2)
ussd/            USSD / SMS handler (Phase 2)
whatsapp/        WhatsApp bot (Phase 2)
shared/          Cross-service TypeScript types & contracts (Phase 2)
infra/           docker-compose, nginx, mosquitto, postgres init
data/            Static seed data (wards.geojson, agencies.json, …)
docs/            Setup, API, architecture
.github/         CI workflows, CODEOWNERS, PR template, Dependabot
```

Each `services/<name>/` is a self-contained Node project with its own `package.json`, `Dockerfile`, and `HEALTHCHECK`. Adding a service means: new folder under `services/`, new entry in `infra/docker-compose.yml`, new row in the CI `build-images` matrix, new Dependabot block.

## Quick start

```bash
cp .env.example .env
cd infra && docker compose up -d --build
curl http://localhost:3001/health
```

Full instructions: [docs/SETUP.md](docs/SETUP.md).
