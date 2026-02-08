@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

:: ============================================================
:: Lambrk Backend - Setup Script (Windows)
:: Checks dependencies, services, ports, DB tables
:: ============================================================

set "PROJECT_DIR=%~dp0"
set "LOG_FILE=%PROJECT_DIR%setup.log"

set APP_PORT=9500
set POSTGRES_PORT=5432
set REDIS_PORT=6379
set KAFKA_PORT=9092
set ZOOKEEPER_PORT=2181
set PROMETHEUS_PORT=9090
set GRAFANA_PORT=3000
set ZIPKIN_PORT=9411

set DB_NAME=lambrk_db
set DB_USER=lambrk_user
set DB_PASS=lambrk_password

set ERRORS=0

echo.
echo   ======================================
echo     Lambrk Backend - Setup (Windows)
echo   ======================================
echo.

echo [%time%] Setup started > "%LOG_FILE%"

:: ============================================================
:: 1. Check Dependencies
:: ============================================================
echo [1/6] Checking System Dependencies
echo ----------------------------------------

:: Java
where java >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        echo   [OK] Java: %%~v
    )
) else (
    echo   [FAIL] Java not found. Install JDK 21+ from https://adoptium.net/
    set /a ERRORS+=1
)

:: Maven
where mvn >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=3" %%v in ('mvn -version 2^>^&1 ^| findstr /i "Apache Maven"') do (
        echo   [OK] Maven: %%v
    )
) else if exist "%PROJECT_DIR%mvnw.cmd" (
    echo   [OK] Maven Wrapper found (mvnw.cmd)
) else (
    echo   [FAIL] Maven not found. Install from https://maven.apache.org/
    set /a ERRORS+=1
)

:: Docker
where docker >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=3 delims= " %%v in ('docker --version 2^>^&1') do (
        echo   [OK] Docker: %%v
    )
) else (
    echo   [FAIL] Docker not found. Install from https://docker.com/
    set /a ERRORS+=1
)

:: Docker Compose
docker compose version >nul 2>&1
if %errorlevel%==0 (
    echo   [OK] Docker Compose: available
) else (
    docker-compose --version >nul 2>&1
    if %errorlevel%==0 (
        echo   [OK] Docker Compose (legacy): available
    ) else (
        echo   [FAIL] Docker Compose not found
        set /a ERRORS+=1
    )
)

:: Git
where git >nul 2>&1
if %errorlevel%==0 (
    echo   [OK] Git: available
) else (
    echo   [WARN] Git not found (optional)
)

:: curl
where curl >nul 2>&1
if %errorlevel%==0 (
    echo   [OK] curl: available
) else (
    echo   [WARN] curl not found (optional)
)

echo.

:: ============================================================
:: 2. Check Ports
:: ============================================================
echo [2/6] Checking Required Ports
echo ----------------------------------------

call :check_port %APP_PORT% "App"
call :check_port %POSTGRES_PORT% "PostgreSQL"
call :check_port %REDIS_PORT% "Redis"
call :check_port %KAFKA_PORT% "Kafka"
call :check_port %ZOOKEEPER_PORT% "Zookeeper"
call :check_port %PROMETHEUS_PORT% "Prometheus"
call :check_port %GRAFANA_PORT% "Grafana"
call :check_port %ZIPKIN_PORT% "Zipkin"

echo.

:: ============================================================
:: 3. Check Docker
:: ============================================================
echo [3/6] Checking Docker Status
echo ----------------------------------------

docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo   [FAIL] Docker daemon is not running. Please start Docker Desktop.
    set /a ERRORS+=1
    goto :skip_services
)
echo   [OK] Docker daemon is running
echo.

:: ============================================================
:: 4. Start Services
:: ============================================================
echo [4/6] Starting Infrastructure Services
echo ----------------------------------------

cd /d "%PROJECT_DIR%"

echo   Starting Docker Compose services...
docker compose up -d 2>&1

echo   Waiting for services...

:: Wait for PostgreSQL
echo   Waiting for PostgreSQL...
set PG_READY=0
for /l %%i in (1,1,30) do (
    if !PG_READY!==0 (
        docker exec lambrk-postgres pg_isready -U %DB_USER% -d %DB_NAME% >nul 2>&1
        if !errorlevel!==0 (
            echo   [OK] PostgreSQL is ready
            set PG_READY=1
        ) else (
            timeout /t 2 /nobreak >nul
        )
    )
)
if !PG_READY!==0 (
    echo   [FAIL] PostgreSQL failed to start
    set /a ERRORS+=1
)

:: Wait for Redis
echo   Waiting for Redis...
set REDIS_READY=0
for /l %%i in (1,1,15) do (
    if !REDIS_READY!==0 (
        docker exec lambrk-redis redis-cli ping >nul 2>&1
        if !errorlevel!==0 (
            echo   [OK] Redis is ready
            set REDIS_READY=1
        ) else (
            timeout /t 2 /nobreak >nul
        )
    )
)
if !REDIS_READY!==0 (
    echo   [FAIL] Redis failed to start
    set /a ERRORS+=1
)

:: Check other containers
for %%c in (lambrk-kafka lambrk-prometheus lambrk-grafana lambrk-zipkin) do (
    docker ps --format "{{.Names}}" | findstr /i "%%c" >nul 2>&1
    if !errorlevel!==0 (
        echo   [OK] %%c is running
    ) else (
        echo   [WARN] %%c is not running
    )
)

echo.

:skip_services

:: ============================================================
:: 5. Check Database Tables
:: ============================================================
echo [5/6] Checking Database Tables
echo ----------------------------------------

docker exec lambrk-postgres psql -U %DB_USER% -d %DB_NAME% -c "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo   [FAIL] Cannot connect to database
    set /a ERRORS+=1
    goto :skip_tables
)
echo   [OK] Database connection OK

for %%t in (users sublambrks posts comments votes user_sublambrk_memberships user_sublambrk_moderators free_tier_usage api_logs flyway_schema_history) do (
    docker exec lambrk-postgres psql -U %DB_USER% -d %DB_NAME% -t -c "SELECT COUNT(*) FROM %%t;" >nul 2>&1
    if !errorlevel!==0 (
        echo   [OK] Table '%%t' exists
    ) else (
        echo   [MISS] Table '%%t' is missing
    )
)

echo.

:skip_tables

:: ============================================================
:: 6. Build Project
:: ============================================================
echo [6/6] Building Project
echo ----------------------------------------

cd /d "%PROJECT_DIR%"

if exist "%PROJECT_DIR%mvnw.cmd" (
    call mvnw.cmd clean compile -DskipTests -q 2>nul
) else (
    call mvn clean compile -DskipTests -q 2>nul
)

if %errorlevel%==0 (
    echo   [OK] Build successful
) else (
    echo   [WARN] Build had issues. App may still run.
)

echo.

:: ============================================================
:: Summary
:: ============================================================
echo ========================================
echo   Setup Summary
echo ========================================

if %ERRORS%==0 (
    echo   All checks passed! Ready to run.
) else (
    echo   Setup completed with %ERRORS% issue(s).
)

echo.
echo   Service URLs:
echo     App:        http://localhost:%APP_PORT%
echo     Swagger:    http://localhost:%APP_PORT%/swagger-ui.html
echo     Actuator:   http://localhost:%APP_PORT%/actuator/health
echo     Grafana:    http://localhost:%GRAFANA_PORT% (admin/admin)
echo     Prometheus: http://localhost:%PROMETHEUS_PORT%
echo     Zipkin:     http://localhost:%ZIPKIN_PORT%
echo.
echo   Next steps:
echo     Run the app:  run.bat
echo     Stop the app: stop.bat
echo.

goto :eof

:: ============================================================
:: Helper: Check if port is in use
:: ============================================================
:check_port
set PORT=%1
set NAME=%~2
netstat -ano | findstr ":%PORT% " | findstr "LISTENING" >nul 2>&1
if %errorlevel%==0 (
    for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT% " ^| findstr "LISTENING"') do (
        echo   [WARN] Port %PORT% (%NAME%) in use by PID %%p
        set /p KILL_IT="    Kill process? (y/N): "
        if /i "!KILL_IT!"=="y" (
            taskkill /PID %%p /F >nul 2>&1
            echo   [OK] Killed PID %%p
        )
    )
) else (
    echo   [OK] Port %PORT% (%NAME%) is free
)
goto :eof
