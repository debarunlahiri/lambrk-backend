package com.lambrk.service;

import com.lambrk.domain.User;
import com.lambrk.dto.NotificationRequest;
import com.lambrk.repository.UserRepository;
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
    public void sendNotificationEmail(Long userId, NotificationRequest notification) {
        logger.info("Email notification skipped in dev mode for user: {}", userId);
    }

    @Async
    public void sendWelcomeEmail(Long userId) {
        logger.info("Welcome email skipped in dev mode for user: {}", userId);
    }

    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        logger.info("Password reset email skipped in dev mode for email: {}", email);
    }

    @Async
    public void sendWeeklyDigest(Long userId) {
        logger.info("Weekly digest skipped in dev mode for user: {}", userId);
    }
}