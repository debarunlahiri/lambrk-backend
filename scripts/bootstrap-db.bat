@echo off
setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0.."
set "DB_USER=debarunlahiri"
set "DB_NAME=lambrk"
set "MIG_DIR=%PROJECT_DIR%\src\main\resources\db\migration"

if not exist "%MIG_DIR%" (
  echo Migration directory not found: %MIG_DIR%
  exit /b 1
)

for /f "delims=" %%f in ('dir /b /on "%MIG_DIR%\V*.sql"') do (
  echo Applying %%f
  docker exec -i lambrk-postgres psql -v ON_ERROR_STOP=1 -U %DB_USER% -d %DB_NAME% < "%MIG_DIR%\%%f"
  if !errorlevel! neq 0 exit /b 1
)

echo DB bootstrap completed
exit /b 0
