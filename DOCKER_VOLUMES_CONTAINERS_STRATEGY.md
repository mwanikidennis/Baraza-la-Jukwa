# Docker Volumes & Containers Strategy for Baraza-la-Jukwa & Multi-Project Setup

## Part 1: Conceptual Understanding

### What is a Volume Container?

A **volume container** is an older pattern (pre-Docker 1.9) that used a container just to hold mounted volumes for other containers. Modern Docker prefers **named volumes** instead.

**Old approach (Don't use):**
```bash
docker create --name dbdata -v /data busybox
docker run --volumes-from dbdata postgres:15
```

**Modern approach (Use this):**
```bash
docker volume create jukwa_postgres_data
docker run -v jukwa_postgres_data:/var/lib/postgresql/data postgres:15
```

**Your project is already modern** — `infra/docker-compose.yml` uses named volumes:
```yaml
volumes:
  postgres_data:        # ← Named volume (persists data)
  redis_data:
  mongodb_data:
  minio_data:
```

Docker stores these in **WSL2 by default** (`\\wsl.localhost\docker-desktop-data\`). This is optimal for Windows performance.

---

### What Are Containers?

**Containers** are running instances of Docker images. When you run `docker compose up`, containers are created and run. They are NOT stored on your `D:\` drive — they exist in Docker's virtual machine.

```
Image (disk blueprint)  → Container (running process) → Data persists in Volumes
    └─ immutable           └─ processes, logs            └─ on host filesystem
       blueprint              temporary                      or Docker VM
```

**Data locality:**
- **Image**: Built once, referenced by all containers
- **Container**: Ephemeral (deleted when stopped with `docker compose down`)
- **Volume**: Persistent (survives container deletion)

---

### Why Use Volumes in This Project?

Your `docker-compose.yml` has:
```yaml
postgres_data:/var/lib/postgresql/data    # DB persists across restarts
redis_data:/data                           # Cache/session data survives
mongodb_data:/data/db                      # Telemetry data persists
minio_data:/data                           # Uploaded files persist
```

**Without volumes:** Every `docker compose down` would delete your database and uploaded files.
**With volumes:** `docker compose down && docker compose up -d` restores everything instantly.

---

## Part 2: Multi-Project Management Strategy

Since you're containerizing multiple projects in `D:\Github Local\`:

### A. Project-Level Isolation (RECOMMENDED)

Each project has its own set of volumes with a **prefix** to avoid collisions:

```
docker volume ls | grep jukwa_
  jukwa_postgres_data
  jukwa_redis_data
  jukwa_mongodb_data
  jukwa_minio_data

docker volume ls | grep other_project_
  other_project_postgres_data
  ...
```

**How to implement:** Update `infra/docker-compose.yml`:
```yaml
volumes:
  postgres_data:     # ← Docker auto-prefixes as "infra_postgres_data"
  redis_data:
  mongodb_data:
  minio_data:
```

To use explicit prefixes:
```yaml
volumes:
  jukwa_postgres_data:
  jukwa_redis_data:
  jukwa_mongodb_data:
  jukwa_minio_data:

services:
  postgres:
    volumes:
      - jukwa_postgres_data:/var/lib/postgresql/data
```

### B. Project Directory Structure

```
D:\Github Local\
├── Baraza-la-Jukwa\
│   ├── infra\
│   │   └── docker-compose.yml      # Defines jukwa_* volumes
│   ├── services\
│   │   ├── incident\Dockerfile
│   │   └── ...
│   ├── .dockerignore
│   ├── .env.example
│   ├── .env.production
│   └── CONTAINERIZATION_GUIDE.md
│
├── other_project\
│   ├── docker-compose.yml          # Defines other_project_* volumes
│   └── ...
│
└── .docker-workspace\              # Optional: local logs/backups (not used by Docker)
    ├── jukwa-backups\
    └── logs\
```

---

## Part 3: Workflow & Checklist

### ✅ Phase 1: Initial Setup (One-time)

**Objective:** Verify Docker Desktop is configured correctly.

```bash
# 1. Confirm Docker Desktop is running
docker --version
docker ps  # Should return: "CONTAINER ID  IMAGE  COMMAND  ..."

# 2. Verify WSL2 backend is active
docker info | findstr -i "os.*Linux"  # Should show "Linux"

# 3. Verify volume storage location (WSL2)
docker volume inspect jukwa_postgres_data | findstr Mountpoint
# Should show: "Mountpoint": "\\wsl.localhost\docker-desktop-data\..."
```

---

### ✅ Phase 2: Project-Specific Setup (Per Project)

**For Baraza-la-Jukwa:**

#### Step A: Update docker-compose.yml (OPTIONAL — already works as-is)

Current (`infra/docker-compose.yml`):
```yaml
volumes:
  postgres_data:
  redis_data:
  mongodb_data:
  minio_data:
```

This automatically becomes `infra_postgres_data` etc. in your Docker volume store.

**To use explicit prefixes** (more control, easier debugging):
```yaml
volumes:
  jukwa_postgres_data:
  jukwa_redis_data:
  jukwa_mongodb_data:
  jukwa_minio_data:

services:
  postgres:
    volumes:
      - jukwa_postgres_data:/var/lib/postgresql/data
  redis:
    volumes:
      - jukwa_redis_data:/data
  # ... (update all services)
```

#### Step B: Create Named Volumes Explicitly (Optional but Recommended)

```bash
cd infra

# Create volumes upfront (explicit, better for production)
docker volume create jukwa_postgres_data
docker volume create jukwa_redis_data
docker volume create jukwa_mongodb_data
docker volume create jukwa_minio_data

# Verify creation
docker volume ls | findstr jukwa_
```

#### Step C: Start the Stack

**Development:**
```bash
cd D:\Github Local\Baraza-la-Jukwa\infra
docker compose up -d --build
```

**Production (with resource limits):**
```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

#### Step D: Verify Volumes Persisted

```bash
# Check all volumes
docker volume ls

# Inspect specific volume
docker volume inspect jukwa_postgres_data

# Check volume usage
docker system df
```

---

### ✅ Phase 3: Data Persistence Testing

**Test:** Restart containers without losing data.

```bash
# 1. Start stack
cd infra
docker compose up -d

# 2. Wait 5 seconds, verify services healthy
docker compose ps

# 3. Create test data (e.g., insert a row in PostgreSQL via incident-service API)
curl http://localhost:3001/health  # Should return 200

# 4. Gracefully stop
docker compose down  # ← Containers deleted, volumes PERSISTED

# 5. Verify volumes still exist
docker volume ls | findstr jukwa_

# 6. Restart
docker compose up -d

# 7. Data is restored automatically
curl http://localhost:3001/health  # Same data intact
```

---

### ✅ Phase 4: Multi-Project Isolation (For Other Projects)

**For each NEW project in `D:\Github Local\`:**

1. **Copy the containerization template:**
   ```bash
   cd D:\Github Local\<new_project>
   
   # Copy from Jukwa (reuse patterns)
   copy ..\Baraza-la-Jukwa\.dockerignore .
   copy ..\Baraza-la-Jukwa\.env.example .
   copy ..\Baraza-la-Jukwa\.env.production .
   ```

2. **Update volume prefixes** in `docker-compose.yml`:
   ```yaml
   volumes:
     new_project_postgres_data:   # NOT jukwa_*
     new_project_redis_data:
   
   services:
     postgres:
       volumes:
         - new_project_postgres_data:/var/lib/postgresql/data
   ```

3. **Run in isolation:**
   ```bash
   docker compose up -d
   # Now two projects run in parallel with separate volumes
   ```

---

### ✅ Phase 5: Cleanup & Maintenance

**Remove all Jukwa data (dangerous!):**
```bash
cd infra

# Stop containers (keep volumes)
docker compose down

# Delete volumes
docker volume rm jukwa_postgres_data jukwa_redis_data jukwa_mongodb_data jukwa_minio_data

# Purge unused volumes/images across ALL projects
docker system prune -a --volumes
```

**Backup volumes:**
```bash
# Export PostgreSQL data
docker run --rm -v jukwa_postgres_data:/data -v D:\Github_Local\Baraza-la-Jukwa\.docker-backups:/backup \
  alpine tar czf /backup/postgres-backup-$(date +%Y%m%d).tar.gz -C /data .

# Export MongoDB
docker exec jukwa-mongodb mongodump --out /tmp/backup
docker cp jukwa-mongodb:/tmp/backup D:\Github_Local\Baraza-la-Jukwa\.docker-backups\
```

---

### ✅ Phase 6: IDE Integration (Android Studio / VS Code)

**For Android UI previews (from your "antigravity" notes):**

Android Studio Preview:
```
1. Open: D:\Github Local\Baraza-la-Jukwa\android\
2. File → Open
3. Android Studio renders Jetpack Compose previews LIVE (no container needed)
```

**For Backend IDE debugging** (optional, advanced):

VS Code can attach to running containers:
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Attach to Incident Service",
      "type": "node",
      "request": "attach",
      "port": 9229,
      "address": "localhost",
      "skipFiles": ["<node_internals>/**"]
    }
  ]
}
```

Requires: Add to `services/incident/Dockerfile`:
```dockerfile
CMD ["node", "--inspect=0.0.0.0:9229", "dist/index.js"]
```

---

## Part 4: Quick Reference Checklist

### Daily Development
- [ ] `docker compose up -d` (infra folder)
- [ ] `http://localhost:8888` → Dozzle logs dashboard
- [ ] `http://localhost:3001/health` → Verify incident service
- [ ] Edit Android UI in Android Studio (Compose preview)
- [ ] Test APIs via `curl http://localhost:3001/...`

### Before Committing Code
- [ ] `docker compose logs` — no ERROR lines
- [ ] `docker system df` — under 50GB used
- [ ] `docker volume ls` — expected volumes present

### Shutdown
- [ ] `docker compose down` (keeps volumes)
- [ ] `docker system prune` (clean unused images)

### Restart
- [ ] `docker compose up -d` (auto-restores from volumes)
- [ ] `docker compose ps` — all healthy

### Production Deploy
- [ ] `.env.production` configured with real secrets
- [ ] `docker compose build --no-cache` (fresh build)
- [ ] `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d`
- [ ] `docker system df` verify resource usage
- [ ] Backup volumes before any major change

---

## Part 5: Troubleshooting Reference

### Problem: Container exits immediately
```bash
docker compose logs <service-name>
# Check exit code and error message
```

### Problem: Port already in use
```bash
# Find what's using port 3001
netstat -ano | findstr :3001
# Or in docker-compose.prod.yml, change port mappings
```

### Problem: Slow builds
```bash
# Clear build cache
docker builder prune --all

# Rebuild with verbose output
docker compose build --no-cache incident-service --progress=plain
```

### Problem: Disk space
```bash
docker system df
docker system prune -a --volumes  # ⚠️ Removes ALL unused data
```

### Problem: Volume not persisting
```bash
# Verify mount point
docker volume inspect jukwa_postgres_data

# Ensure service references volume correctly
docker compose config | findstr -A5 "volumes:"
```

---

## Summary Table: Containers vs. Volumes vs. Images

| Artifact | Storage Location | Persistence | Created by | Managed by |
|----------|------------------|-------------|-----------|-----------|
| **Image** | Docker global store (~50GB) | ✓ (unless deleted) | `docker build` or pulled | `docker image` CLI |
| **Container** | Docker VM ephemeral | ✗ (deleted with `compose down`) | `docker compose up` | `docker container` CLI |
| **Volume** | WSL2 or custom driver | ✓ (survives container deletion) | `docker volume create` or auto | `docker volume` CLI |
| **Bind Mount** | Your D:\ drive | ✓ (your filesystem) | Manual: `-v D:\path:/container/path` | Your filesystem |

---

## Next: Action Items

1. **For Baraza-la-Jukwa:** Run Phase 2 checklist above
2. **For other projects:** Prepare Phase 4 setup
3. **Optional:** Add explicit volume prefixes to `docker-compose.yml` (better multi-project clarity)
4. **Android UI:** Open `android/` folder in Android Studio for live Compose previews
5. **Backend testing:** Use Phase 3 persistence test to verify data survives restarts
