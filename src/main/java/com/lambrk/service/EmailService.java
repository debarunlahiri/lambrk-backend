package com.lambrk.service;

import com.lambrk.dto.NotificationRequest;
import com.lambrk.repository.UserRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Profile({"dev"})
public class EmailService {

  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

  private final UserRepository userRepository;

  public EmailService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Async
  public void sendNotificationEmail(UUID userId, NotificationRequest notification) {
    logger.info("Email notification skipped in dev mode for user: {}", userId);
  }

  @Async
  public void sendWelcomeEmail(UUID userId) {
    logger.info("Welcome email skipped in dev mode for user: {}", userId);
  }

  @Async
  public void sendPasswordResetEmail(String email, String resetToken) {
    logger.info("Password reset email skipped in dev mode for email: {}", email);
  }

  @Async
  public void sendWeeklyDigest(UUID userId) {
    logger.info("Weekly digest skipped in dev mode for user: {}", userId);
  }
}
