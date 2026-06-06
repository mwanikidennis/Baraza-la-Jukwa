# 📁 Folder Structure & Where to Run Commands

## Your Current Confusion

You're in this nested folder:
```
D:\Github Local\Baraza-la-Jukwa\
└── Baraza-la-Jukwa\           ← YOU ARE HERE (wrong!)
    ├── infra\
    ├── services\
    ├── android\
    └── package.json
```

But the scripts are in the PARENT folder:
```
D:\Github Local\
└── Baraza-la-Jukwa\           ← SCRIPTS ARE HERE (docker-setup.bat, etc.)
    ├── docker-setup.bat       ← ← ← YOU NEED TO BE HERE
    ├── docker-setup.ps1
    ├── docker-setup-bypass.ps1
    ├── infra\
    ├── services\
    ├── android\
    ├── package.json
    └── Baraza-la-Jukwa\       ← Nested subfolder (can ignore)
```

---

## How to Fix

### Step 1: See Where You Are Now
```powershell
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa> pwd
# Shows: D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa
```

### Step 2: Go UP One Level
```powershell
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa> cd ..

PS D:\Github Local\Baraza-la-Jukwa> pwd
# Now shows: D:\Github Local\Baraza-la-Jukwa ✓ CORRECT!
```

### Step 3: List Files to Confirm
```powershell
PS D:\Github Local\Baraza-la-Jukwa> ls

Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d----          29/05/2026    23:24                infra
d----          29/05/2026    23:24                services
d----          29/05/2026    23:24                android
d----          29/05/2026    23:24                Baraza-la-Jukwa      ← nested folder
-a---          29/05/2026    23:24            6111 docker-setup.bat     ✓
-a---          29/05/2026    23:24           5028 docker-setup-bypass.ps1 ✓
-a---          29/05/2026    23:24           6120 docker-setup.ps1     ✓
```

### Step 4: Run the Script
```powershell
PS D:\Github Local\Baraza-la-Jukwa> .\docker-setup.bat start

[*] Starting services...
[*] Ensuring volumes exist...
  + Created volume: jukwa_postgres_data
  + Created volume: jukwa_redis_data
  + Created volume: jukwa_mosquitto_data
  + Created volume: jukwa_mosquitto_log
  + Created volume: jukwa_minio_data
  + Created volume: jukwa_mongodb_data
[*] Development mode
[*] Waiting 5 seconds for health checks...
```

✅ SUCCESS!

---

## Why Was There a Nested Folder?

Your GitHub clone might have created:
```
D:\Github Local\
└── Baraza-la-Jukwa\           ← Repo folder (from git clone)
    └── Baraza-la-Jukwa\       ← Nested copy (unusual)
```

This happens if:
- You cloned the repo INTO a folder named `Baraza-la-Jukwa`
- Or downloaded as ZIP and extracted with auto-nesting

**Solution:** You can delete the nested `Baraza-la-Jukwa\Baraza-la-Jukwa\` folder if you want, but it doesn't hurt to leave it.

---

## Complete File Map (Where Everything Lives)

```
D:\Github Local\Baraza-la-Jukwa\
│
├── 📄 README.md                          # Project overview
├── 📄 package.json                       # Root package (empty)
├── 📄 package-lock.json
├── 📄 .gitignore
├── 📄 .env.example                       # Dev environment vars
├── 📄 .env.production                    # Prod environment vars
├── 📄 .dockerignore                      # Build optimization
│
├── 🚀 docker-setup.bat                   ← START HERE (batch script)
├── 🚀 docker-setup.ps1                   ← Or PowerShell (needs -ExecutionPolicy Bypass)
├── 🚀 docker-setup-bypass.ps1            ← Or PowerShell (easier)
│
├── 📚 QUICK_START_FIX.md                 ← Read this NOW
├── 📚 WORKFLOW_CHECKLIST.md              ← Full workflow guide
├── 📚 CONTAINERIZATION_GUIDE.md          ← Docker best practices
├── 📚 DOCKER_VOLUMES_CONTAINERS_STRATEGY.md ← Volumes explained
│
├── 📁 infra/                             # Docker infrastructure
│   ├── docker-compose.yml                ← Main compose file
│   ├── docker-compose.dev.yml            ← Dev overrides (FIXED)
│   ├── docker-compose.prod.yml           ← Production overrides
│   ├── postgres/                         # PostgreSQL config
│   ├── mongodb/                          # MongoDB config
│   ├── mosquitto/                        # MQTT config
│   └── nginx/                            # Nginx config
│
├── 📁 services/                          # Backend microservices
│   ├── incident/                         # Incident management (port 3001)
│   │   ├── Dockerfile
│   │   ├── package.json
│   │   └── src/
│   ├── commitment/                       # Commitment tracking (port 3002)
│   ├── traffic/                          # Traffic monitoring (port 3003)
│   ├── emergency/                        # Emergency response (port 3004)
│   ├── identity/                         # Auth/identity (port 3006)
│   ├── notification/                     # Notifications (port 3007)
│   ├── ai-agent/                         # AI agent (port 3010)
│   └── citizen-vault/                    # Data vault (port 3011)
│
├── 📁 android/                           # Native Android app
│   ├── build.gradle.kts
│   ├── app/
│   │   ├── src/main/java/               # Kotlin source code
│   │   └── build/reports/tests/         # Test reports
│   └── run_tests.sh
│
├── 📁 shared/                            # Shared TypeScript types
├── 📁 data/                              # Static seed data
├── 📁 Docs/                              # Documentation
├── 📁 .github/                           # CI/CD workflows
│
└── 📁 Baraza-la-Jukwa\                   ← Nested folder (you can ignore this)
    ├── infra\
    ├── services\
    ├── android\
    └── ... (duplicate structure)
```

---

## 🎯 Quick Command Cheat Sheet

**Navigate to correct folder:**
```powershell
cd "D:\Github Local\Baraza-la-Jukwa"
```

**Start development environment:**
```powershell
.\docker-setup.bat start
```

**Start with local PostgreSQL (for offline work):**
```powershell
.\docker-setup.bat start -localdb
```

**Start production environment:**
```powershell
.\docker-setup.bat start -prod
```

**Check status:**
```powershell
.\docker-setup.bat status
```

**View live logs:**
```powershell
.\docker-setup.bat logs
```

**View specific service logs:**
```powershell
.\docker-setup.bat logs incident
```

**Stop services (keep data):**
```powershell
.\docker-setup.bat stop
```

**Backup before major changes:**
```powershell
.\docker-setup.bat backup
```

**Clean up unused Docker artifacts:**
```powershell
.\docker-setup.bat clean
```

**Emergency reset (DELETE ALL DATA):**
```powershell
.\docker-setup.bat reset
```

---

## ✅ Verification Checklist

After running `docker-setup.bat start`:

- [ ] No errors in PowerShell
- [ ] 6 volumes created: `docker volume ls | findstr jukwa_`
- [ ] Containers running: `docker compose ps`
- [ ] Can open http://localhost:8888 (Dozzle logs)
- [ ] Can test: `curl http://localhost:3001/health`
- [ ] Can see all services healthy in Docker Desktop

---

## 🆘 If You're Still Confused

1. **Copy this exact command:**
   ```powershell
   cd "D:\Github Local\Baraza-la-Jukwa" && .\docker-setup.bat start
   ```

2. **Paste it into PowerShell**

3. **Press Enter**

4. **Wait 10 seconds**

5. **Open browser to:** http://localhost:8888

Done! 🎉

---

## Why the Nested Folder?

Looking at your `ls` output from earlier:

```
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa> ls

Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d----          29/05/2026    23:24                .devcontainer
d----          29/05/2026    23:24                .github
d----          29/05/2026    23:24                android       ← In nested folder
d----          29/05/2026    23:24                infra         ← In nested folder
...
-a---          29/05/2026    23:24            5823 CONTAINERIZATION_GUIDE.md  ← At parent level
```

Your scripts and docs are at parent level, but you were looking in the nested folder.

**Git clone typically does this:**
```
git clone https://github.com/kenyawebs/Baraza-la-Jukwa.git D:\Github Local\Baraza-la-Jukwa
# Creates: D:\Github Local\Baraza-la-Jukwa\ (project root)
```

**BUT if a ZIP download auto-nested:**
```
D:\Github Local\
└── Baraza-la-Jukwa-main\
    └── Baraza-la-Jukwa\   ← Extra nesting!
```

Either way, just `cd ..` to go up one level and you'll be fine.
