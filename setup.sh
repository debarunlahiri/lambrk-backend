#!/bin/bash
set -e

# ============================================================
# Lambrk Backend - Setup Script
# Checks dependencies, services, ports, DB tables
# ============================================================

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color
BOLD='\033[1m'

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_FILE="$PROJECT_DIR/setup.log"

# Required ports
APP_PORT=9500
POSTGRES_PORT=5432
REDIS_PORT=6379
KAFKA_PORT=9092
ZOOKEEPER_PORT=2181
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
ZIPKIN_PORT=9411

# DB config
DB_NAME="lambrk_db"
DB_USER="lambrk_user"
DB_PASS="lambrk_password"
DB_HOST="localhost"

# Expected tables
EXPECTED_TABLES=(
    "users"
    "sublambrks"
    "posts"
    "comments"
    "votes"
    "user_sublambrk_memberships"
    "user_sublambrk_moderators"
    "free_tier_usage"
    "api_logs"
    "flyway_schema_history"
)

log() {
    echo -e "${BLUE}[$(date '+%H:%M:%S')]${NC} $1"
    echo "[$(date '+%H:%M:%S')] $(echo "$1" | sed 's/\x1b\[[0-9;]*m//g')" >> "$LOG_FILE"
}

success() {
    echo -e "${GREEN}  ✓${NC} $1"
}

warn() {
    echo -e "${YELLOW}  ⚠${NC} $1"
}

fail() {
    echo -e "${RED}  ✗${NC} $1"
}

header() {
    echo ""
    echo -e "${CYAN}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}${BOLD}  $1${NC}"
    echo -e "${CYAN}${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# Track errors
ERRORS=0

# ============================================================
# 1. Check System Dependencies
# ============================================================
check_dependencies() {
    header "1. Checking System Dependencies"

    # Java
    if command -v java &> /dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}')
        success "Java: $JAVA_VER"
    else
        fail "Java not found. Install JDK 21+ from https://adoptium.net/"
        ERRORS=$((ERRORS + 1))
    fi

    # Maven
    if command -v mvn &> /dev/null; then
        MVN_VER=$(mvn -version 2>&1 | head -1 | awk '{print $3}')
        success "Maven: $MVN_VER"
    elif [ -f "$PROJECT_DIR/mvnw" ]; then
        success "Maven Wrapper found (./mvnw)"
    else
        fail "Maven not found. Install from https://maven.apache.org/"
        ERRORS=$((ERRORS + 1))
    fi

    # Docker
    if command -v docker &> /dev/null; then
        DOCKER_VER=$(docker --version 2>&1 | awk '{print $3}' | tr -d ',')
        success "Docker: $DOCKER_VER"
    else
        fail "Docker not found. Install from https://docker.com/"
        ERRORS=$((ERRORS + 1))
    fi

    # Docker Compose
    if command -v docker compose &> /dev/null || command -v docker-compose &> /dev/null; then
        if command -v docker compose &> /dev/null; then
            DC_VER=$(docker compose version 2>&1 | awk '{print $NF}')
        else
            DC_VER=$(docker-compose --version 2>&1 | awk '{print $NF}')
        fi
        success "Docker Compose: $DC_VER"
    else
        fail "Docker Compose not found"
        ERRORS=$((ERRORS + 1))
    fi

    # Git
    if command -v git &> /dev/null; then
        GIT_VER=$(git --version | awk '{print $3}')
        success "Git: $GIT_VER"
    else
        warn "Git not found (optional)"
    fi

    # curl
    if command -v curl &> /dev/null; then
        success "curl: available"
    else
        warn "curl not found (optional, used for health checks)"
    fi
}

# ============================================================
# 2. Check and Kill Ports
# ============================================================
check_and_kill_ports() {
    header "2. Checking Required Ports"

    PORTS=($APP_PORT $POSTGRES_PORT $REDIS_PORT $KAFKA_PORT $ZOOKEEPER_PORT $PROMETHEUS_PORT $GRAFANA_PORT $ZIPKIN_PORT)
    PORT_NAMES=("App" "PostgreSQL" "Redis" "Kafka" "Zookeeper" "Prometheus" "Grafana" "Zipkin")

    for i in "${!PORTS[@]}"; do
        PORT=${PORTS[$i]}
        NAME=${PORT_NAMES[$i]}

        PID=$(lsof -ti :$PORT 2>/dev/null || true)
        if [ -n "$PID" ]; then
            PROC_NAME=$(ps -p $PID -o comm= 2>/dev/null || echo "unknown")
            warn "Port $PORT ($NAME) in use by PID $PID ($PROC_NAME)"

            # Don't kill Docker-managed ports here, they'll be handled by docker compose
            if [[ "$PROC_NAME" == *"docker"* ]] || [[ "$PROC_NAME" == *"com.docker"* ]]; then
                log "    Port $PORT managed by Docker, will be handled by docker compose"
            else
                read -p "    Kill process on port $PORT? (y/N): " -n 1 -r
                echo
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    kill -9 $PID 2>/dev/null || true
                    sleep 1
                    success "Killed process on port $PORT"
                fi
            fi
        else
            success "Port $PORT ($NAME) is free"
        fi
    done
}

# ============================================================
# 3. Check Docker Status
# ============================================================
check_docker() {
    header "3. Checking Docker Status"

    if ! docker info &> /dev/null; then
        fail "Docker daemon is not running. Please start Docker Desktop."
        ERRORS=$((ERRORS + 1))
        return 1
    fi
    success "Docker daemon is running"
    return 0
}

# ============================================================
# 4. Start Infrastructure Services
# ============================================================
start_services() {
    header "4. Starting Infrastructure Services"

    cd "$PROJECT_DIR"

    log "Starting Docker Compose services..."
    if command -v docker compose &> /dev/null; then
        docker compose up -d 2>&1 | tail -5
    else
        docker-compose up -d 2>&1 | tail -5
    fi

    log "Waiting for services to be healthy..."

    # Wait for PostgreSQL
    echo -n "  Waiting for PostgreSQL"
    for i in $(seq 1 30); do
        if docker exec lambrk-postgres pg_isready -U $DB_USER -d $DB_NAME &> /dev/null; then
            echo ""
            success "PostgreSQL is ready"
            break
        fi
        echo -n "."
        sleep 2
        if [ $i -eq 30 ]; then
            echo ""
            fail "PostgreSQL failed to start within 60s"
            ERRORS=$((ERRORS + 1))
        fi
    done

    # Wait for Redis
    echo -n "  Waiting for Redis"
    for i in $(seq 1 15); do
        if docker exec lambrk-redis redis-cli ping &> /dev/null; then
            echo ""
            success "Redis is ready"
            break
        fi
        echo -n "."
        sleep 2
        if [ $i -eq 15 ]; then
            echo ""
            fail "Redis failed to start within 30s"
            ERRORS=$((ERRORS + 1))
        fi
    done

    # Wait for Kafka
    echo -n "  Waiting for Kafka"
    for i in $(seq 1 30); do
        if docker exec lambrk-kafka kafka-topics --bootstrap-server localhost:9092 --list &> /dev/null; then
            echo ""
            success "Kafka is ready"
            break
        fi
        echo -n "."
        sleep 2
        if [ $i -eq 30 ]; then
            echo ""
            warn "Kafka may not be ready yet (will retry on app start)"
        fi
    done

    # Check other services
    for container in lambrk-prometheus lambrk-grafana lambrk-zipkin; do
        if docker ps --format '{{.Names}}' | grep -q "$container"; then
            success "$container is running"
        else
            warn "$container is not running"
        fi
    done
}

# ============================================================
# 5. Check Database Tables
# ============================================================
check_database_tables() {
    header "5. Checking Database Tables"

    # Check if we can connect
    if ! docker exec lambrk-postgres psql -U $DB_USER -d $DB_NAME -c "SELECT 1" &> /dev/null; then
        fail "Cannot connect to database"
        ERRORS=$((ERRORS + 1))
        return
    fi
    success "Database connection OK"

    # Get existing tables
    EXISTING_TABLES=$(docker exec lambrk-postgres psql -U $DB_USER -d $DB_NAME -t -c \
        "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;" 2>/dev/null | tr -d ' ')

    MISSING_TABLES=()
    for table in "${EXPECTED_TABLES[@]}"; do
        if echo "$EXISTING_TABLES" | grep -q "^${table}$"; then
            TABLE_COUNT=$(docker exec lambrk-postgres psql -U $DB_USER -d $DB_NAME -t -c \
                "SELECT COUNT(*) FROM $table;" 2>/dev/null | tr -d ' ')
            success "Table '$table' exists ($TABLE_COUNT rows)"
        else
            MISSING_TABLES+=("$table")
            fail "Table '$table' is MISSING"
        fi
    done

    if [ ${#MISSING_TABLES[@]} -gt 0 ]; then
        warn "Missing tables detected. Flyway migrations will create them on app start."
        warn "Or run: ./mvnw flyway:migrate"
    else
        success "All expected tables exist"
    fi
}

# ============================================================
# 6. Build Project
# ============================================================
build_project() {
    header "6. Building Project"

    cd "$PROJECT_DIR"

    MVN_CMD="mvn"
    if [ -f "$PROJECT_DIR/mvnw" ]; then
        MVN_CMD="./mvnw"
        chmod +x "$PROJECT_DIR/mvnw"
    fi

    log "Running Maven build (skipping tests)..."
    if $MVN_CMD clean compile -DskipTests -q 2>&1; then
        success "Build successful"
    else
        warn "Build had issues (may be due to Java version). App may still run."
    fi
}

# ============================================================
# 7. Summary
# ============================================================
print_summary() {
    header "Setup Summary"

    if [ $ERRORS -eq 0 ]; then
        echo -e "${GREEN}${BOLD}  All checks passed! Ready to run.${NC}"
    else
        echo -e "${YELLOW}${BOLD}  Setup completed with $ERRORS issue(s).${NC}"
    fi

    echo ""
    echo -e "  ${BOLD}Service URLs:${NC}"
    echo -e "    App:        http://localhost:$APP_PORT"
    echo -e "    Swagger:    http://localhost:$APP_PORT/swagger-ui.html"
    echo -e "    Actuator:   http://localhost:$APP_PORT/actuator/health"
    echo -e "    Grafana:    http://localhost:$GRAFANA_PORT (admin/admin)"
    echo -e "    Prometheus: http://localhost:$PROMETHEUS_PORT"
    echo -e "    Zipkin:     http://localhost:$ZIPKIN_PORT"
    echo ""
    echo -e "  ${BOLD}Next steps:${NC}"
    echo -e "    Run the app:  ${CYAN}./run.sh${NC}"
    echo -e "    Stop the app: ${CYAN}./stop.sh${NC}"
    echo ""
    echo -e "  Log file: $LOG_FILE"
}

# ============================================================
# Main
# ============================================================
main() {
    echo "" > "$LOG_FILE"
    echo -e "${BOLD}"
    echo "  ╔══════════════════════════════════════════╗"
    echo "  ║       Lambrk Backend - Setup             ║"
    echo "  ╚══════════════════════════════════════════╝"
    echo -e "${NC}"

    check_dependencies

    if check_docker; then
        check_and_kill_ports
        start_services
        check_database_tables
    fi

    build_project
    print_summary
}

main "$@"
