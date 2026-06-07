package com.lambrk.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Payload pushed over WebSocket to both sender and recipient when a new message is delivered or a
 * read receipt is sent.
 */
@Schema(description = "WebSocket push payload for real-time message delivery")
public record ChatMessageWebSocketPayload(
    @Schema(description = "Event type: MESSAGE_SENT, READ_RECEIPT, TYPING, MESSAGE_DELETED")
        EventType eventType,
    @Schema(description = "The full message, present for MESSAGE_SENT and MESSAGE_DELETED events")
        ChatMessageResponse message,
    @Schema(description = "Message ID, present for READ_RECEIPT events") String messageId,
    @Schema(description = "Conversation ID this event belongs to") String conversationId,
    @Schema(description = "Username of who triggered the event") String actorUsername,
    @Schema(description = "When this event was generated") Instant timestamp) {
  public enum EventType {
    MESSAGE_SENT,
    READ_RECEIPT,
    TYPING,
    MESSAGE_DELETED
  }

  public static ChatMessageWebSocketPayload ofNewMessage(ChatMessageResponse message) {
    return new ChatMessageWebSocketPayload(
        EventType.MESSAGE_SENT,
        message,
        null,
        message.conversationId(),
        message.senderUsername(),
        Instant.now());
  }

  public static ChatMessageWebSocketPayload ofReadReceipt(
      String messageId, String conversationId, String readerUsername) {
    return new ChatMessageWebSocketPayload(
        EventType.READ_RECEIPT, null, messageId, conversationId, readerUsername, Instant.now());
  }

  public static ChatMessageWebSocketPayload ofDeleted(ChatMessageResponse message) {
    return new ChatMessageWebSocketPayload(
        EventType.MESSAGE_DELETED,
        message,
        null,
        message.conversationId(),
        message.senderUsername(),
        Instant.now());
  }

  public static ChatMessageWebSocketPayload ofTyping(String conversationId, String typingUsername) {
    return new ChatMessageWebSocketPayload(
        EventType.TYPING, null, null, conversationId, typingUsername, Instant.now());
  }
}
