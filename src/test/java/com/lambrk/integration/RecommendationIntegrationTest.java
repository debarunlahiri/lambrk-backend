package com.lambrk.integration;

import com.lambrk.dto.RecommendationRequest;
import com.lambrk.dto.RecommendationResponse;
import com.lambrk.service.RecommendationService;
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
class RecommendationIntegrationTest {

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
    private RecommendationService recommendationService;

    @Test
    void shouldGetPostRecommendations() throws Exception {
        mockMvc.perform(get("/api/recommendations/posts/1")
                .param("limit", "10")
                .param("includeNSFW", "false")
                .param("includeOver18", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("POSTS"))
            .andExpect(jsonPath("$.posts").isArray())
            .andExpect(jsonPath("$.explanation").isString())
            .andExpect(jsonPath("$.confidence").isNumber())
            .andExpect(jsonPath("$.factors").isArray());
    }

    @Test
    void shouldGetSubredditRecommendations() throws Exception {
        mockMvc.perform(get("/api/recommendations/subreddits/1")
                .param("limit", "10")
                .param("includeNSFW", "false")
                .param("includeOver18", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("SUBREDDITS"))
            .andExpect(jsonPath("$.subreddits").isArray())
            .andExpect(jsonPath("$.explanation").isString())
            .andExpect(jsonPath("$.confidence").isNumber())
            .andExpect(jsonPath("$.factors").isArray());
    }

    @Test
    void shouldGetUserRecommendations() throws Exception {
        mockMvc.perform(get("/api/recommendations/users/1")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("USERS"))
            .andExpect(jsonPath("$.users").isArray())
            .andExpect(jsonPath("$.explanation").isString())
            .andExpect(jsonPath("$.confidence").isNumber())
            .andExpect(jsonPath("$.factors").isArray());
    }

    @Test
    void shouldGetCommentRecommendations() throws Exception {
        mockMvc.perform(get("/api/recommendations/comments/1")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("COMMENTS"))
            .andExpect(jsonPath("$.comments").isArray())
            .andExpect(jsonPath("$.explanation").isString())
            .andExpect(jsonPath("$.confidence").isNumber())
            .andExpect(jsonPath("$.factors").isArray());
    }

    @Test
    void shouldGetContextualRecommendations() throws Exception {
        mockMvc.perform(get("/api/recommendations/context/1")
                .param("contextSubredditId", "1")
                .param("contextPostId", "1")
                .param("type", "posts")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("POSTS"))
            .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void shouldGetTrendingRecommendations() throws Exception {
        mockMvc.perform(get("/api/recommendations/trending")
                .param("type", "posts")
                .param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("POSTS"))
            .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void shouldCreateRecommendationRequest() throws Exception {
        RecommendationRequest request = new RecommendationRequest(
            1L,
            RecommendationRequest.RecommendationType.POSTS,
            10,
            List.of(),
            List.of(),
            false,
            false,
            null,
            null
        );

        mockMvc.perform(post("/api/recommendations")
                .contentType("application/json")
                .content("{\"userId\":1,\"type\":\"POSTS\",\"limit\":10,\"excludeSubreddits\":[],\"excludeUsers\":[],\"includeNSFW\":false,\"includeOver18\":false,\"contextSubredditId\":null,\"contextPostId\":null}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("POSTS"))
            .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void shouldGetRecommendationsWithExclusions() throws Exception {
        mockMvc.perform(get("/api/recommendations/posts/1")
                .param("limit", "10")
                .param("excludeSubreddits", "programming,gaming")
                .param("excludeUsers", "user1,user2")
                .param("includeNSFW", "false")
                .param("includeOver18", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void shouldHandleRecommendationServiceGracefully() throws Exception {
        // Test that recommendation service handles errors gracefully
        mockMvc.perform(get("/api/recommendations/posts/999")
                .param("limit", "10")
                .param("includeNSFW", "false")
                .param("includeOver18", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void shouldValidateRecommendationRequest() throws Exception {
        mockMvc.perform(post("/api/recommendations")
                .contentType("application/json")
                .content("{\"userId\":null,\"type\":\"POSTS\"}"))
            .andExpect(status().isBadRequest());
    }
}
