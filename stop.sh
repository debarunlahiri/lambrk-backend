#!/bin/bash

# ============================================================
# Lambrk Backend - Stop Script
# Gracefully stops the app and optionally Docker services
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'
BOLD='\033[1m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$PROJECT_DIR/.lambrk.pid"
APP_PORT=9500

log() { echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"; }
success() { echo -e "${GREEN}  ✓${NC} $1"; }
warn() { echo -e "${YELLOW}  ⚠${NC} $1"; }
fail() { echo -e "${RED}  ✗${NC} $1"; }

echo -e "${BOLD}"
echo "  ╔══════════════════════════════════════════╗"
echo "  ║       Lambrk Backend - Stop              ║"
echo "  ╚══════════════════════════════════════════╝"
echo -e "${NC}"

# ============================================================
# 1. Stop the Spring Boot app
# ============================================================
stop_app() {
    log "Stopping application..."

    STOPPED=false

    # Try PID file first
    if [ -f "$PID_FILE" ]; then
        APP_PID=$(cat "$PID_FILE")
        if ps -p $APP_PID &> /dev/null; then
            log "Sending SIGTERM to PID $APP_PID (graceful shutdown)..."
            kill $APP_PID 2>/dev/null || true

            # Wait up to 30s for graceful shutdown
            for i in $(seq 1 30); do
                if ! ps -p $APP_PID &> /dev/null; then
                    success "App stopped gracefully (PID: $APP_PID)"
                    STOPPED=true
                    break
                fi
                sleep 1
            done

            # Force kill if still running
            if ! $STOPPED && ps -p $APP_PID &> /dev/null; then
                warn "Graceful shutdown timed out. Force killing..."
                kill -9 $APP_PID 2>/dev/null || true
                success "App force stopped (PID: $APP_PID)"
                STOPPED=true
            fi
        else
            warn "PID $APP_PID from pid file is not running"
        fi
        rm -f "$PID_FILE"
    fi

    # Also kill anything on the app port
    PID=$(lsof -ti :$APP_PORT 2>/dev/null || true)
    if [ -n "$PID" ]; then
        PROC_NAME=$(ps -p $PID -o comm= 2>/dev/null || echo "unknown")
        if [[ "$PROC_NAME" != *"docker"* ]] && [[ "$PROC_NAME" != *"com.docker"* ]]; then
            log "Killing process on port $APP_PORT (PID: $PID - $PROC_NAME)"
            kill -9 $PID 2>/dev/null || true
            success "Port $APP_PORT freed"
            STOPPED=true
        fi
    fi

    if ! $STOPPED; then
        success "No running app instance found"
    fi
}

# ============================================================
# 2. Optionally stop Docker services
# ============================================================
stop_services() {
    if [ "$1" = "--all" ] || [ "$1" = "-a" ]; then
        log "Stopping Docker Compose services..."
        cd "$PROJECT_DIR"

        if command -v docker compose &> /dev/null; then
            docker compose down 2>&1 | tail -5
        else
            docker-compose down 2>&1 | tail -5
        fi
        success "All Docker services stopped"

    elif [ "$1" = "--clean" ] || [ "$1" = "-c" ]; then
        log "Stopping Docker services and removing volumes..."
        cd "$PROJECT_DIR"

        if command -v docker compose &> /dev/null; then
            docker compose down -v 2>&1 | tail -5
        else
            docker-compose down -v 2>&1 | tail -5
        fi
        success "All Docker services stopped and volumes removed"

    else
        log "Docker services left running (use --all to stop, --clean to stop + remove data)"
    fi
}

# ============================================================
# 3. Cleanup
# ============================================================
cleanup() {
    log "Cleaning up..."
    rm -f "$PID_FILE"
    success "Cleanup done"
}

# ============================================================
# Main
# ============================================================
stop_app
stop_services "$1"
cleanup

echo ""
echo -e "  ${BOLD}Usage:${NC}"
echo -e "    ./stop.sh          Stop app only (keep Docker services)"
echo -e "    ./stop.sh --all    Stop app + Docker services"
echo -e "    ./stop.sh --clean  Stop app + Docker + remove data volumes"
echo ""
