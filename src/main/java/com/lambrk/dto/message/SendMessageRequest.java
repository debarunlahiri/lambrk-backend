package com.lambrk.dto.message;

import com.lambrk.domain.message.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload to send a new message")
public record SendMessageRequest(
    @NotBlank(message = "Recipient username is required")
        @Schema(description = "Username of the message recipient", example = "john_doe")
        String recipientUsername,
    @NotBlank(message = "Message content is required")
        @Size(max = 5000, message = "Message content must not exceed 5000 characters")
        @Schema(description = "Text content of the message", example = "Hey, how are you?")
        String content,
    @Schema(description = "Type of message", example = "TEXT", defaultValue = "TEXT")
        ChatMessage.MessageType messageType,
    @Schema(
            description = "URL of an attachment (image, video, file)",
            example = "https://cdn.lambrk.com/uploads/img.jpg")
        String attachmentUrl,
    @Schema(description = "MIME type of the attachment", example = "image/jpeg")
        String attachmentType) {
  public SendMessageRequest {
    if (messageType == null) {
      messageType = ChatMessage.MessageType.TEXT;
    }
  }
}
