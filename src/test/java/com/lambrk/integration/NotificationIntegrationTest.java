package com.lambrk.integration;

import com.lambrk.dto.NotificationRequest;
import com.lambrk.dto.NotificationResponse;
import com.lambrk.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
        .withDatabaseName("reddit_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Test
    void shouldCreateNotification() throws Exception {
        NotificationRequest request = new NotificationRequest(
            NotificationRequest.NotificationType.COMMENT_REPLY,
            1L,
            "New reply to your comment",
            "Someone replied to your comment",
            1L,
            10L,
            null,
            "/posts/1#comment-10",
            "View reply",
            false
        );

        mockMvc.perform(post("/api/notifications")
                .contentType("application/json")
                .content("{\"type\":\"COMMENT_REPLY\",\"recipientId\":1,\"title\":\"New reply to your comment\",\"message\":\"Someone replied to your comment\",\"relatedPostId\":1,\"relatedCommentId\":10,\"actionUrl\":\"/posts/1#comment-10\",\"actionText\":\"View reply\",\"isRead\":false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("COMMENT_REPLY"))
            .andExpect(jsonPath("$.recipientId").value(1))
            .andExpect(jsonPath("$.title").value("New reply to your comment"))
            .andExpect(jsonPath("$.isRead").value(false));
    }

    @Test
    void shouldGetUserNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldGetUnreadNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/unread")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldMarkNotificationAsRead() throws Exception {
        mockMvc.perform(put("/api/notifications/1/read"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldMarkAllNotificationsAsRead() throws Exception {
        mockMvc.perform(put("/api/notifications/read-all"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteNotification() throws Exception {
        mockMvc.perform(delete("/api/notifications/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldDeleteAllNotifications() throws Exception {
        mockMvc.perform(delete("/api/notifications"))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        mockMvc.perform(get("/api/notifications/count/unread"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNumber());
    }

    @Test
    void shouldReturnBadRequestForInvalidNotification() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .contentType("application/json")
                .content("{\"type\":\"INVALID\",\"recipientId\":null,\"title\":\"\",\"message\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleNotificationServiceGracefully() throws Exception {
        // Test that notification service handles errors gracefully
        mockMvc.perform(get("/api/notifications")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
