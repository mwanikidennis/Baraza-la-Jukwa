# 🚀 JUKWA Docker Setup Workflow & Checklist

## Executive Summary

**Volumes** = Persistent data storage (survives container restarts)  
**Containers** = Running processes (ephemeral, deleted with `docker compose down`)  
**Images** = Blueprints for containers (built once, reused many times)

For **Baraza-la-Jukwa**, all volumes are **prefixed with `jukwa_`** for multi-project safety in `D:\Github Local\`.

---

## 🎯 One-Time Setup (First Run)

### Prerequisites Checklist
- [ ] Windows 10/11 with WSL2 enabled
- [ ] Docker Desktop installed and running
- [ ] `D:\Github Local\Baraza-la-Jukwa` cloned locally
- [ ] PowerShell 5.0+ available
- [ ] Administrator privileges available (if needed)

### Verification Steps
```powershell
# Run in PowerShell (from project root)
docker --version              # Should show: Docker version 24.0+
docker ps                     # Should show no errors
wsl --version                 # Should show: WSL 2
```

---

## 📋 Phase 1: Initial Infrastructure Setup (One Time)

### Step 1A: Create Named Volumes
```powershell
# Navigate to project root
cd D:\Github Local\Baraza-la-Jukwa

# Run setup script (creates volumes)
.\docker-setup.ps1 -Action start -LocalDb

# Or manually:
docker volume create jukwa_postgres_data
docker volume create jukwa_redis_data
docker volume create jukwa_mosquitto_data
docker volume create jukwa_mosquitto_log
docker volume create jukwa_minio_data
docker volume create jukwa_mongodb_data

# Verify
docker volume ls | findstr jukwa_
```

**Expected Output:**
```
DRIVER    VOLUME NAME
local     jukwa_postgres_data
local     jukwa_redis_data
local     jukwa_mosquitto_data
local     jukwa_mosquitto_log
local     jukwa_minio_data
local     jukwa_mongodb_data
```

✅ **Checkpoint 1:** All 6 volumes created and listed.

---

### Step 1B: Build Docker Images
```powershell
cd D:\Github Local\Baraza-la-Jukwa\infra

# Build all service images (10-15 min on first run)
docker compose build

# Or use script:
cd ..
.\docker-setup.ps1 -Action start

# Optional: Build specific service only
cd infra
docker compose build incident-service
```

**Expected Output:**
```
[+] Building 245.3s (78/78) FINISHED
 => [jukwa/incident-service 15/15] DONE
 => [jukwa/traffic-service 15/15] DONE
 => ...
```

✅ **Checkpoint 2:** All images built successfully (`docker image ls | findstr jukwa`).

---

### Step 1C: Start Services (First Time)
```powershell
cd D:\Github Local\Baraza-la-Jukwa

# Development mode (all ports exposed)
.\docker-setup.ps1 -Action start

# Or with local PostgreSQL
.\docker-setup.ps1 -Action start -LocalDb

# Or manually:
cd infra
docker compose --profile local-db up -d
```

**Expected Output:**
```
✅ 11 containers running
NAME                              STATUS      PORTS
jukwa-postgres                    Up 2m       5432/tcp
jukwa-redis                       Up 2m       6379/tcp
jukwa-incident-service            Up 45s      3001/tcp
jukwa-commitment-service          Up 41s      3002/tcp
...
```

✅ **Checkpoint 3:** All containers healthy (`docker compose ps`).

---

## 📊 Phase 2: Verification & Testing

### Step 2A: Verify Services Are Healthy
```powershell
# Check all containers
docker compose ps

# Should show:
#   STATUS: "Up X minutes (healthy)" for most services
#   PORTS:  "0.0.0.0:3001->3001/tcp" (services exposed)

# Or use script
.\docker-setup.ps1 -Action status
```

### Step 2B: Check Volumes Are Mounted
```powershell
# Inspect a volume
docker volume inspect jukwa_postgres_data

# Output shows mountpoint in WSL2:
# "Mountpoint": "\\wsl.localhost\docker-desktop-data\..."

# Verify PostgreSQL data is being written
docker exec jukwa-postgres ls -la /var/lib/postgresql/data | findstr postgresql.conf
```

### Step 2C: Test API Endpoints
```powershell
# Test incident service
Invoke-WebRequest http://localhost:3001/health

# Response: 200 OK

# Test nginx gateway
Invoke-WebRequest http://localhost/healthz

# View logs for all services
.\docker-setup.ps1 -Action logs

# View logs for specific service
.\docker-setup.ps1 -Action logs -Service incident-service
```

### Step 2D: Access Dashboards
```
Dozzle Logs Dashboard:  http://localhost:8888
PostgreSQL Admin:       http://localhost:5432 (via admin tool)
MinIO Console:          http://localhost:9090 (login: admin/admin12345)
MQTT Broker:            localhost:1883
```

✅ **Checkpoint 4:** All endpoints respond, dashboards accessible.

---

## 💾 Phase 3: Data Persistence Testing

### Step 3A: Verify Volumes Persist After Restart

```powershell
# 1. Create test data (example: insert row via API)
curl -X POST http://localhost:3001/api/test `
  -H "Content-Type: application/json" `
  -d '{"name":"test"}'

# 2. Stop containers (volumes remain)
.\docker-setup.ps1 -Action stop

# Verify containers stopped
docker compose ps
# All should show: "Exited X minutes ago"

# Verify volumes still exist
docker volume ls | findstr jukwa_
# Should show all 6 volumes

# 3. Restart
.\docker-setup.ps1 -Action start -LocalDb

# 4. Verify data restored
curl http://localhost:3001/api/test
# Should return test data from step 1
```

**Expected Result:** Data persists across stop/start cycle.

✅ **Checkpoint 5:** Volumes persist, data survives restart.

---

## 🎮 Phase 4: Daily Development Workflow

### Morning Startup
```powershell
cd D:\Github Local\Baraza-la-Jukwa

# 1. Start services
.\docker-setup.ps1 -Action start -LocalDb
# Wait 10 seconds for health checks

# 2. Verify status
.\docker-setup.ps1 -Action status

# 3. Open dashboards
# → http://localhost:8888 (Dozzle logs)
# → http://localhost:3001/health (incident service)

# 4. Development (edit code)
# → Android UI: Open android/ in Android Studio (Jetpack Compose preview)
# → Backend: Edit services/incident/src/... and rebuild

# Rebuild specific service after code changes:
cd infra
docker compose build incident-service
docker compose up -d incident-service
```

### Monitoring & Debugging
```powershell
# View live logs
.\docker-setup.ps1 -Action logs -Service incident-service

# Or tail all logs
docker compose logs -f

# Check container health
docker compose ps

# Inspect container internals
docker exec jukwa-incident-service sh -c "ps aux | grep node"

# View container resource usage
docker stats jukwa-incident-service
```

### Evening Shutdown
```powershell
# Graceful stop (volumes persist)
.\docker-setup.ps1 -Action stop

# Clean unused Docker artifacts
.\docker-setup.ps1 -Action clean

# Optional: Backup volumes before shutdown
.\docker-setup.ps1 -Action backup
```

---

## 🔒 Phase 5: Production Deployment

### Pre-Deployment Checklist
- [ ] `.env.production` filled with real secrets
- [ ] Database migrations tested locally
- [ ] All services pass health checks
- [ ] Resource limits verified (`docker-compose.prod.yml`)
- [ ] Backup taken: `.\docker-setup.ps1 -Action backup`

### Deploy to Production
```powershell
# 1. Build fresh (no cache)
cd infra
docker compose build --no-cache

# 2. Deploy with production overrides
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# 3. Verify
docker compose -f docker-compose.yml -f docker-compose.prod.yml ps

# 4. Check resource usage
docker system df
```

**Production Features:**
- ✓ Debug ports hidden (services only via nginx)
- ✓ Resource limits enforced (CPU/memory caps)
- ✓ Restart: always (auto-recovery on failure)
- ✓ Dozzle disabled (no log dashboard exposure)

---

## 🗂️ Phase 6: Multi-Project Management

For other projects in `D:\Github Local\`:

### Setup New Project
```powershell
# 1. Copy containerization template from Jukwa
cd D:\Github Local\<new_project>

copy ..\Baraza-la-Jukwa\.dockerignore .
copy ..\Baraza-la-Jukwa\.env.example .
copy ..\Baraza-la-Jukwa\.env.production .

# 2. Update volume prefixes in docker-compose.yml
# Change: postgres_data → new_project_postgres_data
# Change: redis_data → new_project_redis_data
# etc.

# 3. Start project independently
.\docker-setup.ps1 -Action start

# Now both Jukwa and <new_project> run in parallel
docker volume ls | findstr jukwa_        # 6 Jukwa volumes
docker volume ls | findstr new_project_  # 6 new_project volumes
```

---

## 🧹 Cleanup & Maintenance

### Regular Maintenance (Weekly)
```powershell
# Clean dangling images/networks
.\docker-setup.ps1 -Action clean

# View disk usage
docker system df

# If > 50GB, prune aggressively:
docker system prune -a --volumes
```

### Emergency Reset (Lose All Data)
```powershell
# ⚠️  WARNING: This deletes all containers and volumes

.\docker-setup.ps1 -Action reset

# Or manually:
cd infra
docker compose down -v
docker volume rm jukwa_postgres_data jukwa_redis_data ...
```

### Safe Backup Before Reset
```powershell
# Backup first
.\docker-setup.ps1 -Action backup

# Data saved to: .\.docker-backups\<timestamp>\

# Then safe to reset
.\docker-setup.ps1 -Action reset
```

---

## 🆘 Troubleshooting

### Problem: "Docker Desktop is not running"
```powershell
# Solution: Start Docker Desktop
# Windows: Click Docker Desktop icon or:
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

### Problem: Container exits immediately
```powershell
# Check logs
.\docker-setup.ps1 -Action logs -Service <service_name>

# Example:
.\docker-setup.ps1 -Action logs -Service incident-service

# Look for ERROR lines and fix accordingly
```

### Problem: Port already in use
```powershell
# Find what's using port 3001
Get-NetTCPConnection -LocalPort 3001 | Select OwningProcess

# Stop the process or change port in docker-compose.yml
# From: "3001:3001"
# To:   "3101:3001"
```

### Problem: Volume space full
```powershell
# Check usage
docker system df

# If > 100GB, clean up:
docker volume prune
docker image prune -a
docker system prune -a
```

### Problem: Services not communicating
```powershell
# Verify network
docker network ls | findstr jukwa_net

# Inspect network
docker network inspect jukwa_net

# Check if services on same network
docker inspect jukwa-incident-service | findstr -A5 "Networks"
```

---

## 📱 Android UI Development

From your "antigravity" notes:

### Jetpack Compose Live Preview (No Docker Needed)
```
1. Open: D:\Github Local\Baraza-la-Jukwa\android\
2. File → Open in Android Studio
3. Navigate to: android/app/src/main/java/.../<Screen>.kt
4. Right panel shows live Compose preview (interactive!)
5. Edit, preview updates instantly (no container rebuild)
```

### Run on Emulator
```
1. Android Studio → Tools → AVD Manager
2. Create virtual device (or use existing)
3. Green Run button (top toolbar) → installs APK to emulator
4. Test the app
```

---

## 📝 Summary Checklist

| Phase | Task | Status | Date |
|-------|------|--------|------|
| 1 | Create volumes | ☐ |  |
| 2 | Build images | ☐ |  |
| 3 | Start services | ☐ |  |
| 4 | Verify health endpoints | ☐ |  |
| 5 | Test data persistence | ☐ |  |
| 6 | Open Dozzle dashboard | ☐ |  |
| 7 | Open Android UI in IDE | ☐ |  |
| 8 | Test API endpoints | ☐ |  |
| 9 | Backup volumes | ☐ |  |
| 10 | Document for team | ☐ |  |

---

## 🚀 Quick Commands Reference

```powershell
# Start (development)
.\docker-setup.ps1 -Action start

# Start (production)
.\docker-setup.ps1 -Action start -Production

# Start (with local PostgreSQL)
.\docker-setup.ps1 -Action start -LocalDb

# Check status
.\docker-setup.ps1 -Action status

# View logs
.\docker-setup.ps1 -Action logs

# View logs for service
.\docker-setup.ps1 -Action logs -Service incident-service

# Stop
.\docker-setup.ps1 -Action stop

# Backup
.\docker-setup.ps1 -Action backup

# Clean
.\docker-setup.ps1 -Action clean

# Reset (dangerous!)
.\docker-setup.ps1 -Action reset
```

---

**📌 Key Takeaways:**

1. **Volumes** live in WSL2 (fast), prefixed `jukwa_` (safe for multi-projects)
2. **Containers** are ephemeral but restart with same data (via volumes)
3. **Images** are built once per service, reused for all container instances
4. **Daily workflow:** start → develop → test → stop (repeat)
5. **Production:** use `docker-compose.prod.yml` override for hardening
