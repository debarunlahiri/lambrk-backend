# Kafka Events

All domain events are published asynchronously via **Spring Cloud Stream** with the `StreamBridge` API.

---

## Topics

| Topic              | Producer          | Description                    |
|--------------------|-------------------|--------------------------------|
| `post.created`     | PostService       | New post published             |
| `post.updated`     | PostService       | Existing post edited           |
| `comment.created`  | CommentService    | New comment or reply posted    |
| `vote.cast`        | VoteService       | New vote (not toggles/flips)   |
| `file.uploaded`    | FileUploadService | New file uploaded              |
| `file.deleted`     | FileUploadService | File deleted                   |

---

## Event Schemas

### PostEvent

Published to `post.created` and `post.updated`.

```json
{
  "postId": 1,
  "title": "My first post about Spring Boot 3.5",
  "authorId": 2,
  "sublambrkId": 1,
  "timestamp": "2026-02-07T14:00:00Z",
  "eventType": "POST_CREATED"
}
```

| Field       | Type    | Description                          |
|-------------|---------|--------------------------------------|
| postId      | Long    | ID of the post                       |
| title       | String  | Post title                           |
| authorId    | Long    | ID of the author                     |
| sublambrkId | Long    | ID of the sublambrk                  |
| timestamp   | Instant | When the event occurred              |
| eventType   | String  | `POST_CREATED` or `POST_UPDATED`     |

### CommentEvent

Published to `comment.created`.

```json
{
  "commentId": 10,
  "content": "Great post!",
  "authorId": 3,
  "postId": 1,
  "parentCommentId": null,
  "timestamp": "2026-02-07T14:05:00Z",
  "eventType": "COMMENT_CREATED"
}
```

| Field            | Type         | Description                    |
|------------------|--------------|--------------------------------|
| commentId        | Long         | ID of the comment              |
| content          | String       | Comment text                   |
| authorId         | Long         | ID of the author               |
| postId           | Long         | ID of the parent post          |
| parentCommentId  | Long or null | ID of parent comment (replies) |
| timestamp        | Instant      | When the event occurred        |
| eventType        | String       | `COMMENT_CREATED`              |

### VoteEvent

Published to `vote.cast`.

```json
{
  "voteId": 50,
  "voteType": "UPVOTE",
  "userId": 2,
  "postId": 1,
  "commentId": null,
  "timestamp": "2026-02-07T14:10:00Z",
  "eventType": "VOTE_CAST"
}
```

| Field     | Type         | Description                       |
|-----------|--------------|-----------------------------------|
| voteId    | Long         | ID of the vote                    |
| voteType  | String       | `UPVOTE` or `DOWNVOTE`            |
| userId    | Long         | ID of the voter                   |
| postId    | Long or null | Post ID (if post vote)            |
| commentId | Long or null | Comment ID (if comment vote)      |
| timestamp | Instant      | When the event occurred           |
| eventType | String       | `VOTE_CAST`                       |

### FileUploadEvent

Published to `file.uploaded` and `file.deleted`.

```json
{
  "fileId": 1001,
  "fileName": "abc123def456.jpg",
  "fileType": "AVATAR",
  "fileSize": 1048576,
  "userId": 2,
  "timestamp": "2026-02-07T16:00:00Z",
  "eventType": "FILE_UPLOADED"
}
```

| Field     | Type    | Description                          |
|-----------|---------|--------------------------------------|
| fileId    | Long    | ID of the file                       |
| fileName  | String  | Unique file name                     |
| fileType  | String  | `AVATAR`, `POST_IMAGE`, `POST_VIDEO`, `SUBREDDIT_ICON`, `SUBREDDIT_HEADER`, `BANNER` |
| fileSize  | Long    | File size in bytes                   |
| userId    | Long    | ID of the uploader                   |
| timestamp | Instant | When the event occurred              |
| eventType | String  | `FILE_UPLOADED` or `FILE_DELETED`    |

---

## Kafka Configuration

```yaml
spring.cloud.stream:
  kafka:
    binder:
      brokers: localhost:9092
      auto-create-topics: true
      configuration:
        key.serializer: StringSerializer
        value.serializer: JsonSerializer
        key.deserializer: StringDeserializer
        value.deserializer: JsonDeserializer
        spring.json.trusted.packages: "com.example.lambrk.dto"
  bindings:
    postCreated:
      destination: post.created
      content-type: application/json
    postUpdated:
      destination: post.updated
      content-type: application/json
    commentCreated:
      destination: comment.created
      content-type: application/json
    voteCast:
      destination: vote.cast
      content-type: application/json
    fileUploaded:
      destination: file.uploaded
      content-type: application/json
    fileDeleted:
      destination: file.deleted
      content-type: application/json
```

---

## Resilience

- Events are published **fire-and-forget** — failures do not roll back the transaction
- A `kafkaProducer` circuit breaker protects against Kafka outages (opens at 70% failure rate)
- Failed events are logged but not retried (at-most-once delivery)

---

## Consumer Examples

To consume these events in another service:

```java
@Bean
public Consumer<KafkaEventService.PostEvent> postCreatedConsumer() {
    return event -> {
        log.info("Post created: {} by user {}", event.postId(), event.authorId());
        // Trigger notifications, analytics, search indexing, etc.
    };
}
```

### Potential Consumers

| Consumer              | Listens To        | Purpose                          |
|-----------------------|-------------------|----------------------------------|
| NotificationService   | comment.created   | Notify post author of new reply  |
| SearchIndexer         | post.created/updated | Update search index           |
| AnalyticsService      | vote.cast, file.uploaded/deleted | Track engagement and storage metrics |
| RecommendationEngine  | All topics        | Update user preference model     |
| AuditLogger           | All topics        | Compliance audit trail           |
| StorageMonitor        | file.uploaded/deleted | Track S3 storage usage and costs |
| FreeTierNotifier      | file.uploaded     | Alert users approaching free tier limits |
