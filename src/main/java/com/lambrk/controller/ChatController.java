package com.lambrk.controller;

import com.lambrk.config.UserPrincipal;
import com.lambrk.dto.message.ChatMessageResponse;
import com.lambrk.dto.message.ConversationResponse;
import com.lambrk.dto.message.SendMessageRequest;
import com.lambrk.service.ChatMessageService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for 1:1 messaging.
 *
 * <p>Users are sourced from <strong>PostgreSQL</strong>. All messages and conversations are
 * persisted in <strong>MongoDB</strong> ({@code chat_messages} and {@code chat_conversations}
 * collections).
 *
 * <p>The primary real-time delivery mechanism is WebSocket (STOMP). These REST endpoints act as a
 * <em>fallback</em> for clients that cannot maintain a persistent WebSocket connection, and as the
 * canonical HTTP API for fetching history and inbox state.
 *
 * <h2>Endpoints</h2>
 *
 * <pre>
 * GET    /api/messages/conversations              — inbox (paginated, sorted by last message)
 * GET    /api/messages/conversations/{username}   — open or create a conversation
 * GET    /api/messages/{conversationId}           — paginated message history (newest first)
 * POST   /api/messages                            — send a message
 * PUT    /api/messages/{messageId}/read           — mark a message as read
 * DELETE /api/messages/{messageId}                — soft-delete a message (sender only)
 * GET    /api/messages/unread/count               — total unread count for the nav badge
 * </pre>
 *
 * <h2>Authentication</h2>
 *
 * All endpoints require a valid JWT Bearer token. The token is resolved into a {@link
 * UserPrincipal} by the {@code JwtAuthenticationFilter}.
 *
 * <h2>Metrics</h2>
 *
 * Each endpoint is instrumented with Micrometer {@code @Counted} and {@code @Timed} annotations so
 * that request counts and latencies are exported to Prometheus.
 */
@RestController
@RequestMapping("/api/messages")
public class ChatController {

  private final ChatMessageService chatMessageService;

  /**
   * Constructs the controller with its required service dependency.
   *
   * @param chatMessageService service layer handling business logic for messaging
   */
  public ChatController(ChatMessageService chatMessageService) {
    this.chatMessageService = chatMessageService;
  }

  // ─────────────────────────────────────────────
  // Inbox — list all conversations
  // ─────────────────────────────────────────────

  /**
   * Returns the authenticated user's conversation inbox, sorted by most recent message.
   *
   * <p>Each entry shows the other participant's identity, a preview of the last message, and the
   * caller's unread count for that conversation. Only active (non-archived) conversations are
   * included.
   *
   * @param page zero-based page index (default {@code 0})
   * @param size page size (default {@code 20})
   * @param principal the authenticated user
   * @return {@code 200 OK} with a paginated list of {@link ConversationResponse}
   */
  @GetMapping("/conversations")
  @Counted(value = "chat.http.conversations.listed")
  @Timed(value = "chat.http.conversations.list.duration")
  public ResponseEntity<Page<ConversationResponse>> getConversations(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = requireUserId(principal);
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(chatMessageService.getConversations(userId, pageable));
  }

  // ─────────────────────────────────────────────
  // Get or start a conversation with a user
  // ─────────────────────────────────────────────

  /**
   * Opens (or creates) a 1:1 conversation between the authenticated user and {@code username}.
   *
   * <p>Both users must exist in PostgreSQL. A deterministic conversation ID is derived from the
   * sorted pair of UUIDs, so calling this endpoint multiple times is <em>idempotent</em> — the same
   * MongoDB document is returned if the conversation already exists.
   *
   * <p>Typical client flow: open this endpoint when a user clicks on another user's profile to
   * start a chat. The returned {@code conversationId} is then used to load history ({@code GET
   * /api/messages/{conversationId}}) and subscribe to the WebSocket topic.
   *
   * @param username the target user's username (resolved via PostgreSQL)
   * @param principal the authenticated caller
   * @return {@code 200 OK} with the existing or newly created {@link ConversationResponse}
   * @throws IllegalArgumentException if {@code username} does not exist or equals the caller's
   *     username
   */
  @GetMapping("/conversations/{username}")
  @Counted(value = "chat.http.conversation.opened")
  @Timed(value = "chat.http.conversation.open.duration")
  public ResponseEntity<ConversationResponse> getOrStartConversation(
      @PathVariable String username, @AuthenticationPrincipal UserPrincipal principal) {

    UUID currentUserId = requireUserId(principal);
    ConversationResponse response =
        chatMessageService.getOrStartConversation(currentUserId, username);
    return ResponseEntity.ok(response);
  }

  // ─────────────────────────────────────────────
  // Message history for a conversation
  // ─────────────────────────────────────────────

  /**
   * Returns the paginated message history for a conversation, sorted newest first.
   *
   * <p>Soft-deleted messages are excluded. Only participants of the conversation may call this
   * endpoint — a {@link SecurityException} is thrown otherwise.
   *
   * <p>Use {@code page} and {@code size} to implement infinite-scroll / load-more in the client UI
   * (e.g. load page 0 on open, increment page on scroll-up).
   *
   * @param conversationId deterministic conversation identifier in the format {@code
   *     smallerUUID_largerUUID}
   * @param page zero-based page index (default {@code 0})
   * @param size page size (default {@code 30})
   * @param principal the authenticated user (must be a participant)
   * @return {@code 200 OK} with a paginated list of {@link ChatMessageResponse}
   * @throws SecurityException if the caller is not a participant in the conversation
   * @throws IllegalArgumentException if the conversation does not exist
   */
  @GetMapping("/{conversationId}")
  @Counted(value = "chat.http.messages.fetched")
  @Timed(value = "chat.http.messages.fetch.duration")
  public ResponseEntity<Page<ChatMessageResponse>> getMessages(
      @PathVariable String conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "30") int size,
      @AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = requireUserId(principal);
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(chatMessageService.getMessages(userId, conversationId, pageable));
  }

  // ─────────────────────────────────────────────
  // Send a message (REST fallback — WebSocket is preferred)
  // ─────────────────────────────────────────────

  /**
   * Sends a new message to another user via HTTP.
   *
   * <p><b>Preferred path:</b> publish to {@code /app/chat.send} over STOMP WebSocket. Use this HTTP
   * endpoint only when a persistent WebSocket connection is not available.
   *
   * <p>Internally, after persisting the message to MongoDB, the service automatically pushes a
   * {@code MESSAGE_SENT} WebSocket event to both participants' queues ({@code
   * /user/{username}/queue/messages}) so that any open browser tabs or mobile sessions receive the
   * message in real time regardless of which transport was used to send it.
   *
   * @param request the message payload — includes recipient username, content, message type, and
   *     optional attachment URL / MIME type
   * @param principal the authenticated sender
   * @return {@code 200 OK} with the persisted {@link ChatMessageResponse}
   * @throws IllegalArgumentException if the recipient does not exist or the sender attempts to
   *     message themselves
   */
  @PostMapping
  @Counted(value = "chat.http.messages.sent")
  @Timed(value = "chat.http.messages.send.duration")
  public ResponseEntity<ChatMessageResponse> sendMessage(
      @Valid @RequestBody SendMessageRequest request,
      @AuthenticationPrincipal UserPrincipal principal) {

    UUID senderId = requireUserId(principal);
    ChatMessageResponse response = chatMessageService.sendMessage(senderId, request);
    return ResponseEntity.ok(response);
  }

  // ─────────────────────────────────────────────
  // Mark a message as read
  // ─────────────────────────────────────────────

  /**
   * Marks a single message as read and pushes a read-receipt to the original sender via WebSocket.
   *
   * <p>Only the <em>recipient</em> of the message may call this endpoint. The operation is
   * idempotent — calling it on an already-read message returns the current state without making
   * further changes or emitting duplicate WebSocket events.
   *
   * <p>Side effects:
   *
   * <ul>
   *   <li>Sets {@code isRead = true} and {@code readAt = now()} on the message document.
   *   <li>Resets the caller's unread counter in the parent {@code ChatConversation}.
   *   <li>Pushes a {@code READ_RECEIPT} WebSocket payload to the sender's queue.
   * </ul>
   *
   * @param messageId MongoDB ObjectId of the target message
   * @param principal the authenticated user (must be the message recipient)
   * @return {@code 200 OK} with the updated {@link ChatMessageResponse}
   * @throws SecurityException if the caller is not the message recipient
   * @throws IllegalArgumentException if no message with {@code messageId} exists
   */
  @PutMapping("/{messageId}/read")
  @Counted(value = "chat.http.messages.read")
  @Timed(value = "chat.http.messages.read.duration")
  public ResponseEntity<ChatMessageResponse> markAsRead(
      @PathVariable String messageId, @AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = requireUserId(principal);
    return ResponseEntity.ok(chatMessageService.markAsRead(userId, messageId));
  }

  /**
   * Marks every unread message in a conversation as read in one request.
   *
   * <p>Use this when opening a chat or when the visible message list enters the viewport. It avoids
   * sending one read request per message and pushes read receipts back to the sender.
   *
   * @param conversationId deterministic conversation identifier
   * @param principal authenticated recipient
   * @return {@code 200 OK} with the number of messages marked read
   */
  @PutMapping("/conversations/{conversationId}/read")
  @Counted(value = "chat.http.conversations.read")
  @Timed(value = "chat.http.conversations.read.duration")
  public ResponseEntity<Integer> markConversationAsRead(
      @PathVariable String conversationId, @AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = requireUserId(principal);
    return ResponseEntity.ok(chatMessageService.markConversationAsRead(userId, conversationId));
  }

  // ─────────────────────────────────────────────
  // Soft-delete a message (only the sender can delete)
  // ─────────────────────────────────────────────

  /**
   * Soft-deletes a message so its content is no longer visible to either participant.
   *
   * <p>Only the original <em>sender</em> may delete a message. The MongoDB document is
   * <strong>not</strong> physically removed — the {@code isDeleted} flag is set and the content /
   * attachment fields are nulled out in all API responses going forward.
   *
   * <p>Side effects:
   *
   * <ul>
   *   <li>Sets {@code isDeleted = true} and {@code deletedAt = now()} on the message.
   *   <li>Pushes a {@code MESSAGE_DELETED} WebSocket event to <em>both</em> participants.
   * </ul>
   *
   * @param messageId MongoDB ObjectId of the message to delete
   * @param principal the authenticated user (must be the original sender)
   * @return {@code 200 OK} with the updated {@link ChatMessageResponse} (content redacted)
   * @throws SecurityException if the caller is not the original sender
   * @throws IllegalArgumentException if no message with {@code messageId} exists
   */
  @DeleteMapping("/{messageId}")
  @Counted(value = "chat.http.messages.deleted")
  @Timed(value = "chat.http.messages.delete.duration")
  public ResponseEntity<ChatMessageResponse> deleteMessage(
      @PathVariable String messageId, @AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = requireUserId(principal);
    return ResponseEntity.ok(chatMessageService.deleteMessage(userId, messageId));
  }

  // ─────────────────────────────────────────────
  // Total unread count (for nav badge)
  // ─────────────────────────────────────────────

  /**
   * Returns the total number of unread messages across <em>all</em> conversations for the
   * authenticated user.
   *
   * <p>Intended to drive the unread badge in the navigation bar. The count reflects all messages
   * where {@code recipientId} matches the caller and {@code isRead = false} and {@code isDeleted =
   * false}.
   *
   * @param principal the authenticated user
   * @return {@code 200 OK} with the unread message count (≥ 0)
   */
  @GetMapping("/unread/count")
  @Counted(value = "chat.http.unread.count")
  @Timed(value = "chat.http.unread.count.duration")
  public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {

    UUID userId = requireUserId(principal);
    return ResponseEntity.ok(chatMessageService.getTotalUnreadCount(userId));
  }

  // ─────────────────────────────────────────────
  // Internal helper
  // ─────────────────────────────────────────────

  private UUID requireUserId(UserPrincipal principal) {
    if (principal == null) {
      throw new SecurityException("Authentication required");
    }
    return principal.getUserId();
  }
}
