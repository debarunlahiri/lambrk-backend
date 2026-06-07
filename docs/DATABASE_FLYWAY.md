# Database & Flyway Commands

## Build & Run

```bash
# Build the project
mvn clean compile -DskipTests -q

# Build with tests
mvn clean compile -q

# Start the app (prod profile, PostgreSQL)
mvn spring-boot:run -DskipTests

# Start in background
nohup mvn spring-boot:run -DskipTests -q > /tmp/lambrk-app.log 2>&1 &

# Stop the app
pkill -f "spring-boot:run"

# Check if app is running
curl -s http://localhost:9500/actuator/health

# Run with dev profile (H2 file DB)
mvn spring-boot:run -DskipTests -Dspring.profiles.active=dev

# Run on a different port
mvn spring-boot:run -DskipTests -Dspring-boot.run.arguments="--server.port=9501"
```

## Quick Fix Flow (when things break)

```bash
# 1. Stop the app
pkill -f "spring-boot:run"

# 2. Clean rebuild
mvn clean compile -DskipTests -q

# 3. Fix Flyway checksums if needed
mvn flyway:repair -q

# 4. Apply pending migrations
mvn flyway:migrate -q

# 5. Start the app
nohup mvn spring-boot:run -DskipTests -q > /tmp/lambrk-app.log 2>&1 &

# 6. Check health
sleep 15 && curl -s http://localhost:9500/actuator/health
```

## Flyway Migration Commands

```bash
# Apply pending migrations
mvn flyway:migrate -q

# Validate migrations (check checksums match DB)
mvn flyway:validate -q

# Repair checksums after editing an already-applied migration
mvn flyway:repair -q

# Check migration status (which are applied/pending)
mvn flyway:info -q

# Undo the last applied migration (undo must be enabled in config)
mvn flyway:undo -q

# Clean the database (drops all tables — DANGER in production!)
mvn flyway:clean -q

# Baseline an existing database at a specific version
mvn flyway:baseline -q -Dflyway.baselineVersion=5
```

## Common Troubleshooting

```bash
# Checksum mismatch after editing a migration
mvn flyway:repair -q

# Missing tables — apply all pending migrations
mvn flyway:migrate -q

# Schema validation failing on app startup
mvn flyway:validate -q

# Reset database completely (Docker PostgreSQL)
docker exec lambrk-postgres psql -U debarunlahiri -d postgres -c "DROP DATABASE IF EXISTS lambrk;"
docker exec lambrk-postgres psql -U debarunlahiri -d postgres -c "CREATE DATABASE lambrk;"
mvn flyway:migrate -q

# See what migrations are pending
mvn flyway:info -q | grep -i pending
```

## Running SQL Manually (via Docker)

```bash
# Connect to PostgreSQL shell
docker exec -it lambrk-postgres psql -U debarunlahiri -d lambrk

# List all tables
\dt

# Describe a table
\d users

# Run a query
SELECT * FROM categories ORDER BY sort_order;

# Run a SQL file
docker exec -i lambrk-postgres psql -U debarunlahiri -d lambrk < some_file.sql
```

## Existing Migrations

| Version | Description                                                     |
| ------- | --------------------------------------------------------------- |
| V1      | Initial tables (users, communities, posts, comments, votes)     |
| V2      | Add `is_removed` to posts                                       |
| V3      | Add notifications, admin_actions, file_uploads, missing columns |
| V4      | Free tier usage table                                           |
| V5      | API logs table                                                  |
| V6      | Event publication table (Spring Modulith)                       |
| V7      | Categories + community_categories + seed data                   |
| V8      | Make post title nullable                                        |
| V9      | Rename upvote/downvote → like/dislike columns                   |
| V10     | Add completion_date to event_publication                        |
| V11     | Fix event_publication schema for Spring Modulith                |
| V12     | Make post community_id nullable                                 |
