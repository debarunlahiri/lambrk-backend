# Lambrk Backend API Documentation

Lambrk is a Lambrk-like backend built with Spring Boot. This documentation reflects the endpoints currently implemented in the controllers under `src/main/java/com/lambrk/controller` and websocket mappings under `src/main/java/com/lambrk/websocket`.

## Base URL

```text
http://localhost:9500
```

## Authentication

Send `Authorization: Bearer <accessToken>` for every endpoint except `/api/auth/**`, `/health`, `/actuator/health`, `/actuator/info`, `/actuator/prometheus`, `/swagger-ui/**`, and `/v3/api-docs/**`.

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Auth tokens are returned by [Auth API](docs/api/AUTH_API.md). The application is stateless and uses JWT bearer authentication.

## Documentation Index

| Area | Documentation | Base path |
| --- | --- | --- |
| Authentication | [AUTH_API.md](docs/api/AUTH_API.md) | `/api/auth` |
| Users | [USERS_API.md](docs/api/USERS_API.md) | `/api/users` |
| Communities | [COMMUNITIES_API.md](docs/api/COMMUNITIES_API.md) | `/api/communities` |
| Categories | [CATEGORIES_API.md](docs/api/CATEGORIES_API.md) | `/api/categories` |
| Posts | [POSTS_API.md](docs/api/POSTS_API.md) | `/api/posts` |
| Comments | [COMMENTS_API.md](docs/api/COMMENTS_API.md) | `/api/comments` |
| Votes | [VOTES_API.md](docs/api/VOTES_API.md) | `/api/votes` |
| Bookmarks | [BOOKMARKS_API.md](docs/api/BOOKMARKS_API.md) | `/api/bookmarks` |
| Feed | [FEED_API.md](docs/api/FEED_API.md) | `/api/feed` |
| Search | [SEARCH_API.md](docs/api/SEARCH_API.md) | `/api/search` |
| Recommendations | [RECOMMENDATIONS_API.md](docs/api/RECOMMENDATIONS_API.md) | `/api/recommendations` |
| Notifications | [NOTIFICATIONS_API.md](docs/api/NOTIFICATIONS_API.md) | `/api/notifications` |
| Files | [FILES_API.md](docs/api/FILES_API.md) | `/api/files` |
| LoopMix (Reels) | [LOOPMIX_API.md](docs/api/LOOPMIX_API.md) | `/api/posts/media`, `/view`, `/related` |
| Admin | [ADMIN_API.md](docs/api/ADMIN_API.md) | `/api/admin` |
| Logs | [LOGS_API.md](docs/api/LOGS_API.md) | `/api/logs` |
| WebSocket | [WEBSOCKET_API.md](docs/api/WEBSOCKET_API.md) | `/ws` STOMP |
| Actuator/OpenAPI | [ACTUATOR_API.md](docs/api/ACTUATOR_API.md) | `/actuator`, `/v3/api-docs` |
| Errors | [ERRORS_API.md](docs/api/ERRORS_API.md) | shared |

## Endpoint Summary

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | Public | Register a user and return JWT tokens. |
| POST | `/api/auth/login` | Public | Authenticate and return JWT tokens. |
| POST | `/api/auth/refresh` | Public | Refresh access token from refresh token string. |
| GET | `/api/users/{userId}` | Public | Get user by id with social counts. |
| GET | `/api/users/username/{username}` | Public | Get user by username with social counts. |
| GET | `/api/users/me` | User | Get current authenticated user. |
| PUT | `/api/users/me` | User | Update current user profile. |
| GET | `/api/users/top` | Public | List top users by karma. |
| GET | `/api/users/search` | Public | Search active users. |
| POST | `/api/users/{userId}/follow` | User | Follow a user. |
| DELETE | `/api/users/{userId}/follow` | User | Unfollow a user. |
| GET | `/api/users/{userId}/followers` | Public | List followers. |
| GET | `/api/users/{userId}/following` | Public | List followed users. |
| GET | `/api/users/{userId}/friends` | Public | List accepted friends. |
| GET | `/api/users/{userId}/mutual/followers` | Public | Mutual followers with another user. |
| GET | `/api/users/{userId}/mutual/following` | Public | Mutual followed users with another user. |
| GET | `/api/users/{userId}/mutual/friends` | Public | Mutual friends with another user. |
| GET | `/api/users/{userId}/social-stats` | Public | Follower/following/friend counts. |
| POST | `/api/users/{userId}/friend-request` | User | Send friend request. |
| POST | `/api/users/{userId}/friend-request/accept` | User | Accept friend request. |
| POST | `/api/users/{userId}/friend-request/decline` | User | Decline friend request. |
| DELETE | `/api/users/{userId}/friend-request` | User | Cancel outgoing friend request. |
| DELETE | `/api/users/{userId}/friend` | User | Remove friend. |
| GET | `/api/users/me/friend-requests/incoming` | User | Incoming pending friend requests. |
| GET | `/api/users/me/friend-requests/outgoing` | User | Outgoing pending friend requests. |
| DELETE | `/api/users/{userId}` | Admin | Delete user. |
| POST | `/api/communities` | User | Create community. |
| GET | `/api/communities` | User | List public communities. |
| GET | `/api/communities/{communityId}` | User | Get community by id. |
| GET | `/api/communities/r/{name}` | User | Get community by name. |
| GET | `/api/communities/trending` | User | List trending communities. |
| GET | `/api/communities/search` | User | Search communities. |
| PUT | `/api/communities/{communityId}` | Moderator/Admin | Update community. |
| POST | `/api/communities/{communityId}/subscribe` | User | Subscribe. |
| POST | `/api/communities/{communityId}/unsubscribe` | User | Unsubscribe. |
| GET | `/api/communities/user/subscriptions` | User | Current user's subscriptions. |
| POST | `/api/categories` | Admin | Create category. |
| GET | `/api/categories` | User | List all categories. |
| GET | `/api/categories/{categoryId}` | User | Get category by id. |
| GET | `/api/categories/slug/{slug}` | User | Get category by slug. |
| PUT | `/api/categories/{categoryId}` | Admin | Update category. |
| DELETE | `/api/categories/{categoryId}` | Admin | Delete category. |
| POST | `/api/posts` | User | Create post. |
| GET | `/api/posts/{postId}` | User | Get post. |
| GET | `/api/posts/hot` | User | Hot posts. |
| GET | `/api/posts/new` | User | New posts. |
| GET | `/api/posts/top` | User | Top posts. |
| GET | `/api/posts/community/{communityId}` | User | Posts in community. |
| GET | `/api/posts/user/{userId}` | User | Posts by user. |
| GET | `/api/posts/search` | User | Search posts. |
| PUT | `/api/posts/{postId}` | User | Update own post. |
| DELETE | `/api/posts/{postId}` | User | Delete own post. |
| GET | `/api/posts/stickied` | User | Stickied posts, optionally by community. |
| POST | `/api/comments` | User | Create top-level comment. |
| POST | `/api/comments/{commentId}/reply` | User | Reply to a comment. |
| GET | `/api/comments/{commentId}` | User | Get comment. |
| GET | `/api/comments/post/{postId}` | User | Comments for post (with reply preview). |
| GET | `/api/comments/{commentId}/replies` | User | Replies for comment. |
| GET | `/api/comments/user/{userId}` | User | Comments by user. |
| PUT | `/api/comments/{commentId}` | User | Update own comment content. |
| DELETE | `/api/comments/{commentId}` | User | Delete own comment. |
| GET | `/api/comments/search` | User | Search comments. |
| POST | `/api/votes/post` | User | Vote on post. |
| POST | `/api/votes/comment` | User | Vote on comment. |
| POST | `/api/bookmarks/{postId}` | User | Bookmark a post. |
| DELETE | `/api/bookmarks/{postId}` | User | Remove bookmark. |
| GET | `/api/bookmarks` | User | List bookmarked posts. |
| GET | `/api/bookmarks/{postId}/status` | User | Check bookmark status. |
| GET | `/api/bookmarks/count` | User | Bookmark count. |
| GET | `/api/feed` | User role | Personalized feed. |
| POST | `/api/feed` | User role | Personalized feed with filters. |
| GET | `/api/feed/hot` | User role | Hot feed. |
| GET | `/api/feed/new` | User role | New feed. |
| GET | `/api/feed/top` | User role | Top feed. |
| GET | `/api/feed/discover` | User role | Discovery feed. |
| POST | `/api/search` | User | Advanced search. |
| GET | `/api/search/posts` | User | Search posts. |
| GET | `/api/search/comments` | User | Search comments. |
| GET | `/api/search/users` | User | Search users. |
| GET | `/api/search/communities` | User | Search communities. |
| GET | `/api/search/all` | User | Search all content. |
| GET | `/api/search/suggestions` | User | Search suggestions. |
| GET | `/api/search/trending` | User | Trending search response. |
| POST | `/api/recommendations` | User | Recommendations from request. |
| GET | `/api/recommendations/posts/{userId}` | User | Recommended posts. |
| GET | `/api/recommendations/communities/{userId}` | User | Recommended communities. |
| GET | `/api/recommendations/users/{userId}` | User | Recommended users. |
| GET | `/api/recommendations/comments/{userId}` | User | Recommended comments. |
| GET | `/api/recommendations/context/{userId}` | User | Contextual recommendations. |
| GET | `/api/recommendations/trending` | User | Trending recommendations. |
| POST | `/api/notifications` | User | Create notification. |
| GET | `/api/notifications` | User | Current user's notifications. |
| GET | `/api/notifications/unread` | User | Unread notifications. |
| PUT | `/api/notifications/{notificationId}/read` | User | Mark notification read. |
| PUT | `/api/notifications/read-all` | User | Mark all read. |
| DELETE | `/api/notifications/{notificationId}` | User | Delete notification. |
| DELETE | `/api/notifications` | User | Delete all notifications. |
| GET | `/api/notifications/count/unread` | User | Unread count. |
| GET | `/api/notifications/type/{type}` | User | Notifications by type. |
| POST | `/api/files/upload` | User | Upload files (bulk, up to 20). |
| GET | `/api/files/{fileId}` | User | File metadata. |
| GET | `/api/files/{fileId}/content` | User | Download file content. |
| GET | `/api/files` | User | Current user's files. |
| GET | `/api/files/type/{type}` | User | Files by type. |
| GET | `/api/files/public` | User | Public files. |
| PUT | `/api/files/{fileId}` | User | Update file metadata. |
| DELETE | `/api/files/{fileId}` | User | Delete file. |
| GET | `/api/files/stats` | User | Placeholder file stats. |
| GET | `/api/files/search` | User | Placeholder file search. |
| GET | `/api/files/recent` | User | Placeholder recent files. |
| POST | `/api/admin/actions` | Admin | Perform generic admin action. |
| GET | `/api/admin/actions` | Admin | List admin actions. |
| GET | `/api/admin/actions/user/{userId}` | Admin | Actions by target user. |
| GET | `/api/admin/actions/active` | Admin | Active actions. |
| POST | `/api/admin/ban-user/{userId}` | Admin | Ban user. |
| POST | `/api/admin/suspend-user/{userId}` | Admin | Suspend user. |
| POST | `/api/admin/delete-post/{postId}` | Admin | Admin delete post. |
| POST | `/api/admin/delete-comment/{commentId}` | Admin | Admin delete comment. |
| POST | `/api/admin/lock-post/{postId}` | Admin | Lock post. |
| POST | `/api/admin/quarantine-post/{postId}` | Admin | Quarantine post. |
| POST | `/api/admin/remove-moderator/{userId}` | Admin | Remove moderator role/action. |
| GET | `/api/logs` | Admin | All request logs. |
| GET | `/api/logs/user/{userId}` | Admin | Logs by user. |
| GET | `/api/logs/endpoint` | Admin | Logs by endpoint path. |
| GET | `/api/logs/method/{method}` | Admin | Logs by HTTP method. |
| GET | `/api/logs/status/{statusCode}` | Admin | Logs by status code. |
| GET | `/api/logs/errors` | Admin | Error logs. |
| GET | `/api/logs/exceptions` | Admin | Exception logs. |
| GET | `/api/logs/anonymous` | Admin | Anonymous request logs. |
| GET | `/api/logs/authenticated` | Admin | Authenticated request logs. |

## Common Response Notes

Paged endpoints return Spring Data `Page<T>` responses. Empty placeholder endpoints are documented explicitly in their files.

See [ERRORS_API.md](docs/api/ERRORS_API.md) for shared error formats.
