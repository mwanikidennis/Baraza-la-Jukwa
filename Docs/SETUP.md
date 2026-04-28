# JUKWA — Local & CI Setup

This document covers the post-scaffold bring-up. Architecture and module specs live in [`Docs/Start Build/`](../Docs/Start%20Build/).

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
