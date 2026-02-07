package com.lambrk.integration;

import com.lambrk.dto.SearchRequest;
import com.lambrk.dto.SearchResponse;
import com.lambrk.service.SearchService;
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
class SearchIntegrationTest {

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
    private SearchService searchService;

    @Test
    void shouldSearchAllContent() throws Exception {
        SearchRequest request = new SearchRequest(
            "spring boot",
            SearchRequest.SearchType.ALL,
            SearchRequest.SortBy.RELEVANCE,
            SearchRequest.TimeFilter.ALL,
            List.of(),
            List.of(),
            false,
            false,
            null,
            null,
            null,
            0,
            20
        );

        mockMvc.perform(post("/api/search")
                .contentType("application/json")
                .content("{\"query\":\"spring boot\",\"type\":\"ALL\",\"sort\":\"RELEVANCE\",\"timeFilter\":\"ALL\",\"page\":0,\"size\":20}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metadata.query").value("spring boot"))
            .andExpect(jsonPath("$.metadata.type").value("ALL"))
            .andExpect(jsonPath("$.metadata.totalResults").isNumber());
    }

    @Test
    void shouldSearchPostsOnly() throws Exception {
        mockMvc.perform(get("/api/search/posts")
                .param("query", "java")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void shouldSearchWithFilters() throws Exception {
        mockMvc.perform(get("/api/search/posts")
                .param("query", "programming")
                .param("subreddits", "programming,java")
                .param("flairs", "tutorial,guide")
                .param("minScore", "10")
                .param("includeNSFW", "false"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldSearchComments() throws Exception {
        mockMvc.perform(get("/api/search/comments")
                .param("query", "help")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.comments").isArray());
    }

    @Test
    void shouldSearchUsers() throws Exception {
        mockMvc.perform(get("/api/search/users")
                .param("query", "john")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users").isArray());
    }

    @Test
    void shouldSearchSubreddits() throws Exception {
        mockMvc.perform(get("/api/search/subreddits")
                .param("query", "technology")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.subreddits").isArray());
    }

    @Test
    void shouldGetSearchSuggestions() throws Exception {
        mockMvc.perform(get("/api/search/suggestions")
                .param("query", "spring")
                .param("type", "posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetTrendingSearches() throws Exception {
        mockMvc.perform(get("/api/search/trending")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.metadata.suggestions").isArray());
    }

    @Test
    void shouldReturnBadRequestForInvalidQuery() throws Exception {
        mockMvc.perform(post("/api/search")
                .contentType("application/json")
                .content("{\"query\":\"\",\"type\":\"ALL\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void handleSearchServiceGracefully() throws Exception {
        // Test that search service handles errors gracefully
        mockMvc.perform(get("/api/search/posts")
                .param("query", "nonexistent")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts").isArray());
    }
}
