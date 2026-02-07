package com.lambrk.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title("Reddit Backend API")
                .version("1.0.0")
                .description("Production-grade Reddit-like backend API with Spring Boot 3.5 and Java 25")
                .contact(new Contact()
                    .name("API Support")
                    .email("api@reddit.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/**")
            .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("auth")
            .pathsToMatch("/api/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi postsApi() {
        return GroupedOpenApi.builder()
            .group("posts")
            .pathsToMatch("/api/posts/**")
            .build();
    }

    @Bean
    public GroupedOpenApi commentsApi() {
        return GroupedOpenApi.builder()
            .group("comments")
            .pathsToMatch("/api/comments/**")
            .build();
    }

    @Bean
    public GroupedOpenApi usersApi() {
        return GroupedOpenApi.builder()
            .group("users")
            .pathsToMatch("/api/users/**")
            .build();
    }

    @Bean
    public GroupedOpenApi subredditsApi() {
        return GroupedOpenApi.builder()
            .group("subreddits")
            .pathsToMatch("/api/subreddits/**")
            .build();
    }

    @Bean
    public GroupedOpenApi votesApi() {
        return GroupedOpenApi.builder()
            .group("votes")
            .pathsToMatch("/api/votes/**")
            .build();
    }

    @Bean
    public GroupedOpenApi searchApi() {
        return GroupedOpenApi.builder()
            .group("search")
            .pathsToMatch("/api/search/**")
            .build();
    }

    @Bean
    public GroupedOpenApi notificationsApi() {
        return GroupedOpenApi.builder()
            .group("notifications")
            .pathsToMatch("/api/notifications/**")
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/admin/**")
            .build();
    }

    @Bean
    public GroupedOpenApi recommendationsApi() {
        return GroupedOpenApi.builder()
            .group("recommendations")
            .pathsToMatch("/api/recommendations/**")
            .build();
    }

    @Bean
    public GroupedOpenApi filesApi() {
        return GroupedOpenApi.builder()
            .group("files")
            .pathsToMatch("/api/files/**")
            .build();
    }

    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
            .group("actuator")
            .pathsToMatch("/actuator/**")
            .build();
    }
}
