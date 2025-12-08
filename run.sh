#!/bin/bash

# Lambrk Backend Startup Script
# This script checks database and tables before starting services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Database configuration
DB_HOST="${POSTGRES_HOST:-localhost}"
DB_PORT="${POSTGRES_PORT:-5432}"
DB_USER="${POSTGRES_USER:-lambrk_user}"
DB_PASSWORD="${POSTGRES_PASSWORD:-lambrk_password}"
DB_NAME="${POSTGRES_DB:-lambrk}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Lambrk Backend Startup Script${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Function to check if PostgreSQL is accessible
check_postgres_connection() {
    echo -e "${YELLOW}Checking PostgreSQL connection...${NC}"
    
    if command -v psql &> /dev/null; then
        export PGPASSWORD="$DB_PASSWORD"
        if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -c '\q' 2>/dev/null; then
            echo -e "${GREEN}✓ PostgreSQL connection successful${NC}"
            return 0
        else
            echo -e "${RED}✗ Cannot connect to PostgreSQL${NC}"
            echo -e "${YELLOW}Please ensure PostgreSQL is running and credentials are correct${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}psql not found. Using Node.js to check connection...${NC}"
        RESULT=$(node scripts/check-db.js check-connection 2>/dev/null)
        if echo "$RESULT" | grep -q '"success":true'; then
            echo -e "${GREEN}✓ PostgreSQL connection successful${NC}"
            return 0
        else
            echo -e "${RED}✗ Cannot connect to PostgreSQL${NC}"
            echo -e "${YELLOW}Please ensure PostgreSQL is running and credentials are correct${NC}"
            return 1
        fi
    fi
}

# Function to check if database exists
check_database_exists() {
    echo -e "${YELLOW}Checking if database '$DB_NAME' exists...${NC}"
    
    if command -v psql &> /dev/null; then
        export PGPASSWORD="$DB_PASSWORD"
        DB_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>/dev/null || echo "")
        
        if [ "$DB_EXISTS" = "1" ]; then
            echo -e "${GREEN}✓ Database '$DB_NAME' exists${NC}"
            return 0
        else
            echo -e "${YELLOW}Database '$DB_NAME' does not exist. Creating...${NC}"
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -c "CREATE DATABASE $DB_NAME;" 2>/dev/null
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✓ Database '$DB_NAME' created successfully${NC}"
                return 0
            else
                echo -e "${RED}✗ Failed to create database '$DB_NAME'${NC}"
                return 1
            fi
        fi
    else
        # Use Node.js script
        RESULT=$(node scripts/check-db.js check-database 2>/dev/null)
        if echo "$RESULT" | grep -q '"exists":true'; then
            echo -e "${GREEN}✓ Database '$DB_NAME' exists${NC}"
            return 0
        else
            echo -e "${YELLOW}Database '$DB_NAME' does not exist. Creating...${NC}"
            CREATE_RESULT=$(node scripts/check-db.js create-database 2>/dev/null)
            if echo "$CREATE_RESULT" | grep -q '"success":true'; then
                echo -e "${GREEN}✓ Database '$DB_NAME' created successfully${NC}"
                return 0
            else
                echo -e "${RED}✗ Failed to create database '$DB_NAME'${NC}"
                return 1
            fi
        fi
    fi
}

# Function to check required tables
check_required_tables() {
    echo -e "${YELLOW}Checking for required tables...${NC}"
    
    RESULT=$(node scripts/check-db.js check-tables 2>/dev/null)
    
    if echo "$RESULT" | grep -q '"allExist":true'; then
        # All tables exist, print them
        if command -v psql &> /dev/null; then
            export PGPASSWORD="$DB_PASSWORD"
            for table in users videos; do
                echo -e "${GREEN}✓ Table '$table' exists${NC}"
            done
        else
            echo -e "${GREEN}✓ All required tables exist${NC}"
        fi
        return 0
    else
        # Some tables are missing
        if command -v psql &> /dev/null; then
            export PGPASSWORD="$DB_PASSWORD"
            for table in users videos; do
                TABLE_EXISTS=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='$table';" 2>/dev/null || echo "")
                if [ "$TABLE_EXISTS" = "1" ]; then
                    echo -e "${GREEN}✓ Table '$table' exists${NC}"
                else
                    echo -e "${YELLOW}✗ Table '$table' does not exist${NC}"
                fi
            done
        else
            MISSING=$(echo "$RESULT" | grep -o '"missingTables":\[.*\]' | grep -o '"[^"]*"' | tr -d '"' | tr '\n' ' ')
            echo -e "${YELLOW}Missing tables: $MISSING${NC}"
        fi
        echo -e "${YELLOW}Some tables are missing. Running migrations...${NC}"
        return 1
    fi
}

# Function to run migrations
run_migrations() {
    echo -e "${YELLOW}Running database migrations...${NC}"
    
    # Build shared package first
    if [ -d "shared" ]; then
        echo -e "${YELLOW}Building shared package...${NC}"
        cd shared
        npm run build
        cd ..
    fi
    
    # Run migrations using Node.js
    if [ -f "migrations/run_all_migrations.ts" ]; then
        echo -e "${YELLOW}Executing migration script...${NC}"
        npx ts-node migrations/run_all_migrations.ts
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ Migrations completed successfully${NC}"
            return 0
        else
            echo -e "${RED}✗ Migration failed${NC}"
            return 1
        fi
    else
        echo -e "${RED}✗ Migration script not found${NC}"
        return 1
    fi
}

# Function to check dependencies
check_dependencies() {
    echo -e "${YELLOW}Checking dependencies...${NC}"
    
    local missing_deps=0
    local packages=("shared" "services/auth-service" "services/video-service" "gateway")
    
    for package in "${packages[@]}"; do
        if [ -d "$package" ]; then
            if [ ! -d "$package/node_modules" ]; then
                echo -e "${YELLOW}✗ Dependencies missing in $package${NC}"
                missing_deps=1
            else
                echo -e "${GREEN}✓ Dependencies installed in $package${NC}"
            fi
        fi
    done
    
    # Check root node_modules
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}✗ Root dependencies missing${NC}"
        missing_deps=1
    else
        echo -e "${GREEN}✓ Root dependencies installed${NC}"
    fi
    
    if [ $missing_deps -eq 1 ]; then
        echo -e "${YELLOW}Some dependencies are missing. Installing...${NC}"
        echo -e "${YELLOW}This may take a few minutes...${NC}"
        npm run install:all
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ All dependencies installed successfully${NC}"
            return 0
        else
            echo -e "${RED}✗ Failed to install dependencies${NC}"
            return 1
        fi
    else
        echo -e "${GREEN}✓ All dependencies are installed${NC}"
        return 0
    fi
}

# Function to start services
start_services() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Starting Lambrk Services${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    # Start all services
    echo -e "${GREEN}Starting all services...${NC}"
    npm run dev:all
}

# Function to check and kill processes on ports
check_ports() {
    echo -e "${YELLOW}Checking for processes on service ports...${NC}"
    
    local ports=(3100 3101 3102)
    local ports_in_use=0
    
    for port in "${ports[@]}"; do
        local pid=$(lsof -ti:$port 2>/dev/null || echo "")
        if [ -n "$pid" ]; then
            echo -e "${YELLOW}Port $port is in use (PID: $pid). Attempting to stop...${NC}"
            kill -TERM $pid 2>/dev/null || kill -KILL $pid 2>/dev/null
            sleep 1
            local still_running=$(lsof -ti:$port 2>/dev/null || echo "")
            if [ -n "$still_running" ]; then
                echo -e "${RED}✗ Failed to free port $port${NC}"
                ports_in_use=1
            else
                echo -e "${GREEN}✓ Port $port freed${NC}"
            fi
        fi
    done
    
    if [ $ports_in_use -eq 1 ]; then
        echo -e "${YELLOW}Some ports are still in use. Please run './stop.sh' manually and try again.${NC}"
        return 1
    fi
    
    return 0
}

# Main execution
main() {
    # Step 0: Check and free ports if needed
    if ! check_ports; then
        echo -e "${RED}Port check failed. Exiting...${NC}"
        exit 1
    fi
    
    # Step 1: Check PostgreSQL connection
    if ! check_postgres_connection; then
        echo -e "${RED}Failed to connect to PostgreSQL. Exiting...${NC}"
        exit 1
    fi
    
    # Step 2: Check if database exists, create if not
    if ! check_database_exists; then
        echo -e "${RED}Database check failed. Exiting...${NC}"
        exit 1
    fi
    
    # Step 3: Check for required tables
    if ! check_required_tables; then
        # Tables are missing, run migrations
        if ! run_migrations; then
            echo -e "${RED}Migration failed. Exiting...${NC}"
            exit 1
        fi
        
        # Verify tables were created
        echo -e "${YELLOW}Verifying tables were created...${NC}"
        if ! check_required_tables; then
            echo -e "${RED}Tables verification failed. Exiting...${NC}"
            exit 1
        fi
    fi
    
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Database check completed successfully!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    # Step 4: Check dependencies
    if ! check_dependencies; then
        echo -e "${RED}Dependency check failed. Exiting...${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}All checks completed successfully!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    
    # Step 5: Start services
    start_services
}

# Run main function
main

