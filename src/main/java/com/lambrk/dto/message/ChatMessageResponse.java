package com.lambrk.dto.message;

import com.lambrk.domain.message.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "A single chat message in a conversation")
public record ChatMessageResponse(
    @Schema(description = "MongoDB ObjectId of the message") String id,
    @Schema(description = "Deterministic conversation identifier") String conversationId,
    @Schema(description = "UUID of the sender") UUID senderId,
    @Schema(description = "Username of the sender", example = "jane_doe") String senderUsername,
    @Schema(description = "Avatar URL of the sender") String senderAvatarUrl,
    @Schema(description = "UUID of the recipient") UUID recipientId,
    @Schema(description = "Username of the recipient", example = "john_doe")
        String recipientUsername,
    @Schema(description = "Text content of the message") String content,
    @Schema(description = "Type of message (TEXT, IMAGE, VIDEO, FILE, SYSTEM)")
        ChatMessage.MessageType messageType,
    @Schema(description = "Attachment URL if present") String attachmentUrl,
    @Schema(description = "Attachment MIME type if present") String attachmentType,
    @Schema(description = "Whether the recipient has read this message") boolean isRead,
    @Schema(description = "Timestamp when the message was read") Instant readAt,
    @Schema(description = "Whether the message has been deleted (soft delete)") boolean isDeleted,
    @Schema(description = "When the message was sent") Instant createdAt) {
  public static ChatMessageResponse from(ChatMessage msg) {
    return new ChatMessageResponse(
        msg.getId(),
        msg.getConversationId(),
        msg.getSenderId(),
        msg.getSenderUsername(),
        msg.getSenderAvatarUrl(),
        msg.getRecipientId(),
        msg.getRecipientUsername(),
        msg.isDeleted() ? null : msg.getContent(), // hide content of deleted messages
        msg.getMessageType(),
        msg.isDeleted() ? null : msg.getAttachmentUrl(),
        msg.isDeleted() ? null : msg.getAttachmentType(),
        msg.isRead(),
        msg.getReadAt(),
        msg.isDeleted(),
        msg.getCreatedAt());
  }
}
