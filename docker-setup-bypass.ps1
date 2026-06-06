# ============================================================================
# JUKWA Docker Quick-Start (PowerShell Execution Policy Bypass)
# Place in project root: D:\Github Local\Baraza-la-Jukwa\
# Usage: powershell -ExecutionPolicy Bypass -File docker-setup-bypass.ps1 -Action start
# Or simpler: .\docker-setup-bypass.ps1 -Action start (after first run)
# ============================================================================

param(
    [ValidateSet("start", "stop", "logs", "status", "reset", "backup", "clean")]
    [string]$Action = "status",
    [string]$Service = "",
    [switch]$Production,
    [switch]$LocalDb
)

function Test-DockerRunning {
    try {
        docker info *>$null
        return $true
    } catch {
        Write-Host "X Docker Desktop is not running. Please start Docker Desktop." -ForegroundColor Red
        exit 1
    }
}

function Setup-Volumes {
    Write-Host "Setting up volumes..." -ForegroundColor Cyan
    $volumes = @("jukwa_postgres_data", "jukwa_redis_data", "jukwa_mosquitto_data", "jukwa_mosquitto_log", "jukwa_minio_data", "jukwa_mongodb_data")
    
    foreach ($vol in $volumes) {
        $exists = docker volume ls --format "table {{.Name}}" | Select-String $vol
        if (-not $exists) {
            docker volume create $vol | Out-Null
            Write-Host "  + Created volume: $vol" -ForegroundColor Green
        }
    }
}

function Start-Services {
    param([bool]$Prod, [bool]$LocalDb)
    
    Write-Host "Starting services..." -ForegroundColor Cyan
    Setup-Volumes
    
    Push-Location infra
    
    if ($Prod) {
        Write-Host "Production mode" -ForegroundColor Yellow
        docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
    } elseif ($LocalDb) {
        Write-Host "Development mode with local PostgreSQL" -ForegroundColor Yellow
        docker compose --profile local-db up -d --build
    } else {
        Write-Host "Development mode" -ForegroundColor Yellow
        docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
    }
    
    Write-Host "Waiting 5 seconds for health checks..." -ForegroundColor Green
    Start-Sleep -Seconds 5
    
    Show-Status
    Pop-Location
}

function Stop-Services {
    Write-Host "Stopping services..." -ForegroundColor Yellow
    Push-Location infra
    docker compose down
    Pop-Location
    Write-Host "Services stopped (volumes persisted)" -ForegroundColor Green
}

function Show-Status {
    Write-Host "`nContainer Status:" -ForegroundColor Cyan
    docker compose ps
    
    Write-Host "`nVolume Status:" -ForegroundColor Cyan
    docker volume ls | Select-String jukwa_
    
    Write-Host "`nDisk Usage:" -ForegroundColor Cyan
    docker system df | Select-Object -Last 5
}

function View-Logs {
    param([string]$ServiceName)
    Push-Location infra
    if ($ServiceName) {
        docker compose logs --tail 50 -f $ServiceName
    } else {
        docker compose logs --tail 50 -f
    }
    Pop-Location
}

function Reset-Stack {
    Write-Host "WARNING: This will delete all containers and volumes for Jukwa!" -ForegroundColor Red
    Write-Host "Continue? (yes/no)" -ForegroundColor Yellow
    $confirm = Read-Host
    
    if ($confirm -eq "yes") {
        Push-Location infra
        docker compose down -v
        Pop-Location
        
        $volumes = @("jukwa_postgres_data", "jukwa_redis_data", "jukwa_mosquitto_data", "jukwa_mosquitto_log", "jukwa_minio_data", "jukwa_mongodb_data")
        foreach ($vol in $volumes) {
            docker volume rm $vol 2>$null
        }
        
        Write-Host "Stack reset. All volumes deleted." -ForegroundColor Green
    } else {
        Write-Host "Reset cancelled." -ForegroundColor Yellow
    }
}

function Backup-Volumes {
    Write-Host "Backing up volumes..." -ForegroundColor Cyan
    $backupDir = ".\.docker-backups\$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
    
    if (docker ps | Select-String "jukwa-postgres") {
        Write-Host "Backing up PostgreSQL..." -ForegroundColor Green
        docker run --rm -v jukwa_postgres_data:/data -v "${backupDir}:/backup" alpine tar czf /backup/postgres.tar.gz -C /data .
    }
    
    Write-Host "Backup complete: $backupDir" -ForegroundColor Green
}

function Clean-Docker {
    Write-Host "Cleaning unused Docker artifacts..." -ForegroundColor Cyan
    docker image prune -f | Out-Null
    docker network prune -f | Out-Null
    Write-Host "Cleanup complete" -ForegroundColor Green
    Show-Status
}

if (-not (Test-DockerRunning)) {
    exit 1
}

switch ($Action) {
    "start" { Start-Services -Prod $Production -LocalDb $LocalDb }
    "stop" { Stop-Services }
    "logs" { View-Logs -ServiceName $Service }
    "status" { Show-Status }
    "reset" { Reset-Stack }
    "backup" { Backup-Volumes }
    "clean" { Clean-Docker }
    default { Write-Host "Invalid action. Use: start, stop, logs, status, reset, backup, clean" -ForegroundColor Red; exit 1 }
}
