package com.lambrk.domain.message;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_messages")
@CompoundIndexes({
  @CompoundIndex(name = "idx_conversation_created", def = "{'conversationId': 1, 'createdAt': -1}"),
  @CompoundIndex(name = "idx_recipient_read", def = "{'recipientId': 1, 'isRead': 1}")
})
public class ChatMessage {

  @Id private String id;

  @Indexed private String conversationId;

  private UUID senderId;
  private String senderUsername;
  private String senderAvatarUrl;

  private UUID recipientId;
  private String recipientUsername;

  private String content;

  private MessageType messageType = MessageType.TEXT;

  private String attachmentUrl;
  private String attachmentType;

  private boolean isRead = false;
  private Instant readAt;

  private boolean isDeleted = false;
  private Instant deletedAt;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    FILE,
    SYSTEM
  }

  protected ChatMessage() {}

  public ChatMessage(
      String conversationId,
      UUID senderId,
      String senderUsername,
      String senderAvatarUrl,
      UUID recipientId,
      String recipientUsername,
      String content,
      MessageType messageType,
      String attachmentUrl,
      String attachmentType) {
    this.conversationId = conversationId;
    this.senderId = senderId;
    this.senderUsername = senderUsername;
    this.senderAvatarUrl = senderAvatarUrl;
    this.recipientId = recipientId;
    this.recipientUsername = recipientUsername;
    this.content = content;
    this.messageType = messageType != null ? messageType : MessageType.TEXT;
    this.attachmentUrl = attachmentUrl;
    this.attachmentType = attachmentType;
    this.isRead = false;
    this.isDeleted = false;
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getConversationId() {
    return conversationId;
  }

  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  public UUID getSenderId() {
    return senderId;
  }

  public void setSenderId(UUID senderId) {
    this.senderId = senderId;
  }

  public String getSenderUsername() {
    return senderUsername;
  }

  public void setSenderUsername(String senderUsername) {
    this.senderUsername = senderUsername;
  }

  public String getSenderAvatarUrl() {
    return senderAvatarUrl;
  }

  public void setSenderAvatarUrl(String senderAvatarUrl) {
    this.senderAvatarUrl = senderAvatarUrl;
  }

  public UUID getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(UUID recipientId) {
    this.recipientId = recipientId;
  }

  public String getRecipientUsername() {
    return recipientUsername;
  }

  public void setRecipientUsername(String recipientUsername) {
    this.recipientUsername = recipientUsername;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  public String getAttachmentUrl() {
    return attachmentUrl;
  }

  public void setAttachmentUrl(String attachmentUrl) {
    this.attachmentUrl = attachmentUrl;
  }

  public String getAttachmentType() {
    return attachmentType;
  }

  public void setAttachmentType(String attachmentType) {
    this.attachmentType = attachmentType;
  }

  public boolean isRead() {
    return isRead;
  }

  public void setRead(boolean read) {
    this.isRead = read;
  }

  public Instant getReadAt() {
    return readAt;
  }

  public void setReadAt(Instant readAt) {
    this.readAt = readAt;
  }

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    this.isDeleted = deleted;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
