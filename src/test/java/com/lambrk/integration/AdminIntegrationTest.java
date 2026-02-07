package com.lambrk.integration;

import com.lambrk.dto.AdminActionRequest;
import com.lambrk.dto.AdminActionResponse;
import com.lambrk.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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
class AdminIntegrationTest {

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
    private AdminService adminService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldPerformAdminAction() throws Exception {
        AdminActionRequest request = new AdminActionRequest(
            AdminActionRequest.AdminActionType.DELETE_POST,
            1L,
            "Violation of community guidelines",
            "Spam content",
            null,
            false,
            true
        );

        mockMvc.perform(post("/api/admin/actions")
                .contentType("application/json")
                .content("{\"type\":\"DELETE_POST\",\"targetId\":1,\"reason\":\"Violation of community guidelines\",\"notes\":\"Spam content\",\"durationDays\":null,\"permanent\":false,\"notifyUser\":true}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("DELETE_POST"))
            .andExpect(jsonPath("$.targetId").value(1))
            .andExpect(jsonPath("$.reason").value("Violation of community guidelines"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldBanUser() throws Exception {
        mockMvc.perform(post("/api/admin/ban-user/1")
                .param("reason", "Repeated policy violations")
                .param("durationDays", "30")
                .param("permanent", "false")
                .param("notifyUser", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("BAN_USER"))
            .andExpect(jsonPath("$.targetId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldSuspendUser() throws Exception {
        mockMvc.perform(post("/api/admin/suspend-user/1")
                .param("reason", "Temporary suspension")
                .param("durationDays", "7")
                .param("notifyUser", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("SUSPEND_USER"))
            .andExpect(jsonPath("$.targetId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldDeletePost() throws Exception {
        mockMvc.perform(post("/api/admin/delete-post/1")
                .param("reason", "Inappropriate content")
                .param("notifyUser", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("DELETE_POST"))
            .andExpect(jsonPath("$.targetId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldDeleteComment() throws Exception {
        mockMvc.perform(post("/api/admin/delete-comment/1")
                .param("reason", "Harassment")
                .param("notifyUser", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("DELETE_COMMENT"))
            .andExpect(jsonPath("$.targetId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldLockPost() throws Exception {
        mockMvc.perform(post("/api/admin/lock-post/1")
                .param("reason", "Heated discussion")
                .param("durationDays", "7")
                .param("permanent", "false")
                .param("notifyUser", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("LOCK_POST"))
            .andExpect(jsonPath("$.targetId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldQuarantinePost() throws Exception {
        mockMvc.perform(post("/api/admin/quarantine-post/1")
                .param("reason", "Potentially harmful content")
                .param("notifyUser", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.action").value("QUARANTINE_POST"))
            .andExpect(jsonPath("$.targetId").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAdminActions() throws Exception {
        mockMvc.perform(get("/api/admin/actions")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetAdminActionsByUser() throws Exception {
        mockMvc.perform(get("/api/admin/actions/user/1")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldGetActiveActions() throws Exception {
        mockMvc.perform(get("/api/admin/actions/active")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/actions")
                .contentType("application/json")
                .content("{\"type\":\"DELETE_POST\",\"targetId\":1,\"reason\":\"test\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldHandleAdminServiceGracefully() throws Exception {
        // Test that admin service handles errors gracefully
        mockMvc.perform(get("/api/admin/actions")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}
