package com.lambrk.domain.message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat_conversations")
@CompoundIndexes({
  @CompoundIndex(
      name = "idx_participants_last_msg",
      def = "{'participantIds': 1, 'lastMessageAt': -1}")
})
public class ChatConversation {

  @Id private String id;

  /**
   * Deterministic ID: min(userId1, userId2) + "_" + max(userId1, userId2) Ensures both participants
   * always resolve to the same conversation document.
   */
  @Indexed(unique = true)
  private String conversationId;

  @Indexed private List<UUID> participantIds = new ArrayList<>();

  private List<String> participantUsernames = new ArrayList<>();

  private String lastMessage;
  private UUID lastMessageSenderId;
  private Instant lastMessageAt;

  /** Maps userId (as String) → unread count for that user. */
  private Map<String, Integer> unreadCountMap = new HashMap<>();

  private boolean isActive = true;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;

  protected ChatConversation() {}

  public ChatConversation(
      String conversationId,
      UUID participant1Id,
      String participant1Username,
      UUID participant2Id,
      String participant2Username) {
    this.conversationId = conversationId;
    this.participantIds = new ArrayList<>(List.of(participant1Id, participant2Id));
    this.participantUsernames =
        new ArrayList<>(List.of(participant1Username, participant2Username));
    this.unreadCountMap = new HashMap<>();
    this.unreadCountMap.put(participant1Id.toString(), 0);
    this.unreadCountMap.put(participant2Id.toString(), 0);
    this.isActive = true;
  }

  /** Generates a deterministic conversation ID from two user UUIDs. */
  public static String buildConversationId(UUID userId1, UUID userId2) {
    int cmp = userId1.compareTo(userId2);
    UUID first = cmp <= 0 ? userId1 : userId2;
    UUID second = cmp <= 0 ? userId2 : userId1;
    return first.toString() + "_" + second.toString();
  }

  public void incrementUnreadCount(UUID userId) {
    unreadCountMap.merge(userId.toString(), 1, Integer::sum);
  }

  public void resetUnreadCount(UUID userId) {
    unreadCountMap.put(userId.toString(), 0);
  }

  public int getUnreadCount(UUID userId) {
    return unreadCountMap.getOrDefault(userId.toString(), 0);
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

  public List<UUID> getParticipantIds() {
    return participantIds;
  }

  public void setParticipantIds(List<UUID> participantIds) {
    this.participantIds = participantIds;
  }

  public List<String> getParticipantUsernames() {
    return participantUsernames;
  }

  public void setParticipantUsernames(List<String> participantUsernames) {
    this.participantUsernames = participantUsernames;
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public void setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
  }

  public UUID getLastMessageSenderId() {
    return lastMessageSenderId;
  }

  public void setLastMessageSenderId(UUID lastMessageSenderId) {
    this.lastMessageSenderId = lastMessageSenderId;
  }

  public Instant getLastMessageAt() {
    return lastMessageAt;
  }

  public void setLastMessageAt(Instant lastMessageAt) {
    this.lastMessageAt = lastMessageAt;
  }

  public Map<String, Integer> getUnreadCountMap() {
    return unreadCountMap;
  }

  public void setUnreadCountMap(Map<String, Integer> unreadCountMap) {
    this.unreadCountMap = unreadCountMap;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    this.isActive = active;
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
