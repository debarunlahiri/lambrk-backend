@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

:: ============================================================
:: Lambrk Backend - Run Script (Windows)
:: Kills existing ports, starts services, runs the application
:: ============================================================

set "PROJECT_DIR=%~dp0"
set "PID_FILE=%PROJECT_DIR%.lambrk.pid"
set "LOG_FILE=%PROJECT_DIR%app.log"
set APP_PORT=9500
set DB_USER=lambrk_user
set DB_NAME=lambrk_db

echo.
echo   ======================================
echo     Lambrk Backend - Run (Windows)
echo   ======================================
echo.

:: ============================================================
:: 1. Kill previous instance
:: ============================================================
echo [1/4] Stopping previous instance...

if exist "%PID_FILE%" (
    set /p OLD_PID=<"%PID_FILE%"
    tasklist /FI "PID eq !OLD_PID!" 2>nul | findstr /i "java" >nul 2>&1
    if !errorlevel!==0 (
        echo   Killing previous instance PID !OLD_PID!...
        taskkill /PID !OLD_PID! /F >nul 2>&1
        timeout /t 2 /nobreak >nul
        echo   [OK] Previous instance stopped
    )
    del "%PID_FILE%" >nul 2>&1
)

:: Kill anything on app port
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%APP_PORT% " ^| findstr "LISTENING" 2^>nul') do (
    echo   Killing process on port %APP_PORT% (PID: %%p)...
    taskkill /PID %%p /F >nul 2>&1
    timeout /t 1 /nobreak >nul
    echo   [OK] Port %APP_PORT% freed
)

echo.

:: ============================================================
:: 2. Ensure Docker services
:: ============================================================
echo [2/4] Checking infrastructure services...

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo   [FAIL] Docker is not running. Start Docker Desktop first.
    exit /b 1
)

cd /d "%PROJECT_DIR%"

:: Count running containers
set RUNNING=0
for /f %%c in ('docker compose ps --format "{{.Name}}" 2^>nul') do set /a RUNNING+=1

if !RUNNING! lss 3 (
    echo   Starting Docker Compose services...
    docker compose up -d 2>&1
) else (
    echo   [OK] Docker services already running (!RUNNING! containers)
)

:: Wait for PostgreSQL
echo   Waiting for PostgreSQL...
set PG_READY=0
for /l %%i in (1,1,20) do (
    if !PG_READY!==0 (
        docker exec lambrk-postgres pg_isready -U %DB_USER% -d %DB_NAME% >nul 2>&1
        if !errorlevel!==0 (
            echo   [OK] PostgreSQL ready
            set PG_READY=1
        ) else (
            timeout /t 2 /nobreak >nul
        )
    )
)
if !PG_READY!==0 (
    echo   [FAIL] PostgreSQL not ready
    exit /b 1
)

:: Wait for Redis
echo   Waiting for Redis...
set REDIS_READY=0
for /l %%i in (1,1,10) do (
    if !REDIS_READY!==0 (
        docker exec lambrk-redis redis-cli ping >nul 2>&1
        if !errorlevel!==0 (
            echo   [OK] Redis ready
            set REDIS_READY=1
        ) else (
            timeout /t 2 /nobreak >nul
        )
    )
)

echo.

:: ============================================================
:: 3. Verify DB tables
:: ============================================================
echo [3/4] Verifying database tables...

for /f "tokens=*" %%t in ('docker exec lambrk-postgres psql -U %DB_USER% -d %DB_NAME% -t -c "SELECT COUNT(*) FROM pg_tables WHERE schemaname = ''public'';" 2^>nul') do (
    set TABLE_COUNT=%%t
)
set TABLE_COUNT=!TABLE_COUNT: =!

if defined TABLE_COUNT (
    if !TABLE_COUNT! gtr 0 (
        echo   [OK] Database has !TABLE_COUNT! tables
    ) else (
        echo   [WARN] No tables found. Flyway will create them on startup.
    )
) else (
    echo   [WARN] Could not check tables
)

echo.

:: ============================================================
:: 4. Run the application
:: ============================================================
echo [4/4] Starting Lambrk Backend...

cd /d "%PROJECT_DIR%"

set MVN_CMD=mvn
if exist "%PROJECT_DIR%mvnw.cmd" set MVN_CMD=mvnw.cmd

if "%1"=="-d" goto :run_background
if "%1"=="bg" goto :run_background
if "%1"=="background" goto :run_background

echo.
echo   Running in foreground. Press Ctrl+C to stop.
echo   Swagger: http://localhost:%APP_PORT%/swagger-ui.html
echo.
call %MVN_CMD% spring-boot:run -DskipTests
goto :eof

:run_background
echo   Running in background mode. Logs: %LOG_FILE%
start /b cmd /c "%MVN_CMD% spring-boot:run -DskipTests > "%LOG_FILE%" 2>&1"

:: Get the PID
timeout /t 5 /nobreak >nul
for /f "tokens=2" %%p in ('tasklist /FI "IMAGENAME eq java.exe" /FO LIST 2^>nul ^| findstr "PID"') do (
    echo %%p > "%PID_FILE%"
    echo   [OK] App started with PID: %%p
)

echo.
echo   Waiting for app to start...
set APP_READY=0
for /l %%i in (1,1,60) do (
    if !APP_READY!==0 (
        curl -s http://localhost:%APP_PORT%/actuator/health >nul 2>&1
        if !errorlevel!==0 (
            echo   [OK] App is UP and healthy!
            set APP_READY=1
        ) else (
            timeout /t 3 /nobreak >nul
        )
    )
)

echo.
echo   App URL:     http://localhost:%APP_PORT%
echo   Swagger:     http://localhost:%APP_PORT%/swagger-ui.html
echo   Health:      http://localhost:%APP_PORT%/actuator/health
echo   Logs:        type %LOG_FILE%
echo   Stop:        stop.bat
echo.

goto :eof
