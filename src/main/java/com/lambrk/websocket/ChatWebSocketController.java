package com.lambrk.websocket;

import com.lambrk.config.UserPrincipal;
import com.lambrk.dto.message.SendMessageRequest;
import com.lambrk.service.ChatMessageService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.security.Principal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * STOMP WebSocket controller for 1:1 real-time messaging.
 *
 * <p>WebSocket flow:
 *
 * <pre>
 *   Client subscribes:  /user/queue/messages
 *   Client sends:       /app/chat.send          → new message
 *                       /app/chat.read          → mark message as read
 *                       /app/chat.read.conversation → mark visible conversation as read
 *                       /app/chat.typing        → typing indicator
 * </pre>
 *
 * <p>All pushes go to {@code /user/{username}/queue/messages} as {@link
 * ChatMessageWebSocketPayload} envelopes. Clients switch on {@code eventType} to handle
 * MESSAGE_SENT, READ_RECEIPT, TYPING, MESSAGE_DELETED.
 */
@Controller
public class ChatWebSocketController {

  private static final Logger log = LoggerFactory.getLogger(ChatWebSocketController.class);

  private final ChatMessageService chatMessageService;
  private final SimpMessagingTemplate messagingTemplate;

  public ChatWebSocketController(
      ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
    this.chatMessageService = chatMessageService;
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Send a new message to another user.
   *
   * <p>Client publishes to: {@code /app/chat.send}
   *
   * <p>Payload: {@link SendMessageRequest}
   *
   * <p>Response delivered to: {@code /user/{senderUsername}/queue/messages} and: {@code
   * /user/{recipientUsername}/queue/messages}
   */
  @MessageMapping("/chat.send")
  @Counted(value = "chat.websocket.messages.sent")
  @Timed(value = "chat.websocket.send.duration")
  public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
    if (principal == null) {
      log.warn("Unauthenticated WebSocket message send attempt blocked");
      return;
    }
    UUID senderId = extractUserId(principal);
    try {
      chatMessageService.sendMessage(senderId, request);
      // Delivery is handled inside ChatMessageService via SimpMessagingTemplate
    } catch (Exception e) {
      log.error("Error sending message from {}: {}", principal.getName(), e.getMessage());
      // Push error event back to sender
      messagingTemplate.convertAndSendToUser(
          principal.getName(), "/queue/errors", "Failed to send message: " + e.getMessage());
    }
  }

  /**
   * Mark a specific message as read.
   *
   * <p>Client publishes to: {@code /app/chat.read}
   *
   * <p>Payload: {@code messageId} (String)
   */
  @MessageMapping("/chat.read")
  @Counted(value = "chat.websocket.read.receipts")
  public void markRead(@Payload String messageId, Principal principal) {
    if (principal == null) return;
    UUID userId = extractUserId(principal);
    try {
      chatMessageService.markAsRead(userId, messageId);
    } catch (Exception e) {
      log.warn(
          "Error marking message {} as read for {}: {}",
          messageId,
          principal.getName(),
          e.getMessage());
    }
  }

  /**
   * Mark every unread message in a conversation as read in one round trip.
   *
   * <p>Client publishes to: {@code /app/chat.read.conversation}
   *
   * <p>Payload: {@code conversationId} (String)
   */
  @MessageMapping("/chat.read.conversation")
  @Counted(value = "chat.websocket.read.conversations")
  public void markConversationRead(@Payload String conversationId, Principal principal) {
    if (principal == null) return;
    UUID userId = extractUserId(principal);
    try {
      chatMessageService.markConversationAsRead(userId, conversationId);
    } catch (Exception e) {
      log.warn(
          "Error marking conversation {} as read for {}: {}",
          conversationId,
          principal.getName(),
          e.getMessage());
    }
  }

  /**
   * Typing indicator — forwarded to the other participant in the conversation.
   *
   * <p>Client publishes to: {@code /app/chat.typing}
   *
   * <p>Payload: {@code conversationId} (String)
   */
  @MessageMapping("/chat.typing")
  public void sendTypingIndicator(@Payload String conversationId, Principal principal) {
    if (principal == null) return;
    UUID userId = extractUserId(principal);
    try {
      chatMessageService.sendTypingIndicator(userId, conversationId);
    } catch (Exception e) {
      log.debug("Ignoring typing indicator for {}: {}", conversationId, e.getMessage());
    }
  }

  // ─────────────────────────────────────────────
  // Internal helper
  // ─────────────────────────────────────────────

  private UUID extractUserId(Principal principal) {
    if (principal instanceof UserPrincipal up) {
      return up.getUserId();
    }
    if (principal instanceof Authentication authentication
        && authentication.getPrincipal() instanceof UserPrincipal up) {
      return up.getUserId();
    }
    throw new IllegalStateException(
        "Principal is not a UserPrincipal: " + principal.getClass().getName());
  }
}
