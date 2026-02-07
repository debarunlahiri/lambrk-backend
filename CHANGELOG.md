# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-02-07

### Added

#### Core Features
- **User Management**: Registration, authentication, JWT tokens, refresh tokens, role-based access (USER, MODERATOR, ADMIN)
- **Posts**: Create, read, update, delete posts with rich text support
- **Comments**: Nested comment threads with depth tracking
- **Voting**: Upvote/downvote system with score calculation
- **Subreddits**: Community creation, subscription, moderation

#### Advanced Search
- Full-text search across posts, comments, users, subreddits
- Advanced filtering (time, subreddit, flair, score thresholds)
- Multiple sorting options (relevance, hot, new, top, controversial)
- Auto-generated search suggestions
- Structured concurrency for parallel multi-type search

#### Real-Time Features
- WebSocket support with STOMP protocol
- Real-time notifications for replies, upvotes, mentions
- Live post and comment updates
- Connection management and event tracking

#### Notifications
- Multi-type notification system (comment replies, upvotes, mentions, admin actions)
- WebSocket + Kafka real-time delivery
- Email notifications via Spring Mail + Thymeleaf
- Read/unread status management

#### Admin & Moderation
- Admin operations: ban, suspend, delete, lock, quarantine
- Audit trail with `admin_actions` table
- AI-powered content moderation with Spring AI
- Soft delete functionality for posts and comments

#### ML Recommendations
- Personalized content recommendations using Spring AI
- Post, subreddit, user, and comment recommendations
- Confidence scoring with explanation factors
- Context-aware suggestions

#### File Management
- Multi-type file upload support (images, videos, avatars)
- SHA-256 checksums for file integrity
- Public/private visibility controls
- MIME type validation and size limits

#### Observability
- Micrometer metrics with Prometheus export
- OpenTelemetry tracing with OTLP export
- Custom business metrics
- Structured logging with correlation IDs
- Health checks (liveness, readiness)

#### Resilience
- Circuit breakers (Resilience4j) for all external services
- Rate limiting per endpoint type
- Retry policies with exponential backoff
- Bulkhead isolation for concurrent requests
- Time limiters for timeouts

#### Caching
- Caffeine L1 cache (in-process)
- Redis L2 cache (distributed)
- Multi-level cache strategy
- Cache eviction on writes

#### Security
- JWT authentication with HS512
- Role-based access control
- BCrypt password hashing
- CORS configuration
- Method-level security with @PreAuthorize
- Input validation

#### Infrastructure
- Kubernetes deployment manifests
- GitHub Actions CI/CD pipeline
- Docker support (JVM and native image)
- Prometheus alerting rules
- Flyway database migrations

#### Documentation
- 14 comprehensive API documentation files
- Architecture documentation
- Security policy
- This changelog

### Technical Highlights

- **Java 25** with preview features (virtual threads, structured concurrency)
- **Spring Boot 3.5** with all latest features
- **Record-based JPA entities** for immutable domain models
- **Structured concurrency** using `StructuredTaskScope`
- **GraalVM native image** support with reflection configuration
- **Testcontainers** for integration testing
- **OpenAPI/Swagger** documentation

### Database Schema

- 8 domain tables: users, subreddits, posts, comments, votes, notifications, admin_actions, file_uploads
- Proper indexing for performance
- Audit columns (created_at, updated_at)
- Soft delete support

### API Endpoints

- `/api/auth/*` - Authentication
- `/api/posts/*` - Post management
- `/api/comments/*` - Comment management
- `/api/subreddits/*` - Community management
- `/api/votes/*` - Voting
- `/api/users/*` - User profiles
- `/api/search/*` - Advanced search
- `/api/notifications/*` - Notifications
- `/api/admin/*` - Admin operations
- `/api/recommendations/*` - ML recommendations
- `/api/files/*` - File uploads
- `/actuator/*` - Health and metrics
- `/ws` - WebSocket endpoint

### Metrics

All endpoints emit Micrometer metrics:
- HTTP request counts and durations
- Custom business metrics (posts created, votes cast, etc.)
- JVM metrics
- Database connection pool metrics
- Cache metrics
- Kafka metrics
- Resilience4j metrics

### Security Headers

- Strict-Transport-Security
- X-Content-Type-Options
- X-Frame-Options
- X-XSS-Protection
- Content-Security-Policy
- Referrer-Policy

### Rate Limits

- Authentication: 10/min
- Post creation: 100/min
- Comment creation: 500/min
- Search: 50/min
- Voting: 1000/min

### Dependencies

- Spring Boot 3.5.0
- Spring Security 6
- Spring Cloud Stream (Kafka)
- Spring AI 1.0.0-M2
- Spring Modulith
- Resilience4j 2.1.0
- Micrometer / OpenTelemetry
- PostgreSQL 16
- Redis 7
- Kafka 3.7
- GraalVM 0.10.3

## [0.9.0] - 2026-01-15

### Added
- Initial project setup
- Basic CRUD operations
- JPA entities and repositories
- Spring Security configuration
- Docker Compose setup

## Future Roadmap

### [1.1.0] - Planned
- Elasticsearch integration for search
- GraphQL API endpoint
- Mobile push notifications (Firebase)
- Advanced analytics dashboard
- Machine learning for content ranking

### [1.2.0] - Planned
- Multi-region deployment support
- CDN integration for static assets
- Video streaming support
- Live chat rooms
- Polls and surveys

### [2.0.0] - Planned
- Microservices architecture migration
- Event sourcing with CQRS
- Graph database for relationships
- Federated identity (SSO)
- Blockchain-based content verification

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring Team for the amazing Spring Boot ecosystem
- Java Team for Java 25 features
- Open source community for all the libraries used
