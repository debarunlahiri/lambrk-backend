@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul 2>&1

:: ============================================================
:: Lambrk Backend - Stop Script (Windows)
:: Gracefully stops the app and optionally Docker services
:: ============================================================

set "PROJECT_DIR=%~dp0"
set "PID_FILE=%PROJECT_DIR%.lambrk.pid"
set APP_PORT=9500

echo.
echo   ======================================
echo     Lambrk Backend - Stop (Windows)
echo   ======================================
echo.

:: ============================================================
:: 1. Stop the Spring Boot app
:: ============================================================
echo [1/3] Stopping application...

set STOPPED=0

:: Try PID file first
if exist "%PID_FILE%" (
    set /p APP_PID=<"%PID_FILE%"
    tasklist /FI "PID eq !APP_PID!" 2>nul | findstr /i "java" >nul 2>&1
    if !errorlevel!==0 (
        echo   Sending termination signal to PID !APP_PID!...
        taskkill /PID !APP_PID! /F >nul 2>&1
        timeout /t 3 /nobreak >nul
        echo   [OK] App stopped (PID: !APP_PID!)
        set STOPPED=1
    ) else (
        echo   [WARN] PID !APP_PID! from pid file is not running
    )
    del "%PID_FILE%" >nul 2>&1
)

:: Kill anything on the app port
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%APP_PORT% " ^| findstr "LISTENING" 2^>nul') do (
    echo   Killing process on port %APP_PORT% (PID: %%p)...
    taskkill /PID %%p /F >nul 2>&1
    echo   [OK] Port %APP_PORT% freed
    set STOPPED=1
)

if !STOPPED!==0 (
    echo   [OK] No running app instance found
)

echo.

:: ============================================================
:: 2. Optionally stop Docker services
:: ============================================================
echo [2/3] Docker services...

if "%1"=="--all" goto :stop_docker
if "%1"=="-a" goto :stop_docker
if "%1"=="--clean" goto :clean_docker
if "%1"=="-c" goto :clean_docker

echo   Docker services left running (use --all to stop, --clean to stop + remove data)
goto :cleanup

:stop_docker
echo   Stopping Docker Compose services...
cd /d "%PROJECT_DIR%"
docker compose down 2>&1
echo   [OK] All Docker services stopped
goto :cleanup

:clean_docker
echo   Stopping Docker services and removing volumes...
cd /d "%PROJECT_DIR%"
docker compose down -v 2>&1
echo   [OK] All Docker services stopped and volumes removed
goto :cleanup

:: ============================================================
:: 3. Cleanup
:: ============================================================
:cleanup
echo.
echo [3/3] Cleaning up...
if exist "%PID_FILE%" del "%PID_FILE%" >nul 2>&1
echo   [OK] Cleanup done

echo.
echo   Usage:
echo     stop.bat          Stop app only (keep Docker services)
echo     stop.bat --all    Stop app + Docker services
echo     stop.bat --clean  Stop app + Docker + remove data volumes
echo.
