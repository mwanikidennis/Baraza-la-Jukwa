@echo off
REM ============================================================================
REM JUKWA Docker Quick-Start Setup & Management Script (Batch Version)
REM Usage: docker-setup.bat start|stop|logs|status|reset|backup|clean [service]
REM ============================================================================

setlocal enabledelayedexpansion

if "%1"=="" (
    echo Usage: docker-setup.bat [start^|stop^|logs^|status^|reset^|backup^|clean] [options]
    echo.
    echo Examples:
    echo   docker-setup.bat start              - Start development
    echo   docker-setup.bat start -prod        - Start production
    echo   docker-setup.bat start -localdb     - Start with local PostgreSQL
    echo   docker-setup.bat logs               - View all logs
    echo   docker-setup.bat logs incident      - View incident service logs
    echo   docker-setup.bat status             - Show container and volume status
    echo   docker-setup.bat stop               - Stop all containers
    echo   docker-setup.bat backup             - Backup volumes
    echo   docker-setup.bat clean              - Clean unused Docker artifacts
    exit /b 1
)

REM Verify Docker is running
docker ps >nul 2>&1
if errorlevel 1 (
    echo [X] Docker Desktop is not running. Please start Docker Desktop.
    pause
    exit /b 1
)

cd infra

if "%1"=="start" (
    echo [*] Starting services...
    
    REM Create volumes if they don't exist
    echo [*] Ensuring volumes exist...
    for %%v in (jukwa_postgres_data jukwa_redis_data jukwa_mosquitto_data jukwa_mosquitto_log jukwa_minio_data jukwa_mongodb_data) do (
        docker volume ls | findstr "%%v" >nul 2>&1
        if errorlevel 1 (
            docker volume create %%v
            echo [+] Created volume: %%v
        )
    )
    
    REM Check for production flag
    if "%2"=="-prod" (
        echo [*] Starting production environment...
        docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
    ) else if "%2"=="-localdb" (
        echo [*] Starting development with local PostgreSQL...
        docker compose --profile local-db up -d --build
    ) else (
        echo [*] Starting development environment...
        docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build
    )
    
    timeout /t 5 /nobreak
    
    echo [+] Services started. Checking status...
    docker compose ps
    
) else if "%1"=="stop" (
    echo [*] Stopping services...
    docker compose down
    echo [+] Services stopped (volumes persisted)
    
) else if "%1"=="status" (
    echo.
    echo === Container Status ===
    docker compose ps
    
    echo.
    echo === Volume Status ===
    docker volume ls | findstr jukwa_
    
    echo.
    echo === Disk Usage ===
    docker system df
    
) else if "%1"=="logs" (
    if "%2"=="" (
        echo [*] Tailing all logs...
        docker compose logs -f
    ) else (
        echo [*] Tailing logs for %2...
        docker compose logs -f %2
    )
    
) else if "%1"=="backup" (
    echo [*] Backing up volumes...
    set BACKUP_DIR=.\.docker-backups\%date:~-4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%
    md "!BACKUP_DIR!" 2>nul
    
    docker ps | findstr jukwa-postgres >nul 2>&1
    if not errorlevel 1 (
        echo [*] Backing up PostgreSQL...
        docker run --rm -v jukwa_postgres_data:/data -v "%cd%\!BACKUP_DIR!:/backup" alpine tar czf /backup/postgres.tar.gz -C /data . 2>nul
    )
    
    docker ps | findstr jukwa-mongodb >nul 2>&1
    if not errorlevel 1 (
        echo [*] Backing up MongoDB...
        docker exec jukwa-mongodb mongodump --out /tmp/backup >nul 2>&1
        docker cp jukwa-mongodb:/tmp/backup "%cd%\!BACKUP_DIR!\mongodb" >nul 2>&1
    )
    
    echo [+] Backup complete: !BACKUP_DIR!
    
) else if "%1"=="clean" (
    echo [*] Cleaning unused Docker artifacts...
    docker image prune -f >nul 2>&1
    docker network prune -f >nul 2>&1
    echo [+] Cleanup complete
    
) else if "%1"=="reset" (
    echo.
    echo [!] WARNING: This will delete ALL containers and volumes for Jukwa!
    echo [!] Your data will be LOST. Continue? [y/n]
    set /p CONFIRM=
    
    if /i "!CONFIRM!"=="y" (
        echo [*] Resetting stack...
        docker compose down -v
        for %%v in (jukwa_postgres_data jukwa_redis_data jukwa_mosquitto_data jukwa_mosquitto_log jukwa_minio_data jukwa_mongodb_data) do (
            docker volume rm %%v 2>nul
        )
        echo [+] Stack reset. All volumes deleted.
    ) else (
        echo [X] Reset cancelled.
    )
    
) else (
    echo [X] Unknown action: %1
    exit /b 1
)

cd ..
