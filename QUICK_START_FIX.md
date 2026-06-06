# 🚀 Quick Start — Fix Your PowerShell Issue

## The Problem
You're in: `D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa\`
PowerShell cannot find `docker-setup.ps1` because it's in the parent folder.

## Solutions (Pick One)

---

## ✅ Solution 1: Use Batch Script (Easiest, No Policy Changes)

Navigate to **parent folder** first:

```powershell
# You are here:
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa>

# Go UP one level:
cd ..

# Now you're here (correct location):
PS D:\Github Local\Baraza-la-Jukwa>

# Use batch script (works with default PowerShell settings)
.\docker-setup.bat start

# Or with options:
.\docker-setup.bat start -localdb     # With local PostgreSQL
.\docker-setup.bat start -prod        # Production mode
.\docker-setup.bat status             # Check status
.\docker-setup.bat logs               # View logs
.\docker-setup.bat logs incident      # Specific service logs
.\docker-setup.bat stop               # Stop services
.\docker-setup.bat backup             # Backup volumes
.\docker-setup.bat clean              # Clean artifacts
```

**This is the fastest fix.** Batch files work without any PowerShell execution policy changes.

---

## ✅ Solution 2: Use PowerShell with Bypass Flag

If you prefer PowerShell:

```powershell
# From ANY folder, run:
powershell -ExecutionPolicy Bypass -File "D:\Github Local\Baraza-la-Jukwa\docker-setup-bypass.ps1" -Action start

# Or simpler (from project root):
cd "D:\Github Local\Baraza-la-Jukwa"
powershell -ExecutionPolicy Bypass -File docker-setup-bypass.ps1 -Action start
```

**One-liner shortcut** (paste into PowerShell):
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser; cd "D:\Github Local\Baraza-la-Jukwa"; .\docker-setup-bypass.ps1 -Action start
```

---

## ✅ Solution 3: Fix Execution Policy Permanently

If you want PowerShell scripts to work globally:

```powershell
# Run PowerShell as Administrator and execute:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Then you can run any script:
cd "D:\Github Local\Baraza-la-Jukwa"
.\docker-setup-bypass.ps1 -Action start
```

---

## Your Current Error Explained

```
PS D:\Github Local\Baraza-la-Jukwa\Baraza-la-Jukwa> .\docker-setup.ps1
# ERROR: Script not found in CURRENT directory
```

**Why?** Your file structure is:
```
D:\Github Local\
└── Baraza-la-Jukwa\              ← Script is here (docker-setup.ps1)
    └── Baraza-la-Jukwa\          ← You are here (nested folder)
        ├── infra\
        ├── services\
        └── ...
```

The script is ONE LEVEL UP, not in the current folder.

---

## 🎯 Immediate Action (Copy & Paste)

**Open PowerShell and run this:**

```powershell
cd "D:\Github Local\Baraza-la-Jukwa"
.\docker-setup.bat start
```

That's it! The batch script requires no permission changes and handles everything.

---

## 📋 Full Command Reference

### Batch Script (`docker-setup.bat`)
```bash
# Start services
docker-setup.bat start              # Development
docker-setup.bat start -localdb     # With local PostgreSQL
docker-setup.bat start -prod        # Production hardened

# View status
docker-setup.bat status             # Containers + volumes + disk usage
docker-setup.bat logs               # All logs (tail)
docker-setup.bat logs incident      # Specific service logs

# Manage
docker-setup.bat stop               # Graceful stop (keep volumes)
docker-setup.bat backup             # Backup all data
docker-setup.bat clean              # Clean unused Docker artifacts
docker-setup.bat reset              # DELETE everything (ask for confirmation)
```

### PowerShell Script (`docker-setup-bypass.ps1`)
```powershell
# Same commands as batch, but PowerShell syntax:
.\docker-setup-bypass.ps1 -Action start
.\docker-setup-bypass.ps1 -Action start -LocalDb
.\docker-setup-bypass.ps1 -Action start -Production
.\docker-setup-bypass.ps1 -Action logs -Service incident
.\docker-setup-bypass.ps1 -Action status
.\docker-setup-bypass.ps1 -Action backup
```

---

## ✅ Correct Directory Structure

After you run the correct commands, your project should look like:

```
D:\Github Local\Baraza-la-Jukwa\           ← RUN COMMANDS FROM HERE
├── docker-setup.bat                       ← Use this
├── docker-setup.ps1                       ← Or this (with -ExecutionPolicy Bypass)
├── docker-setup-bypass.ps1               ← Or this (easier PowerShell version)
├── infra\
│   ├── docker-compose.yml
│   ├── docker-compose.dev.yml             ← FIXED (now has proper service refs)
│   ├── docker-compose.prod.yml
│   └── ...
├── services\
│   ├── incident\
│   ├── commitment\
│   └── ...
└── Baraza-la-Jukwa\                       ← This is the old nested folder (ignore it)
```

---

## 🔧 Troubleshooting

### Error: "docker-setup.bat: command not found"
**Fix:** You're still in the nested folder
```powershell
cd ..  # Go up one level
.\docker-setup.bat start
```

### Error: "The term '.\docker-setup.ps1' is not recognized"
**Fix:** Either:
1. Use batch script: `.\docker-setup.bat start`
2. Or use bypass flag: `powershell -ExecutionPolicy Bypass -File docker-setup-bypass.ps1 -Action start`

### Error: "docker compose command not found"
**Fix:** Docker Desktop is not running
```powershell
# Start Docker Desktop (wait 30 seconds for it to initialize)
# Then try again
```

### Error: "docker compose service has neither image nor build context"
**Fix:** FIXED! The `docker-compose.dev.yml` has been corrected. Run:
```powershell
cd D:\Github Local\Baraza-la-Jukwa
.\docker-setup.bat start
```

---

## ✨ Now What?

After running `.\docker-setup.bat start`, you'll have:
- ✅ All volumes created (jukwa_*)
- ✅ All images built
- ✅ All containers running
- ✅ Health checks passing

Then:
1. **View logs:** http://localhost:8888 (Dozzle dashboard)
2. **Test API:** http://localhost:3001/health (incident service)
3. **Edit code:** Open `services/incident/src/` in your IDE
4. **Test Android UI:** Open `android/` in Android Studio (Jetpack Compose preview)

---

## 📱 Next: Android UI Preview

From your "antigravity" notes:

```
1. Open Android Studio
2. File → Open → D:\Github Local\Baraza-la-Jukwa\android
3. Navigate to: android/app/src/main/java/.../<Screen>.kt
4. Right panel shows LIVE Compose preview (interactive!)
5. Edit code, preview updates instantly
```

---

**TL;DR:** 
1. `cd "D:\Github Local\Baraza-la-Jukwa"` (go UP one folder)
2. `.\docker-setup.bat start` (use batch script)
3. Wait 10 seconds
4. Open http://localhost:8888 (view logs)
5. Done!
