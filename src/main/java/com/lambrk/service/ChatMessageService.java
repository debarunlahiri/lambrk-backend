package com.lambrk.service;

import com.lambrk.domain.User;
import com.lambrk.domain.message.ChatConversation;
import com.lambrk.domain.message.ChatMessage;
import com.lambrk.dto.message.ChatMessageResponse;
import com.lambrk.dto.message.ChatMessageWebSocketPayload;
import com.lambrk.dto.message.ConversationResponse;
import com.lambrk.dto.message.SendMessageRequest;
import com.lambrk.repository.UserRepository;
import com.lambrk.repository.mongo.ChatConversationRepository;
import com.lambrk.repository.mongo.ChatMessageRepository;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageService {

  private final ChatMessageRepository messageRepository;
  private final ChatConversationRepository conversationRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public ChatMessageService(
      ChatMessageRepository messageRepository,
      ChatConversationRepository conversationRepository,
      UserRepository userRepository,
      SimpMessagingTemplate messagingTemplate) {
    this.messageRepository = messageRepository;
    this.conversationRepository = conversationRepository;
    this.userRepository = userRepository;
    this.messagingTemplate = messagingTemplate;
  }

  // ──────────────────────────────────────────────
  // Send a message
  // ──────────────────────────────────────────────

  @Counted(value = "chat.messages.sent")
  @Timed(value = "chat.messages.send.duration")
  public ChatMessageResponse sendMessage(UUID senderId, SendMessageRequest request) {
    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + senderId));

    User recipient =
        userRepository
            .findByUsername(request.recipientUsername())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Recipient not found: " + request.recipientUsername()));

    if (senderId.equals(recipient.getId())) {
      throw new IllegalArgumentException("Cannot send message to yourself");
    }

    String conversationId = ChatConversation.buildConversationId(senderId, recipient.getId());

    // Ensure conversation doc exists
    ChatConversation conversation = getOrCreateConversation(conversationId, sender, recipient);

    // Persist message
    ChatMessage message =
        new ChatMessage(
            conversationId,
            senderId,
            sender.getUsername(),
            sender.getAvatarUrl(),
            recipient.getId(),
            recipient.getUsername(),
            request.content(),
            request.messageType(),
            request.attachmentUrl(),
            request.attachmentType());
    ChatMessage saved = messageRepository.save(message);

    // Update conversation metadata
    String preview = buildPreview(request.content(), request.messageType());
    conversation.setLastMessage(preview);
    conversation.setLastMessageSenderId(senderId);
    conversation.setLastMessageAt(
        saved.getCreatedAt() != null ? saved.getCreatedAt() : Instant.now());
    conversation.incrementUnreadCount(recipient.getId());
    conversationRepository.save(conversation);

    ChatMessageResponse response = ChatMessageResponse.from(saved);

    // Deliver via WebSocket
    deliverViaWebSocket(sender.getUsername(), recipient.getUsername(), response);

    return response;
  }

  // ──────────────────────────────────────────────
  // Inbox
  // ──────────────────────────────────────────────

  @Timed(value = "chat.conversations.list.duration")
  public Page<ConversationResponse> getConversations(UUID userId, Pageable pageable) {
    return conversationRepository
        .findByParticipantIdsContainingAndIsActiveTrueOrderByLastMessageAtDesc(userId, pageable)
        .map(conv -> ConversationResponse.from(conv, userId));
  }

  // ──────────────────────────────────────────────
  // Get or start a conversation with another user
  // ──────────────────────────────────────────────

  @Timed(value = "chat.conversation.open.duration")
  public ConversationResponse getOrStartConversation(UUID currentUserId, String targetUsername) {
    User currentUser =
        userRepository
            .findById(currentUserId)
            .orElseThrow(
                () -> new IllegalArgumentException("Current user not found: " + currentUserId));

    User targetUser =
        userRepository
            .findByUsername(targetUsername)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUsername));

    if (currentUserId.equals(targetUser.getId())) {
      throw new IllegalArgumentException("Cannot start a conversation with yourself");
    }

    String conversationId = ChatConversation.buildConversationId(currentUserId, targetUser.getId());
    ChatConversation conversation =
        getOrCreateConversation(conversationId, currentUser, targetUser);
    return ConversationResponse.from(conversation, currentUserId);
  }

  // ──────────────────────────────────────────────
  // Message history
  // ──────────────────────────────────────────────

  @Timed(value = "chat.messages.history.duration")
  public Page<ChatMessageResponse> getMessages(
      UUID userId, String conversationId, Pageable pageable) {
    validateParticipant(userId, conversationId);
    return messageRepository
        .findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(conversationId, pageable)
        .map(ChatMessageResponse::from);
  }

  // ──────────────────────────────────────────────
  // Mark as read
  // ──────────────────────────────────────────────

  @Counted(value = "chat.messages.read")
  public ChatMessageResponse markAsRead(UUID userId, String messageId) {
    ChatMessage message =
        messageRepository
            .findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

    if (!message.getRecipientId().equals(userId)) {
      throw new SecurityException("Not authorised to mark this message as read");
    }
    if (!message.isRead()) {
      message.setRead(true);
      message.setReadAt(Instant.now());
      messageRepository.save(message);

      // Reset unread count in conversation
      conversationRepository
          .findByConversationId(message.getConversationId())
          .ifPresent(
              conv -> {
                conv.resetUnreadCount(userId);
                conversationRepository.save(conv);
              });

      // Push read receipt to sender via WebSocket
      ChatMessageResponse response = ChatMessageResponse.from(message);
      String senderUsername = message.getSenderUsername();
      String recipientUsername = message.getRecipientUsername();
      ChatMessageWebSocketPayload receipt =
          ChatMessageWebSocketPayload.ofReadReceipt(
              messageId, message.getConversationId(), recipientUsername);
      messagingTemplate.convertAndSendToUser(senderUsername, "/queue/messages", receipt);
    }
    return ChatMessageResponse.from(message);
  }

  @Counted(value = "chat.messages.read.conversation")
  public int markConversationAsRead(UUID userId, String conversationId) {
    validateParticipant(userId, conversationId);

    List<ChatMessage> unreadMessages =
        messageRepository
            .findByConversationIdAndRecipientIdAndIsReadFalseAndIsDeletedFalseOrderByCreatedAtAsc(
                conversationId, userId);
    if (unreadMessages.isEmpty()) {
      return 0;
    }

    Instant readAt = Instant.now();
    for (ChatMessage message : unreadMessages) {
      message.setRead(true);
      message.setReadAt(readAt);
    }
    messageRepository.saveAll(unreadMessages);

    conversationRepository
        .findByConversationId(conversationId)
        .ifPresent(
            conv -> {
              conv.resetUnreadCount(userId);
              conversationRepository.save(conv);
            });

    for (ChatMessage message : unreadMessages) {
      ChatMessageWebSocketPayload receipt =
          ChatMessageWebSocketPayload.ofReadReceipt(
              message.getId(), conversationId, message.getRecipientUsername());
      messagingTemplate.convertAndSendToUser(
          message.getSenderUsername(), "/queue/messages", receipt);
    }

    return unreadMessages.size();
  }

  public void sendTypingIndicator(UUID userId, String conversationId) {
    ChatConversation conversation = validateParticipantAndGet(userId, conversationId);
    int currentUserIndex = conversation.getParticipantIds().indexOf(userId);
    int otherUserIndex = currentUserIndex == 0 ? 1 : 0;

    if (otherUserIndex < 0 || otherUserIndex >= conversation.getParticipantUsernames().size()) {
      return;
    }

    String typingUsername = conversation.getParticipantUsernames().get(currentUserIndex);
    String recipientUsername = conversation.getParticipantUsernames().get(otherUserIndex);
    ChatMessageWebSocketPayload payload =
        ChatMessageWebSocketPayload.ofTyping(conversationId, typingUsername);
    messagingTemplate.convertAndSendToUser(recipientUsername, "/queue/messages", payload);
  }

  // ──────────────────────────────────────────────
  // Soft delete
  // ──────────────────────────────────────────────

  @Counted(value = "chat.messages.deleted")
  public ChatMessageResponse deleteMessage(UUID userId, String messageId) {
    ChatMessage message =
        messageRepository
            .findById(messageId)
            .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

    if (!message.getSenderId().equals(userId)) {
      throw new SecurityException("Not authorised to delete this message");
    }
    message.setDeleted(true);
    message.setDeletedAt(Instant.now());
    messageRepository.save(message);

    ChatMessageResponse response = ChatMessageResponse.from(message);

    // Notify the other participant via WebSocket
    ChatMessageWebSocketPayload payload = ChatMessageWebSocketPayload.ofDeleted(response);
    messagingTemplate.convertAndSendToUser(
        message.getRecipientUsername(), "/queue/messages", payload);
    messagingTemplate.convertAndSendToUser(message.getSenderUsername(), "/queue/messages", payload);

    return response;
  }

  // ──────────────────────────────────────────────
  // Total unread count (for nav badge)
  // ──────────────────────────────────────────────

  public long getTotalUnreadCount(UUID userId) {
    return messageRepository.countByRecipientIdAndIsReadFalseAndIsDeletedFalse(userId);
  }

  // ──────────────────────────────────────────────
  // Internal helpers
  // ──────────────────────────────────────────────

  public ChatConversation getOrCreateConversation(
      String conversationId, User sender, User recipient) {
    return conversationRepository
        .findByConversationId(conversationId)
        .orElseGet(
            () -> {
              ChatConversation conv =
                  new ChatConversation(
                      conversationId,
                      sender.getId(),
                      sender.getUsername(),
                      recipient.getId(),
                      recipient.getUsername());
              return conversationRepository.save(conv);
            });
  }

  private void validateParticipant(UUID userId, String conversationId) {
    validateParticipantAndGet(userId, conversationId);
  }

  private ChatConversation validateParticipantAndGet(UUID userId, String conversationId) {
    ChatConversation conv =
        conversationRepository
            .findByConversationId(conversationId)
            .orElseThrow(
                () -> new IllegalArgumentException("Conversation not found: " + conversationId));
    if (!conv.getParticipantIds().contains(userId)) {
      throw new SecurityException("Not a participant in this conversation");
    }
    return conv;
  }

  private void deliverViaWebSocket(
      String senderUsername, String recipientUsername, ChatMessageResponse message) {
    ChatMessageWebSocketPayload payload = ChatMessageWebSocketPayload.ofNewMessage(message);
    messagingTemplate.convertAndSendToUser(recipientUsername, "/queue/messages", payload);
  }

  private String buildPreview(String content, ChatMessage.MessageType type) {
    if (type == null || type == ChatMessage.MessageType.TEXT) {
      return content != null && content.length() > 80 ? content.substring(0, 80) + "..." : content;
    }
    return switch (type) {
      case IMAGE -> "[Image]";
      case VIDEO -> "[Video]";
      case FILE -> "[File]";
      case SYSTEM -> content;
      case TEXT -> content;
    };
  }
}
