# Architecture Overview

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Layer                             │
│              (Web / Mobile / API consumers)                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTPS + JWT
┌──────────────────────────▼──────────────────────────────────────┐
│                    API Gateway / Load Balancer                   │
│                  (CORS, Rate Limiting, TLS)                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                  Spring Boot Application                        │
│                                                                 │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐       │
│  │  Auth    │  │  Post    │  │ Comment  │  │Subreddit │       │
│  │Controller│  │Controller│  │Controller│  │Controller│       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │             │
│  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐  ┌────▼─────┐       │
│  │  Auth   │  │  Post    │  │ Comment  │  │Subreddit │       │
│  │ Service │  │ Service  │  │ Service  │  │ Service  │       │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘       │
│       │              │              │              │             │
│  ┌────▼──────────────▼──────────────▼──────────────▼─────┐      │
│  │              JPA Repositories (Spring Data)           │      │
│  └───────────────────────┬───────────────────────────────┘      │
│                          │                                      │
│  ┌───────────────────────▼───────────────────────────────┐      │
│  │  Cross-Cutting Concerns                               │      │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐  │      │
│  │  │Security  │ │Resilience│ │Caching   │ │Observ.  │  │      │
│  │  │(JWT/RBAC)│ │(R4j)    │ │(Caffeine)│ │(OTel)   │  │      │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────┘  │      │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐              │      │
│  │  │AI Moder. │ │Kafka Evt │ │Exception │              │      │
│  │  │(Spring AI)│ │(Stream) │ │Handler   │              │      │
│  │  └──────────┘ └──────────┘ └──────────┘              │      │
│  └───────────────────────────────────────────────────────┘      │
└──────────────────────────┬──────────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
┌────────▼───┐   ┌────────▼───┐   ┌────────▼───┐
│ PostgreSQL │   │   Redis    │   │   Kafka    │
│  (Primary) │   │  (Cache)   │   │ (Events)   │
└────────────┘   └────────────┘   └────────────┘
         │
         │                 ┌──────────────┐
         │                 │              │
┌────────▼───┐    ┌────────▼───┐  ┌────▼──────┐
│   AWS S3   │    │   Local    │  │  MinIO    │
│  (Files)   │    │  (Fallback)│  │ (Local S3)│
└────────────┘    └────────────┘  └───────────┘
```

---

## Package Structure

```
com.example.reddit
├── LambrkBackendApplication.java       # @SpringBootApplication + @Modulith
├── config/
│   ├── SecurityConfig.java             # Spring Security 6 filter chain
│   ├── JwtAuthenticationFilter.java    # JWT extraction + validation filter
│   ├── JwtTokenProvider.java           # Token generation, validation, refresh
│   ├── UserDetailsService.java         # Loads user from DB for auth
│   ├── JpaConfig.java                  # JPA auditing, Hibernate tuning
│   ├── VirtualThreadConfig.java        # Virtual thread executors
│   ├── CacheConfig.java               # Caffeine L1 + Redis L2
│   ├── ObservabilityConfig.java        # Micrometer, OTel, custom health
│   ├── ResilienceConfig.java           # CB, Retry, RateLimiter, Bulkhead
│   ├── NativeImageConfig.java          # GraalVM AOT reflection hints
│   └── S3Config.java                   # AWS S3 client configuration
├── domain/
│   ├── User.java                       # JPA record entity
│   ├── Post.java                       # JPA record entity + PostType enum
│   ├── Comment.java                    # JPA record entity (tree structure)
│   ├── Subreddit.java                  # JPA record entity
│   ├── Vote.java                       # JPA record entity + VoteType enum
│   ├── FileUpload.java                 # JPA record entity for file metadata
│   └── FreeTierUsage.java              # JPA record entity for usage tracking
├── dto/
│   ├── AuthRequest.java                # Login request
│   ├── AuthResponse.java               # Token pair + user
│   ├── RegisterRequest.java            # Registration request
│   ├── PostCreateRequest.java          # Post creation/update
│   ├── PostResponse.java               # Post API response
│   ├── CommentCreateRequest.java       # Comment creation
│   ├── CommentResponse.java            # Comment API response
│   ├── SubredditCreateRequest.java     # Subreddit creation/update
│   ├── SubredditResponse.java          # Subreddit API response
│   ├── UserResponse.java               # User API response (no email/password)
│   ├── VoteRequest.java                # Vote creation
│   ├── FileUploadRequest.java          # File upload request
│   └── FileUploadResponse.java         # File upload response
├── repository/
│   ├── UserRepository.java             # JPA + custom queries
│   ├── PostRepository.java             # JPA + custom queries
│   ├── CommentRepository.java          # JPA + custom queries
│   ├── SubredditRepository.java        # JPA + custom queries
│   ├── VoteRepository.java             # JPA + custom queries
│   ├── FileUploadRepository.java       # File metadata queries
│   └── FreeTierUsageRepository.java    # Free tier usage queries
├── service/
│   ├── PostService.java                # Business logic + structured concurrency
│   ├── CommentService.java             # Comment CRUD + threading
│   ├── VoteService.java                # Toggle voting logic
│   ├── SubredditService.java           # Community management
│   ├── AuthService.java                # Registration + login + refresh
│   ├── KafkaEventService.java          # Event publishing via StreamBridge
│   ├── AIContentModerationService.java # Spring AI moderation + recommendations
│   ├── ContentModerationAspect.java    # AOP aspect for @ModerateContent
│   ├── ContentModerationException.java # Moderation violation exception
│   ├── ModerateContent.java            # Custom annotation
│   ├── CustomMetrics.java              # Business metric counters
│   ├── FileUploadService.java          # File upload business logic
│   ├── S3StorageService.java           # AWS S3 operations
│   └── FreeTierLimitService.java       # Free tier limit enforcement
├── controller/
│   ├── AuthController.java             # /api/auth/*
│   ├── PostController.java             # /api/posts/*
│   ├── CommentController.java          # /api/comments/*
│   ├── SubredditController.java        # /api/subreddits/*
│   ├── VoteController.java             # /api/votes/*
│   ├── UserController.java             # /api/users/*
│   └── FileUploadController.java       # /api/files/*
└── exception/
    ├── GlobalExceptionHandler.java     # @RestControllerAdvice (RFC 7807)
    ├── ResourceNotFoundException.java
    ├── DuplicateResourceException.java
    ├── UnauthorizedActionException.java
    └── FreeTierLimitExceededException.java
```

---

## Request Flow

```
HTTP Request
  │
  ▼
JwtAuthenticationFilter          ← extracts JWT, sets SecurityContext
  │
  ▼
Spring Security Filter Chain     ← RBAC, CORS, CSRF disabled
  │
  ▼
Controller (@Timed, @NewSpan)    ← observability annotations
  │
  ▼
Service Layer                    ← @CircuitBreaker, @RateLimiter, @Retry, @Bulkhead
  │                              ← @ModerateContent (AOP → AI moderation)
  │                              ← @Cacheable / @CacheEvict
  │                              ← @Transactional
  ▼
Repository (Spring Data JPA)     ← virtual-thread-friendly connection pool
  │
  ▼
PostgreSQL                       ← Flyway-managed schema
  │
  ▼
Side Effects:
  ├── Kafka event published (async via StreamBridge)
  ├── Cache updated (Caffeine L1 eviction)
  ├── Metrics recorded (Micrometer counters/timers)
  └── Trace span completed (OpenTelemetry)
```

---

## Key Design Decisions

### Virtual Threads Everywhere
- `spring.threads.virtual.enabled=true` — Tomcat uses virtual threads for request handling
- `Executors.newVirtualThreadPerTaskExecutor()` for async operations
- Avoids `synchronized` blocks to prevent carrier thread pinning
- HikariCP pool sized for virtual threads (not 1:1 with platform threads)

### Structured Concurrency in Services
- `StructuredTaskScope.ShutdownOnFailure` for parallel DB lookups
- Clean cancellation when any subtask fails
- Used in `PostService.createPost()` to fetch User + Subreddit concurrently

### Record-Based Domain Model
- JPA entities as Java records (immutable by default)
- Custom canonical constructors for defaults
- Convenience constructors for common creation patterns

### Multi-Level Caching
- **L1 (Caffeine)**: In-process, 5-minute TTL, 10K max entries
- **L2 (Redis)**: Distributed, 10-minute TTL, JSON serialization
- Cache eviction on writes; read-through on misses

### Event-Driven Side Effects
- Kafka events for post/comment/vote actions
- Spring Cloud Stream with StreamBridge for decoupled publishing
- Events are fire-and-forget with circuit breaker protection

### AI Content Moderation
- AOP-based via `@ModerateContent` annotation
- Parallel analysis: moderation + toxicity + spam (structured concurrency)
- Results cached to avoid duplicate API calls
- Graceful degradation if AI service is unavailable

### S3 Storage Architecture
- AWS S3 as primary file storage with configurable fallback to local filesystem
- Files organized by type: `avatars/`, `posts/images/`, `posts/videos/`, `subreddits/icons/`, etc.
- Presigned URLs for secure temporary access to private files
- Circuit breaker and retry patterns for S3 operations (Resilience4j)
- Support for MinIO and other S3-compatible services via custom endpoint
- Checksum verification (SHA-256) for data integrity

### Free Tier System
- Monthly usage tracking per user (storage, uploads, bandwidth)
- Configurable limits: 5GB storage, 100 uploads/month, 100GB bandwidth/month
- Usage stored in `free_tier_usage` table with year/month partitioning
- Automatic enforcement at upload time with clear error messages
- Usage reset at the beginning of each calendar month
- `FreeTierLimitExceededException` for graceful handling of limit violations

---

## Database Schema

See `src/main/resources/db/migration/V1__Create_initial_tables.sql` for the full schema.

### Entity Relationships

```
User 1──* Post
User 1──* Comment
User 1──* Vote
User 1──* FileUpload
User 1──* FreeTierUsage
User *──* Subreddit (membership)
User *──* Subreddit (moderation)
Subreddit 1──* Post
Post 1──* Comment
Post 1──* Vote
Post *──* FileUpload (post attachments)
Comment 1──* Comment (self-referencing tree)
Comment 1──* Vote
```

### Indexes

Every foreign key and frequently-queried column is indexed. Composite indexes exist for:
- `votes(user_id, post_id)` — unique constraint + fast lookup
- `votes(user_id, comment_id)` — unique constraint + fast lookup
- `file_uploads(type, uploaded_by, created_at)` — file listing queries
- `free_tier_usage(user_id, period_year, period_month)` — unique constraint + usage lookup

---

## Security Model

### Authentication Flow

```
Register/Login → JWT access token (24h) + refresh token (7d)
                      │
                      ▼
              Authorization: Bearer <token>
                      │
                      ▼
              JwtAuthenticationFilter validates
                      │
                      ▼
              SecurityContext populated
                      │
                      ▼
              @PreAuthorize / method security
```

### Roles

| Role       | Permissions                                    |
|------------|------------------------------------------------|
| USER       | CRUD own posts/comments, vote, subscribe       |
| VERIFIED   | Same as USER (badge only)                      |
| MODERATOR  | Edit subreddit settings, remove content        |
| ADMIN      | All permissions, delete users, manage system   |
