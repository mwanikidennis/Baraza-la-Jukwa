# ============================================================================
# JUKWA Docker Quick-Start Setup & Management Script
# Usage: .\docker-setup.ps1 -Action [start|stop|logs|status|reset|backup]
# ============================================================================

param(
    [ValidateSet("start", "stop", "logs", "status", "reset", "backup", "clean")]
    [string]$Action = "status",
    
    [string]$Service = "",  # Optional: target specific service
    
    [switch]$Production,    # Flag: Use production compose override
    
    [switch]$LocalDb        # Flag: Enable local PostgreSQL
)

# Ensure Docker is running
function Test-DockerRunning {
    try {
        docker info *>$null
        return $true
    }
    catch {
        Write-Host "❌ Docker Desktop is not running. Please start Docker Desktop." -ForegroundColor Red
        exit 1
    }
}

# Setup volumes with explicit prefixes
function Setup-Volumes {
    Write-Host "🔧 Setting up volumes..." -ForegroundColor Cyan
    
    $volumes = @(
        "jukwa_postgres_data",
        "jukwa_redis_data",
        "jukwa_mosquitto_data",
        "jukwa_mosquitto_log",
        "jukwa_minio_data",
        "jukwa_mongodb_data"
    )
    
    foreach ($vol in $volumes) {
        $exists = docker volume ls --format "table {{.Name}}" | Select-String $vol
        if (-not $exists) {
            docker volume create $vol | Out-Null
            Write-Host "  ✓ Created volume: $vol" -ForegroundColor Green
        }
    }
}

# Start services
function Start-Services {
    param([bool]$Prod, [bool]$LocalDb)
    
    Write-Host "🚀 Starting services..." -ForegroundColor Cyan
    
    Setup-Volumes
    
    $composeArgs = @("docker", "compose")
    
    if ($LocalDb) {
        $composeArgs += "--profile", "local-db"
    }
    
    $composeArgs += "up", "-d", "--build"
    
    if ($Prod) {
        $composeArgs = $composeArgs[0..($composeArgs.Length-3)] + `-f, "docker-compose.yml", `-f, "docker-compose.prod.yml" + $composeArgs[-2..-1]
    }
    
    Set-Location infra
    & $composeArgs
    Set-Location ..
    
    Write-Host "✅ Services started. Waiting 5 seconds for health checks..." -ForegroundColor Green
    Start-Sleep -Seconds 5
    
    Show-Status
}

# Stop services
function Stop-Services {
    Write-Host "⏹️  Stopping services..." -ForegroundColor Yellow
    Set-Location infra
    docker compose down
    Set-Location ..
    Write-Host "✅ Services stopped (volumes persisted)" -ForegroundColor Green
}

# Show status
function Show-Status {
    Set-Location infra
    Write-Host "`n📊 Container Status:" -ForegroundColor Cyan
    docker compose ps
    
    Write-Host "`n📦 Volume Status:" -ForegroundColor Cyan
    docker volume ls | Select-String jukwa_
    
    Write-Host "`n📈 Disk Usage:" -ForegroundColor Cyan
    docker system df | Select-Object -Last 5
    Set-Location ..
}

# View logs
function View-Logs {
    param([string]$ServiceName)
    
    Set-Location infra
    if ($ServiceName) {
        Write-Host "📋 Logs for $ServiceName:" -ForegroundColor Cyan
        docker compose logs --tail 50 -f $ServiceName
    }
    else {
        Write-Host "📋 All logs:" -ForegroundColor Cyan
        docker compose logs --tail 50 -f
    }
    Set-Location ..
}

# Reset (remove volumes and containers)
function Reset-Stack {
    Write-Host "⚠️  WARNING: This will delete all containers and volumes for Jukwa!" -ForegroundColor Red
    Write-Host "Your data will be LOST. Continue? (yes/no)" -ForegroundColor Yellow
    $confirm = Read-Host
    
    if ($confirm -eq "yes") {
        Set-Location infra
        docker compose down -v
        Set-Location ..
        
        $volumes = @("jukwa_postgres_data", "jukwa_redis_data", "jukwa_mosquitto_data", "jukwa_mosquitto_log", "jukwa_minio_data", "jukwa_mongodb_data")
        foreach ($vol in $volumes) {
            docker volume rm $vol 2>$null
        }
        
        Write-Host "✅ Stack reset. All volumes deleted." -ForegroundColor Green
    }
    else {
        Write-Host "❌ Reset cancelled." -ForegroundColor Yellow
    }
}

# Backup volumes
function Backup-Volumes {
    Write-Host "💾 Backing up volumes..." -ForegroundColor Cyan
    
    $backupDir = ".\.docker-backups\$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
    
    # Backup PostgreSQL
    if (docker ps | Select-String "jukwa-postgres") {
        Write-Host "  📦 Backing up PostgreSQL..." -ForegroundColor Green
        docker run --rm `
            -v jukwa_postgres_data:/data `
            -v "${backupDir}:/backup" `
            alpine tar czf /backup/postgres.tar.gz -C /data .
    }
    
    # Backup MongoDB
    if (docker ps | Select-String "jukwa-mongodb") {
        Write-Host "  📦 Backing up MongoDB..." -ForegroundColor Green
        docker exec jukwa-mongodb mongodump --out /tmp/backup
        docker cp jukwa-mongodb:/tmp/backup "${backupDir}/mongodb"
    }
    
    Write-Host "✅ Backup complete: $backupDir" -ForegroundColor Green
}

# Clean unused Docker artifacts
function Clean-Docker {
    Write-Host "🧹 Cleaning unused Docker artifacts..." -ForegroundColor Cyan
    
    Write-Host "  Removing dangling images..." -ForegroundColor Green
    docker image prune -f | Out-Null
    
    Write-Host "  Removing unused networks..." -ForegroundColor Green
    docker network prune -f | Out-Null
    
    Write-Host "✅ Cleanup complete" -ForegroundColor Green
    Show-Status
}

# Main entry point
if (-not (Test-DockerRunning)) {
    exit 1
}

switch ($Action) {
    "start" {
        Start-Services -Prod $Production -LocalDb $LocalDb
    }
    "stop" {
        Stop-Services
    }
    "logs" {
        View-Logs -ServiceName $Service
    }
    "status" {
        Show-Status
    }
    "reset" {
        Reset-Stack
    }
    "backup" {
        Backup-Volumes
    }
    "clean" {
        Clean-Docker
    }
    default {
        Write-Host "Invalid action. Use: start, stop, logs, status, reset, backup, clean" -ForegroundColor Red
        exit 1
    }
}
