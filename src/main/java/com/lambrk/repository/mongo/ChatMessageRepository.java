package com.lambrk.repository.mongo;

import com.lambrk.domain.message.ChatMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

  /** Paginated message history for a conversation (newest first, excludes soft-deleted). */
  Page<ChatMessage> findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
      String conversationId, Pageable pageable);

  /** All unread messages in a conversation for a specific recipient. */
  List<ChatMessage> findByConversationIdAndRecipientIdAndIsReadFalseAndIsDeletedFalse(
      String conversationId, UUID recipientId);

  /** All unread message IDs in a conversation for a specific recipient. */
  List<ChatMessage>
      findByConversationIdAndRecipientIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtAsc(
          String conversationId, UUID recipientId);

  /** Count of unread messages per conversation for a recipient. */
  long countByConversationIdAndRecipientIdAndIsReadFalseAndIsDeletedFalse(
      String conversationId, UUID recipientId);

  /** All unread messages across all conversations for a user (for total unread badge). */
  long countByRecipientIdAndIsReadFalseAndIsDeletedFalse(UUID recipientId);
}
