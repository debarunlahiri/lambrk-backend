# Lambrk Backend — Spring Boot 3.5 + Java 25

Production-grade Lambrk-like backend showcasing every advanced topic a senior/lead Java engineer needs in 2026.

| Stack | Version |
|-------|---------|
| Java | 25 (preview features enabled) |
| Spring Boot | 3.5.0 |
| Spring Security | 6+ (JWT / OAuth2 Resource Server) |
| Spring Cloud Stream | 2024.0.0 (Kafka binder) |
| Spring AI | 1.0.0-M2 (OpenAI) |
| Spring Modulith | latest |
| Resilience4j | 2.1.0 |
| Micrometer / OTel | 2+ / OTLP exporter |
| Database | PostgreSQL 16 + Flyway |
| Cache | Caffeine (L1) + Redis 7 (L2) |
| Messaging | Apache Kafka 3.7 |
| WebSocket | STOMP + SockJS |
| Native | GraalVM AOT / native-image |
| Testing | JUnit 5, Testcontainers, jqwik |

---

## Quick Start

```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Run the app (dev profile, H2 in-memory)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="--enable-preview"

# 3. Or build a native image
mvn clean package -Pnative -DskipTests
./target/reddit-backend
```

The app starts on **http://localhost:8080**.

Default seed users (password for all: `password`):

| Username    | Role            |
|-------------|-----------------|
| admin       | ADMIN, MODERATOR|
| john_doe    | USER            |
| jane_smith  | USER            |

---

## API Documentation

Full endpoint docs live in the **`docs/api/`** folder:

| Document | Endpoints |
|----------|-----------|
| [AUTH_API.md](docs/api/AUTH_API.md) | `POST /api/auth/register`, `/login`, `/refresh` |
| [POSTS_API.md](docs/api/POSTS_API.md) | `GET/POST/PUT/DELETE /api/posts/*` |
| [COMMENTS_API.md](docs/api/COMMENTS_API.md) | `GET/POST/PUT/DELETE /api/comments/*` |
| [SUBREDDITS_API.md](docs/api/SUBREDDITS_API.md) | `GET/POST/PUT /api/subreddits/*` |
| [VOTES_API.md](docs/api/VOTES_API.md) | `POST /api/votes/post`, `/api/votes/comment` |
| [USERS_API.md](docs/api/USERS_API.md) | `GET/DELETE /api/users/*` |
| [SEARCH_API.md](docs/api/SEARCH_API.md) | `GET/POST /api/search/*` - Advanced search with filters |
| [NOTIFICATIONS_API.md](docs/api/NOTIFICATIONS_API.md) | `GET/POST/PUT/DELETE /api/notifications/*` |
| [ADMIN_API.md](docs/api/ADMIN_API.md) | `POST /api/admin/*` - Admin operations |
| [RECOMMENDATIONS_API.md](docs/api/RECOMMENDATIONS_API.md) | `GET/POST /api/recommendations/*` - ML recommendations |
| [FILES_API.md](docs/api/FILES_API.md) | `GET/POST/PUT/DELETE /api/files/*` - File uploads |
| [WEBSOCKET_API.md](docs/api/WEBSOCKET_API.md) | WebSocket real-time updates |
| [ERRORS_API.md](docs/api/ERRORS_API.md) | RFC 7807 error catalogue, rate limits, circuit breakers |
| [ACTUATOR_API.md](docs/api/ACTUATOR_API.md) | Health, Prometheus, tracing, custom metrics |

Additional architecture docs:

| Document | Contents |
|----------|----------|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | System diagram, package structure, request flow, design decisions |
| [KAFKA_EVENTS.md](docs/KAFKA_EVENTS.md) | Event schemas, topic list, consumer examples |

---

## Project Structure

```
src/main/java/com/example/reddit/
├── config/          SecurityConfig, JwtTokenProvider, JpaConfig, VirtualThreadConfig,
│                    CacheConfig, ObservabilityConfig, ResilienceConfig, NativeImageConfig
├── controller/      AuthController, PostController, CommentController, SubredditController,
│                    VoteController, UserController, SearchController, NotificationController,
│                    AdminController, RecommendationController, FileUploadController
├── service/         PostService, CommentService, VoteService, SubredditService, AuthService,
│                    SearchService, NotificationService, AdminService, RecommendationService,
│                    FileUploadService, KafkaEventService, AIContentModerationService,
│                    ContentModerationAspect, CustomMetrics
├── domain/          User, Post, Comment, Subreddit, Vote, Notification, AdminAction, FileUpload
├── dto/             Request/Response records for every endpoint
├── repository/      Spring Data JPA repositories with custom JPQL queries
├── exception/       GlobalExceptionHandler (RFC 7807), custom exceptions
└── websocket/       WebSocketConfig, WebSocketController, WebSocketEventListener

src/main/resources/
├── application.yml              Multi-profile config (dev / test / prod)
├── db/migration/                Flyway SQL migrations (V1, V2, V3)
└── META-INF/native-image/       GraalVM reflect-config.json + native-image.properties

docs/
├── api/                         14 API documentation files
├── ARCHITECTURE.md              System design & package map
└── KAFKA_EVENTS.md              Event schemas & consumer patterns

infra/
└── prometheus.yml               Prometheus scrape config
```

---

## Advanced Topics Covered

### Java 25 / JVM

| Topic | Where in code |
|-------|---------------|
| **Virtual Threads** | `application.yml` → `spring.threads.virtual.enabled: true`; `VirtualThreadConfig` |
| **Structured Concurrency** | `PostService.createPost()` — parallel User + Subreddit fetch via `StructuredTaskScope.ShutdownOnFailure` |
| **Structured Concurrency** | `AIContentModerationService.moderateContent()` — parallel moderation + toxicity + spam |
| **Record-based entities** | All domain classes are Java `record` types |
| **Pattern matching / switch** | Used in DTOs and service logic |
| **Preview features** | `--enable-preview` in compiler + surefire config |

### Spring Boot 3.5 / Ecosystem

| Topic | Where in code |
|-------|---------------|
| **Virtual Threads in Tomcat** | `spring.threads.virtual.enabled=true` |
| **Spring Security 6 + JWT** | `SecurityConfig`, `JwtTokenProvider`, `JwtAuthenticationFilter` |
| **OAuth2 Resource Server** | Configured in `SecurityConfig.securityFilterChain()` |
| **Method Security** | `@PreAuthorize("hasRole('ADMIN')")` on `UserController.deleteUser()`, `SubredditController.updateSubreddit()` |
| **Spring Data JPA** | 5 repositories with 60+ custom JPQL queries |
| **Spring Modulith** | `@Modulith` on main class |
| **Spring Cloud Stream + Kafka** | `KafkaEventService` publishes to 4 topics via `StreamBridge` |
| **Spring AI (OpenAI)** | `AIContentModerationService` — ChatClient, parallel analysis, caching |
| **Actuator deep customization** | Health groups (liveness/readiness), Prometheus, custom health indicators |
| **GraalVM Native Image** | `NativeImageConfig`, `reflect-config.json`, `native-image.properties`, Maven `native` profile |
| **AOT Processing** | `spring-boot-maven-plugin` → `process-aot` goal |

### Observability

| Topic | Where in code |
|-------|---------------|
| **Micrometer 2+ metrics** | `CustomMetrics`, `@Timed`, `@Counted` on every controller method |
| **OpenTelemetry tracing** | `ObservabilityConfig` — OTLP exporter, W3C propagation |
| **Custom spans** | `@NewSpan` + `@SpanTag` on all controller methods |
| **Structured logging** | Log pattern with `traceId` / `spanId` in `application.yml` |
| **Prometheus histograms** | SLA buckets + percentiles configured in `management.metrics` |

### Resilience

| Topic | Where in code |
|-------|---------------|
| **Circuit Breaker** | `ResilienceConfig` — 4 named instances; `@CircuitBreaker` on services |
| **Retry** | `ResilienceConfig` — exponential backoff for external APIs |
| **Rate Limiter** | 6 named limiters (post, comment, vote, registration, search, upload) |
| **Bulkhead** | 5 named bulkheads with concurrent call limits |
| **Time Limiter** | Per-service timeout beans |
| **Metrics integration** | All R4j instances bound to `MeterRegistry` |

### Caching

| Topic | Where in code |
|-------|---------------|
| **Caffeine L1** | `CacheConfig` — 5 named cache managers with different TTLs |
| **Redis L2** | `CacheConfig.redisCacheManager()` — JSON serialization, 10-min TTL |
| **Cache eviction** | `@CacheEvict` on every write operation |

### Security

| Topic | Where in code |
|-------|---------------|
| **JWT generation + validation** | `JwtTokenProvider` with HS512, metrics on errors |
| **Refresh tokens** | 7-day TTL, separate `tokenType` claim |
| **RBAC** | USER, VERIFIED, MODERATOR, ADMIN roles |
| **CORS** | Configured in `SecurityConfig.corsConfigurationSource()` |
| **BCrypt** | Strength 12 in `SecurityConfig.passwordEncoder()` |

### Error Handling

| Topic | Where in code |
|-------|---------------|
| **RFC 7807 ProblemDetail** | `GlobalExceptionHandler` — 10 exception handlers |
| **Validation errors** | Field-level error map in response |
| **Resilience errors** | Circuit breaker → 503, rate limit → 429, bulkhead → 429 |
| **Content moderation** | 422 with violation categories |

### Testing

| Topic | Where in code |
|-------|---------------|
| **Testcontainers** | `PostIntegrationTest`, `SearchIntegrationTest`, `NotificationIntegrationTest`, `AdminIntegrationTest` — PostgreSQL + Redis containers |
| **@WithMockUser** | Role-based test authentication |
| **MockMvc** | Full API integration tests |
| **jqwik** | Property-based testing dependency ready |

### Search & Discovery

| Topic | Where in code |
|-------|---------------|
| **Advanced Search** | `SearchService` — Full-text search across posts, comments, users, subreddits |
| **Structured Concurrency** | `search()` — Parallel multi-type search with `StructuredTaskScope` |
| **Filtering** | Time-based, subreddit, flair, score, comment count filters |
| **Sorting** | Relevance, new, hot, top, controversial |
| **Suggestions** | Auto-generated search suggestions |
| **Caching** | Search results cached with 5-minute TTL |

### Real-Time Features

| Topic | Where in code |
|-------|---------------|
| **WebSocket** | `WebSocketConfig` — STOMP over WebSocket with SockJS fallback |
| **Real-time Notifications** | `WebSocketController` — User-specific and public topics |
| **Event Broadcasting** | Kafka + WebSocket for live updates |
| **Connection Management** | `WebSocketEventListener` — Connection tracking |

### Notifications

| Topic | Where in code |
|-------|---------------|
| **Notification System** | `NotificationService` — Multi-type notifications |
| **Real-time Delivery** | WebSocket + Kafka for instant delivery |
| **Notification Types** | Comment replies, upvotes, mentions, admin actions |
| **Read Management** | Mark as read, bulk operations |

### Admin & Moderation

| Topic | Where in code |
|-------|---------------|
| **Admin Operations** | `AdminService` — Ban, suspend, delete, lock, quarantine |
| **Audit Trail** | `AdminAction` entity + `AdminActionRepository` |
| **Role-based Access** | `@PreAuthorize("hasRole('ADMIN')")` on all admin endpoints |
| **Soft Deletes** | `isRemoved` flag for posts and comments |
| **Content Moderation** | AI-powered with `@ModerateContent` aspect |

### ML Recommendations

| Topic | Where in code |
|-------|---------------|
| **Recommendation Engine** | `RecommendationService` — ML-based suggestions |
| **Spring AI Integration** | ChatClient for personalized explanations |
| **Multi-type Recommendations** | Posts, subreddits, users, comments |
| **Confidence Scoring** | 0-1 confidence with factor explanations |
| **Context-aware** | Subreddit/post context for relevant suggestions |

### File Management

| Topic | Where in code |
|-------|---------------|
| **File Upload** | `FileUploadService` — Multi-type file support |
| **File Types** | AVATAR, POST_IMAGE, POST_VIDEO, SUBREDDIT_ICON, BANNER |
| **Security** | SHA-256 checksums, MIME type validation |
| **Storage** | Local filesystem with configurable directory |
| **Access Control** | Public/private visibility with user permissions |

---

## Infrastructure

### Docker Compose

```bash
docker-compose up -d
```

Starts: **PostgreSQL**, **Redis**, **Kafka** + Zookeeper, **Prometheus**, **Grafana**, **Zipkin**

| Service    | Port  | URL                          |
|------------|-------|------------------------------|
| App        | 8080  | http://localhost:8080         |
| PostgreSQL | 5432  | `jdbc:postgresql://localhost:5432/lambrk_db` |
| Redis      | 6379  | `redis://localhost:6379`      |
| Kafka      | 9092  | `localhost:9092`              |
| Prometheus | 9090  | http://localhost:9090         |
| Grafana    | 3000  | http://localhost:3000 (admin/admin) |
| Zipkin     | 9411  | http://localhost:9411         |

### Environment Variables

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/lambrk_db
DATABASE_USERNAME=lambrk_user
DATABASE_PASSWORD=lambrk_password
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BROKERS=localhost:9092
OPENAI_API_KEY=your-openai-key
OTLP_ENDPOINT=http://localhost:4317
```

### Application Profiles

| Profile | Database | Cache | Kafka | Use case |
|---------|----------|-------|-------|----------|
| `dev`   | H2 in-memory | Caffeine | localhost:9092 | Local development |
| `test`  | H2 in-memory | Caffeine | — | Unit/integration tests |
| `prod`  | PostgreSQL | Caffeine + Redis | Kafka cluster | Production |

---

## Building

```bash
# Standard JAR
mvn clean package -DskipTests

# With tests
mvn clean verify

# GraalVM native image
mvn clean package -Pnative -DskipTests

# Docker image with native
mvn spring-boot:build-image -Pnative
```

---

## License

MIT
