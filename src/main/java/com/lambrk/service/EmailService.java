package com.lambrk.service;

import com.lambrk.domain.User;
import com.lambrk.dto.NotificationRequest;
import com.lambrk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;

    @Value("${spring.mail.from:noreply@reddit.com}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
                        UserRepository userRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
    }

    @Async
    public void sendNotificationEmail(Long userId, NotificationRequest notification) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.email() == null) {
            return;
        }

        try {
            String subject = buildSubject(notification);
            String content = buildContent(notification);

            sendHtmlEmail(user.email(), subject, content);
        } catch (Exception e) {
            // Log error but don't fail the notification
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }

    @Async
    public void sendWelcomeEmail(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.email() == null) {
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", user.username());
            context.setVariable("displayName", user.displayName());

            String content = templateEngine.process("welcome-email", context);
            sendHtmlEmail(user.email(), "Welcome to Reddit!", content);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String email, String resetToken) {
        try {
            Context context = new Context();
            context.setVariable("resetToken", resetToken);
            context.setVariable("resetUrl", "https://reddit.com/reset-password?token=" + resetToken);

            String content = templateEngine.process("password-reset", context);
            sendHtmlEmail(email, "Password Reset Request", content);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
        }
    }

    @Async
    public void sendWeeklyDigest(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.email() == null) {
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", user.username());
            // Add top posts, mentions, etc.

            String content = templateEngine.process("weekly-digest", context);
            sendHtmlEmail(user.email(), "Your Weekly Reddit Digest", content);
        } catch (Exception e) {
            System.err.println("Failed to send weekly digest: " + e.getMessage());
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    private void sendPlainTextEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    private String buildSubject(NotificationRequest notification) {
        return switch (notification.type()) {
            case COMMENT_REPLY -> "New reply to your comment";
            case POST_UPVOTE -> "Your post received an upvote";
            case COMMENT_UPVOTE -> "Your comment received an upvote";
            case POST_MENTION -> "You were mentioned in a post";
            case COMMENT_MENTION -> "You were mentioned in a comment";
            case SUBREDDIT_INVITE -> "You've been invited to moderate a subreddit";
            case MODERATOR_ACTION -> "Moderator action on your content";
            case SYSTEM_ANNOUNCEMENT -> "Important announcement from Reddit";
            case CONTENT_MODERATION -> "Content moderation notice";
        };
    }

    private String buildContent(NotificationRequest notification) {
        return "<html>" +
            "<body>" +
            "<h2>" + notification.title() + "</h2>" +
            "<p>" + notification.message() + "</p>" +
            (notification.actionUrl() != null ?
                "<p><a href=\"" + notification.actionUrl() + "\">" +
                (notification.actionText() != null ? notification.actionText() : "View") +
                "</a></p>" : "") +
            "<hr/>" +
            "<p style=\"font-size: 12px; color: #666;\">" +
            "You received this email because you have email notifications enabled. " +
            "<a href=\"https://reddit.com/settings/notifications\">Manage preferences</a>" +
            "</p>" +
            "</body>" +
            "</html>";
    }
}
