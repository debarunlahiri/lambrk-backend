#!/bin/bash

# Setup script to ensure lambrk database has all tables

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Setting up lambrk database...${NC}\n"

# Set environment variables for lambrk database
export POSTGRES_DB=lambrk
export POSTGRES_USER=${POSTGRES_USER:-${USER}}
export POSTGRES_HOST=${POSTGRES_HOST:-localhost}
export POSTGRES_PORT=${POSTGRES_PORT:-5432}

echo "Database: $POSTGRES_DB"
echo "User: $POSTGRES_USER"
echo "Host: $POSTGRES_HOST:$POSTGRES_PORT"
echo ""

# Check if tables exist
echo -e "${YELLOW}Checking tables...${NC}"
node scripts/check-lambrk-db.js

if [ $? -ne 0 ]; then
    echo -e "\n${YELLOW}Running migrations...${NC}"
    npm run migrate
    echo -e "\n${YELLOW}Verifying tables...${NC}"
    node scripts/check-lambrk-db.js
fi

echo -e "\n${GREEN}Setup complete!${NC}"
echo -e "\nConnection string for your DB client:"
echo -e "jdbc:postgresql://${POSTGRES_HOST}:${POSTGRES_PORT}/${POSTGRES_DB}"
echo -e "User: ${POSTGRES_USER}"

