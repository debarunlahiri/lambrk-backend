#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
DB_USER="${DB_USER:-debarunlahiri}"
DB_NAME="${DB_NAME:-lambrk}"
MIG_DIR="$PROJECT_DIR/src/main/resources/db/migration"

# Check if Flyway is already managing this database
FLYWAY_EXISTS=$(docker exec lambrk-postgres psql -U "$DB_USER" -d "$DB_NAME" -t -c \
    "SELECT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'flyway_schema_history');" 2>/dev/null | tr -d ' ' || true)

if [ "$FLYWAY_EXISTS" = "t" ]; then
    echo "Database is managed by Flyway. Use: ./mvnw flyway:migrate"
    exit 0
fi

# Check for stale pre-rename tables
STALE_TABLES=("sublambrks" "user_sublambrk_memberships" "user_sublambrk_moderators")
for stale in "${STALE_TABLES[@]}"; do
    EXISTS=$(docker exec lambrk-postgres psql -U "$DB_USER" -d "$DB_NAME" -t -c \
        "SELECT EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = '$stale');" 2>/dev/null | tr -d ' ' || true)
    if [ "$EXISTS" = "t" ]; then
        echo "ERROR: Database has stale table '$stale'. Please reset the database first:"
        echo "  docker exec lambrk-postgres psql -U $DB_USER -d postgres -c 'DROP DATABASE IF EXISTS $DB_NAME; CREATE DATABASE $DB_NAME;'"
        exit 1
    fi
done

if [ ! -d "$MIG_DIR" ]; then
  echo "Migration directory not found: $MIG_DIR"
  exit 1
fi

for f in $(ls "$MIG_DIR"/V*.sql 2>/dev/null | sort -V); do
  echo "Applying $(basename "$f")"
  docker exec -i lambrk-postgres psql -v ON_ERROR_STOP=1 -U "$DB_USER" -d "$DB_NAME" < "$f"
done

echo "DB bootstrap completed"
