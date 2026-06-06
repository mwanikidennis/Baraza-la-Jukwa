# ⚡ Copy-Paste Commands — No Thinking Required

## The Problem (Your Current State)
```
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa> .\docker-setup.ps1 -Action start
# ERROR: Script not found
```

## The Solution (What To Do)

### Step 1: Copy & Paste This (Click PowerShell, paste, press Enter)
```powershell
cd "D:\Github Local\Baraza-la-Jukwa"
```

### Step 2: Verify You're In The Right Place
After pasting Step 1, you should see:
```
PS D:\Github Local\Baraza-la-Jukwa>
```

NOT:
```
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa>  ← WRONG
```

If you see the wrong one, paste Step 1 again.

### Step 3: List Files to Double-Check
```powershell
ls *.bat
```

You should see:
```
docker-setup.bat
```

If you don't, you're still in the wrong folder. Paste Step 1 again.

### Step 4: Now Start Docker Services
```powershell
.\docker-setup.bat start
```

### Step 5: Wait 10 Seconds, Then Check
```powershell
docker compose ps
```

Should show 11-12 containers with status "Up X seconds (healthy)"

---

## Common Issues & Fixes

### Issue 1: "docker-setup.bat: command not found"
**You're still in the nested folder.**

Copy & paste this:
```powershell
cd ..
.\docker-setup.bat start
```

### Issue 2: "Docker Desktop is not running"
**Docker isn't started.**

1. Click Windows Start menu
2. Type "Docker Desktop"
3. Click the app
4. Wait 1 minute for it to start
5. Then try Step 4 above again

### Issue 3: "docker: command not found"
**Docker is not installed or not in PATH.**

Install Docker Desktop from: https://www.docker.com/products/docker-desktop

### Issue 4: "service has neither image nor build context"
**FIXED! This was a docker-compose.dev.yml bug. Your version now works.**

Just run:
```powershell
cd "D:\Github Local\Baraza-la-Jukwa"
.\docker-setup.bat start
```

---

## What Each Command Does

```powershell
# Navigate to project root (REQUIRED first step)
cd "D:\Github Local\Baraza-la-Jukwa"

# Start development environment
# Creates volumes + builds images + starts containers
.\docker-setup.bat start

# Start with local PostgreSQL (for offline work)
.\docker-setup.bat start -localdb

# Start production environment (hardened security, no debug ports)
.\docker-setup.bat start -prod

# Check what's running
.\docker-setup.bat status

# View live logs (all containers)
.\docker-setup.bat logs

# View logs for specific service
.\docker-setup.bat logs incident

# Stop all containers (keeps data in volumes)
.\docker-setup.bat stop

# Backup all data before major changes
.\docker-setup.bat backup

# Clean up unused Docker files
.\docker-setup.bat clean

# DELETE EVERYTHING (ask for confirmation)
.\docker-setup.bat reset
```

---

## What Happens When You Run `.\docker-setup.bat start`

1. ✅ Checks Docker Desktop is running
2. ✅ Creates 6 named volumes (jukwa_postgres_data, jukwa_redis_data, etc.)
3. ✅ Builds Docker images for all services (incident, traffic, identity, etc.)
4. ✅ Starts 11 containers
5. ✅ Waits for health checks to pass
6. ✅ Shows container status

**Total time:** 5-15 minutes (first run), 10-30 seconds (subsequent runs)

---

## After It Succeeds

### 1. View Live Logs
Open browser → http://localhost:8888
(You'll see a dashboard with all container logs)

### 2. Test Backend API
Open browser → http://localhost:3001/health
(Should show: {"status":"ok"})

### 3. Edit & Test Code
Backend services are in: `services/incident/src/`
Android UI is in: `android/app/src/main/java/`

After editing, rebuild with:
```powershell
.\docker-setup.bat stop
.\docker-setup.bat start
```

### 4. View Android UI Preview (No Docker Needed!)
1. Open Android Studio
2. File → Open → `D:\Github Local\Baraza-la-Jukwa\android`
3. Navigate to any `.kt` file in `app/src/main/java/`
4. Right panel shows LIVE Jetpack Compose preview
5. Edit code, preview updates instantly

---

## Keyboard Shortcuts (Make It Faster)

In PowerShell, press **UP ARROW** to repeat last command:
```powershell
.\docker-setup.bat start
# ↑ Press UP arrow to repeat
# ↑ Press UP arrow again to go back further
```

Autocomplete: Type `.\docker` then press **TAB**:
```powershell
.\docker   ← Type this
# ↑ Press TAB
# ↓ Autocompletes to:
.\docker-setup.bat
```

---

## Copy-Paste All-In-One Command

If you want to do it all in one go (from any folder):

```powershell
cd "D:\Github Local\Baraza-la-Jukwa" && .\docker-setup.bat start && echo "✓ Done! Open http://localhost:8888 to view logs"
```

Paste that into PowerShell, press Enter, wait 10 seconds.

---

## Verify Everything Works

After running `docker-setup.bat start`, paste this:

```powershell
docker compose ps
```

You should see output like:
```
CONTAINER ID   IMAGE                  COMMAND                  CREATED         STATUS                   PORTS
abc123def456   node:20-alpine         "docker-entrypoint.s…"   10 seconds ago  Up 8 seconds (healthy)   0.0.0.0:3001->3001/tcp
def456ghi789   redis:7-alpine         "docker-entrypoint.s…"   10 seconds ago  Up 8 seconds (healthy)   0.0.0.0:6379->6379/tcp
...
```

All STATUS should show: `Up X seconds (healthy)` or `Up X minutes (healthy)`

If any show `Exited`, run:
```powershell
.\docker-setup.bat logs <service_name>
```
to see what went wrong.

---

## If Something Breaks

**Step 1: Stop everything**
```powershell
.\docker-setup.bat stop
```

**Step 2: Check for errors**
```powershell
.\docker-setup.bat logs
```

**Step 3: Restart**
```powershell
.\docker-setup.bat start
```

**Step 4: If that doesn't work, restart Docker Desktop**
1. Click Docker icon in taskbar
2. Click "Quit Docker Desktop"
3. Wait 5 seconds
4. Click Docker Desktop again
5. Wait 30 seconds for it to start
6. Run `.\docker-setup.bat start` again

**Step 5: Nuclear option (deletes all data!)**
```powershell
.\docker-setup.bat reset
.\docker-setup.bat start
```

---

## Timeline Expectations

| Action | Time | Notes |
|--------|------|-------|
| `cd "D:\..."` | <1s | Change folder |
| `.\docker-setup.bat start` (1st run) | 5-15 min | Downloading/building images |
| `.\docker-setup.bat start` (2nd+ run) | 10-30s | Using cached images |
| `docker compose ps` | <1s | Check status |
| `.\docker-setup.bat stop` | 5-10s | Graceful shutdown |
| `.\docker-setup.bat logs` | 1-5s | Start streaming logs |
| Browser → http://localhost:8888 | <2s | Open Dozzle dashboard |
| Browser → http://localhost:3001/health | <2s | Test API |

---

## You're Ready! 🚀

```powershell
cd "D:\Github Local\Baraza-la-Jukwa"
.\docker-setup.bat start
```

That's literally it. Paste those two lines, wait 10 seconds, open http://localhost:8888.

If you get stuck, re-read this file focusing on the "Common Issues & Fixes" section.

**Questions?** Check:
1. FOLDER_STRUCTURE_MAP.md — Where things are located
2. QUICK_START_FIX.md — Troubleshooting execution policy
3. WORKFLOW_CHECKLIST.md — Full detailed workflow
4. DOCKER_VOLUMES_CONTAINERS_STRATEGY.md — Deep conceptual explanation

You've got this! 💪
