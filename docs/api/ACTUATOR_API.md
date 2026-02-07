# Actuator & Observability Endpoints

Spring Boot Actuator endpoints for health checks, metrics, and monitoring.

---

## Health Checks

### GET `/actuator/health` (Public)

Overall application health.

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "redis": { "status": "UP" },
    "kafka": { "status": "UP" },
    "virtualThreadHealth": {
      "status": "UP",
      "details": { "liveThreads": 42, "virtualThreadsEnabled": true }
    },
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "postService": "CLOSED",
        "commentService": "CLOSED"
      }
    }
  }
}
```

### Health Groups

| Group      | Path                          | Includes       |
|------------|-------------------------------|----------------|
| Liveness   | `/actuator/health/liveness`   | ping           |
| Readiness  | `/actuator/health/readiness`  | db, redis, kafka |

Use these for Kubernetes probes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
```

---

## Metrics

### GET `/actuator/prometheus` (Public)

Prometheus-compatible metrics scrape endpoint.

#### Key Business Metrics

```
# Posts
lambrk_posts_created_total{sublambrk="programming"} 42
lambrk_comments_created_total{sublambrk="programming"} 128
lambrk_votes_cast_total{type="UPVOTE"} 1500
lambrk_votes_cast_total{type="DOWNVOTE"} 200

# Users
lambrk_users_registered_total 350
lambrk_users_login_total{userId="1"} 15

# Sublambrks
lambrk_sublambrks_created_total 12
lambrk_sublambrks_subscription_total{action="subscribe"} 500

# Content Moderation
lambrk_moderation_result_total{type="post",approved="true"} 95
lambrk_moderation_result_total{type="post",approved="false"} 5

# Search
lambrk_search_queries_total{type="sublambrk"} 200

# Errors
errors_total{type="not_found"} 50
errors_total{type="rate_limit"} 10
```

#### HTTP Metrics (auto-generated)

```
http_server_requests_seconds_count{method="GET",uri="/api/posts/{postId}",status="200"} 1000
http_server_requests_seconds_sum{method="GET",uri="/api/posts/{postId}",status="200"} 12.5
http_server_requests_seconds_bucket{method="GET",uri="/api/posts/{postId}",le="0.1"} 950
```

Histogram buckets: 10ms, 25ms, 50ms, 100ms, 200ms, 500ms, 1s, 2s, 5s  
Percentiles: p50, p90, p95, p99

#### JVM Metrics

```
jvm_memory_used_bytes{area="heap"} 150000000
jvm_threads_live_threads 42
jvm_gc_pause_seconds_count{cause="G1 Young Generation"} 10
process_cpu_usage 0.15
process_uptime_seconds 86400
```

#### Resilience4j Metrics

```
resilience4j_circuitbreaker_state{name="postService"} 0
resilience4j_retry_calls_total{name="postService",kind="successful_without_retry"} 500
resilience4j_ratelimiter_available_permissions{name="postCreation"} 95
resilience4j_bulkhead_available_concurrent_calls{name="postService"} 8
```

#### JWT Metrics

```
jwt_token_generation_seconds_count 200
jwt_token_validation_seconds_count 5000
jwt_validation_error_total{type="expired"} 15
```

---

## Info

### GET `/actuator/info` (Public)

Build and git information.

```json
{
  "build": {
    "artifact": "lambrk-backend",
    "name": "lambrk-backend",
    "version": "1.0.0"
  },
  "git": {
    "branch": "main",
    "commit": { "id": "abc1234", "time": "2026-02-07T12:00:00Z" }
  }
}
```

---

## Other Endpoints (Authenticated)

| Endpoint                    | Description                    |
|-----------------------------|--------------------------------|
| `/actuator/env`             | Environment properties         |
| `/actuator/configprops`     | Configuration properties       |
| `/actuator/loggers`         | Logger levels (GET/POST)       |
| `/actuator/threaddump`      | Thread dump                    |
| `/actuator/heapdump`        | Heap dump (binary download)    |
| `/actuator/metrics`         | Metric names listing           |
| `/actuator/metrics/{name}`  | Specific metric detail         |

---

## Distributed Tracing

Traces are exported via **OpenTelemetry OTLP** to the configured collector.

### Trace Context Propagation

All HTTP responses include W3C trace context headers:

```
traceparent: 00-abc123def456-789012-01
tracestate: lambrk=t:1
```

### Sampling

- **Development**: 100% sampling
- **Production**: 10% sampling (`management.tracing.sampling.probability=0.1`)

### Structured Logging

Log pattern includes trace/span IDs:

```
2026-02-07 14:00:00.123 [virtual-thread-42] INFO  [abc123,def456] c.e.r.s.PostService - Post created: id=1
```

### Custom Spans

All controller methods are annotated with `@NewSpan` for automatic span creation:

```
create-post (3ms)
├── db.query: findById (1ms)
├── db.query: save (1ms)
└── kafka.send: postCreated (0.5ms)
```
