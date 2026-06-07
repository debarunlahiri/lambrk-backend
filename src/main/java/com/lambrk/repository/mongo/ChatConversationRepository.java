package com.lambrk.repository.mongo;

import com.lambrk.domain.message.ChatConversation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatConversationRepository extends MongoRepository<ChatConversation, String> {

  /** Inbox — all conversations the user participates in, sorted by most recent message. */
  Page<ChatConversation> findByParticipantIdsContainingAndIsActiveTrueOrderByLastMessageAtDesc(
      UUID userId, Pageable pageable);

  /** Lookup by deterministic conversationId. */
  Optional<ChatConversation> findByConversationId(String conversationId);
}
