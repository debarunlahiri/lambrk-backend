#!/bin/bash
set -e

# ============================================================
# Lambrk Backend - Run Script
# Kills existing ports, starts services, runs the application
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'
BOLD='\033[1m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
PID_FILE="$PROJECT_DIR/.lambrk.pid"
LOG_FILE="$PROJECT_DIR/app.log"

APP_PORT=9500
POSTGRES_PORT=5432
REDIS_PORT=6379

log() { echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"; }
success() { echo -e "${GREEN}  ✓${NC} $1"; }
warn() { echo -e "${YELLOW}  ⚠${NC} $1"; }
fail() { echo -e "${RED}  ✗${NC} $1"; }

# ============================================================
# Kill app port if occupied by non-Docker process
# ============================================================
kill_app_port() {
    PID=$(lsof -ti :$APP_PORT 2>/dev/null || true)
    if [ -n "$PID" ]; then
        PROC_NAME=$(ps -p $PID -o comm= 2>/dev/null || echo "unknown")
        if [[ "$PROC_NAME" != *"docker"* ]] && [[ "$PROC_NAME" != *"com.docker"* ]]; then
            log "Killing existing process on port $APP_PORT (PID: $PID - $PROC_NAME)"
            kill -9 $PID 2>/dev/null || true
            sleep 1
            success "Port $APP_PORT freed"
        fi
    fi
}

# ============================================================
# Kill previous app instance
# ============================================================
kill_previous_instance() {
    if [ -f "$PID_FILE" ]; then
        OLD_PID=$(cat "$PID_FILE")
        if ps -p $OLD_PID &> /dev/null; then
            log "Stopping previous instance (PID: $OLD_PID)..."
            kill $OLD_PID 2>/dev/null || true
            sleep 3
            if ps -p $OLD_PID &> /dev/null; then
                kill -9 $OLD_PID 2>/dev/null || true
            fi
            success "Previous instance stopped"
        fi
        rm -f "$PID_FILE"
    fi
}

# ============================================================
# Ensure Docker services are running
# ============================================================
ensure_services() {
    log "Checking infrastructure services..."

    if ! docker info &> /dev/null; then
        fail "Docker is not running. Start Docker Desktop first."
        exit 1
    fi

    cd "$PROJECT_DIR"

    # Check if containers are running
    RUNNING=$(docker compose ps --format '{{.Name}}' 2>/dev/null | wc -l | tr -d ' ')
    if [ "$RUNNING" -lt 3 ]; then
        log "Starting Docker Compose services..."
        docker compose up -d 2>&1 | tail -3
    else
        success "Docker services already running ($RUNNING containers)"
    fi

    # Wait for PostgreSQL
    echo -n "  Checking PostgreSQL"
    for i in $(seq 1 20); do
        if docker exec lambrk-postgres pg_isready -U lambrk_user -d lambrk_db &> /dev/null; then
            echo ""
            success "PostgreSQL ready"
            break
        fi
        echo -n "."
        sleep 2
        if [ $i -eq 20 ]; then
            echo ""
            fail "PostgreSQL not ready"
            exit 1
        fi
    done

    # Wait for Redis
    echo -n "  Checking Redis"
    for i in $(seq 1 10); do
        if docker exec lambrk-redis redis-cli ping &> /dev/null; then
            echo ""
            success "Redis ready"
            break
        fi
        echo -n "."
        sleep 2
        if [ $i -eq 10 ]; then
            echo ""
            warn "Redis not ready (app will retry)"
        fi
    done
}

# ============================================================
# Verify DB tables exist
# ============================================================
verify_tables() {
    log "Verifying database tables..."

    TABLE_COUNT=$(docker exec lambrk-postgres psql -U lambrk_user -d lambrk_db -t -c \
        "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public';" 2>/dev/null | tr -d ' ')

    if [ -n "$TABLE_COUNT" ] && [ "$TABLE_COUNT" -gt 0 ]; then
        success "Database has $TABLE_COUNT tables"
    else
        warn "No tables found. Flyway will create them on startup."
    fi
}

# ============================================================
# Run the application
# ============================================================
run_app() {
    log "Starting Lambrk Backend..."

    cd "$PROJECT_DIR"

    MVN_CMD="mvn"
    if [ -f "$PROJECT_DIR/mvnw" ]; then
        MVN_CMD="./mvnw"
        chmod +x "$PROJECT_DIR/mvnw"
    fi

    MODE="${1:-foreground}"

    if [ "$MODE" = "background" ] || [ "$MODE" = "bg" ] || [ "$MODE" = "-d" ]; then
        log "Running in background mode. Logs: $LOG_FILE"
        nohup $MVN_CMD spring-boot:run -DskipTests > "$LOG_FILE" 2>&1 &
        APP_PID=$!
        echo $APP_PID > "$PID_FILE"
        success "App started with PID: $APP_PID"

        # Wait for app to be ready
        echo -n "  Waiting for app to start"
        for i in $(seq 1 60); do
            if curl -s http://localhost:$APP_PORT/actuator/health &> /dev/null; then
                echo ""
                success "App is UP and healthy!"
                echo ""
                echo -e "  ${BOLD}App URL:${NC}     http://localhost:$APP_PORT"
                echo -e "  ${BOLD}Swagger:${NC}     http://localhost:$APP_PORT/swagger-ui.html"
                echo -e "  ${BOLD}Health:${NC}      http://localhost:$APP_PORT/actuator/health"
                echo -e "  ${BOLD}Logs:${NC}        tail -f $LOG_FILE"
                echo -e "  ${BOLD}Stop:${NC}        ./stop.sh"
                echo ""
                return 0
            fi
            echo -n "."
            sleep 3
            if [ $i -eq 60 ]; then
                echo ""
                warn "App may still be starting. Check logs: tail -f $LOG_FILE"
            fi
        done
    else
        echo ""
        echo -e "  ${BOLD}Running in foreground. Press Ctrl+C to stop.${NC}"
        echo -e "  ${BOLD}Swagger:${NC} http://localhost:$APP_PORT/swagger-ui.html"
        echo ""
        $MVN_CMD spring-boot:run -DskipTests
    fi
}

# ============================================================
# Main
# ============================================================
main() {
    echo -e "${BOLD}"
    echo "  ╔══════════════════════════════════════════╗"
    echo "  ║       Lambrk Backend - Run               ║"
    echo "  ╚══════════════════════════════════════════╝"
    echo -e "${NC}"

    kill_previous_instance
    kill_app_port
    ensure_services
    verify_tables
    run_app "$1"
}

main "$@"
