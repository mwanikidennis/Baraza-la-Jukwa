## Summary

<!-- 1-3 sentences: what changes and why -->

## Checklist (per `.cursorrules`)

- [ ] No placeholders or unfinished TODOs
- [ ] Strict types (no `any` without justification)
- [ ] Anonymity modes respected (no PII written in Standard/Incognito paths)
- [ ] If a new service: Dockerfile + healthcheck + `docker-compose.yml` entry included
- [ ] If touching DB: migration / `init.sql` updated
- [ ] Docs (`README.md`, `docs/SETUP.md`, `docs/API.md`, `docs/ARCHITECTURE.md`, `docs/BARAZA.md`) updated where relevant
- [ ] CI green (`lint-test-backend`, `compose-smoke`, `build-images`)

## Test plan

<!-- How a reviewer can verify this locally / in CI -->
