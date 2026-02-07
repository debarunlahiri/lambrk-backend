package com.lambrk.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(NativeImageConfig.RuntimeHintsRegistrar.class)
public class NativeImageConfig {

    static class RuntimeHintsRegistrar implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // Register reflection hints for JPA entities
            hints.reflection()
                .registerType(TypeReference.of("com.lambrk.domain.User"))
                .registerType(TypeReference.of("com.lambrk.domain.Post"))
                .registerType(TypeReference.of("com.lambrk.domain.Comment"))
                .registerType(TypeReference.of("com.lambrk.domain.Subreddit"))
                .registerType(TypeReference.of("com.lambrk.domain.Vote"));

            // Register reflection hints for DTOs
            hints.reflection()
                .registerType(TypeReference.of("com.lambrk.dto.PostCreateRequest"))
                .registerType(TypeReference.of("com.lambrk.dto.PostResponse"))
                .registerType(TypeReference.of("com.lambrk.dto.UserResponse"))
                .registerType(TypeReference.of("com.lambrk.dto.SubredditResponse"));

            // Register reflection hints for Spring Security
            hints.reflection()
                .registerType(TypeReference.of("org.springframework.security.core.userdetails.User"))
                .registerType(TypeReference.of("org.springframework.security.authentication.UsernamePasswordAuthenticationToken"));

            // Register reflection hints for Jackson
            hints.reflection()
                .registerType(TypeReference.of("com.fasterxml.jackson.databind.ObjectMapper"))
                .registerType(TypeReference.of("com.fasterxml.jackson.databind.JsonNode"));

            // Register reflection hints for Hibernate
            hints.reflection()
                .registerType(TypeReference.of("org.hibernate.internal.SessionFactoryImpl"))
                .registerType(TypeReference.of("org.hibernate.engine.spi.SessionImplementor"));

            // Register reflection hints for PostgreSQL driver
            hints.reflection()
                .registerType(TypeReference.of("org.postgresql.Driver"))
                .registerType(TypeReference.of("org.postgresql.util.PGobject"));

            // Register reflection hints for Redis
            hints.reflection()
                .registerType(TypeReference.of("org.springframework.data.redis.connection.RedisConnectionFactory"))
                .registerType(TypeReference.of("org.springframework.data.redis.core.RedisTemplate"));

            // Register reflection hints for Kafka
            hints.reflection()
                .registerType(TypeReference.of("org.apache.kafka.clients.producer.Producer"))
                .registerType(TypeReference.of("org.apache.kafka.clients.consumer.Consumer"));

            // Register reflection hints for Spring AI
            hints.reflection()
                .registerType(TypeReference.of("org.springframework.ai.chat.client.ChatClient"))
                .registerType(TypeReference.of("org.springframework.ai.openai.OpenAiChatModel"));

            // Register resource hints for configuration files
            hints.resources()
                .registerPattern("application*.yml")
                .registerPattern("application*.properties")
                .registerPattern("*.sql")
                .registerPattern("META-INF/**")
                .registerPattern("db/migration/**");

            // Register JNI hints for native libraries
            hints.jni()
                .registerType(TypeReference.of("java.sql.Connection"))
                .registerType(TypeReference.of("java.sql.Driver"));

            // Register serialization hints
            hints.serialization()
                .registerType(TypeReference.of("com.lambrk.domain.User"))
                .registerType(TypeReference.of("com.lambrk.domain.Post"))
                .registerType(TypeReference.of("com.lambrk.domain.Comment"))
                .registerType(TypeReference.of("com.lambrk.domain.Subreddit"))
                .registerType(TypeReference.of("com.lambrk.domain.Vote"));

            // Register method hints for JPA repositories
            hints.reflection()
                .registerType(TypeReference.of("com.lambrk.repository.UserRepository"), 
                    MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("com.lambrk.repository.PostRepository"), 
                    MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("com.lambrk.repository.CommentRepository"), 
                    MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("com.lambrk.repository.SubredditRepository"), 
                    MemberCategory.INVOKE_DECLARED_METHODS)
                .registerType(TypeReference.of("com.lambrk.repository.VoteRepository"), 
                    MemberCategory.INVOKE_DECLARED_METHODS);

            // Register hints for Spring Boot auto-configuration
            hints.reflection()
                .registerType(TypeReference.of("org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"))
                .registerType(TypeReference.of("org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"))
                .registerType(TypeReference.of("org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"));

            // Register hints for Micrometer and OpenTelemetry
            hints.reflection()
                .registerType(TypeReference.of("io.micrometer.core.instrument.MeterRegistry"))
                .registerType(TypeReference.of("io.micrometer.tracing.Tracer"))
                .registerType(TypeReference.of("io.opentelemetry.api.OpenTelemetry"));

            // Register hints for Resilience4j
            hints.reflection()
                .registerType(TypeReference.of("io.github.resilience4j.circuitbreaker.CircuitBreaker"))
                .registerType(TypeReference.of("io.github.resilience4j.retry.Retry"))
                .registerType(TypeReference.of("io.github.resilience4j.ratelimiter.RateLimiter"))
                .registerType(TypeReference.of("io.github.resilience4j.bulkhead.Bulkhead"));

            // Register hints for Caffeine cache
            hints.reflection()
                .registerType(TypeReference.of("com.github.benmanes.caffeine.cache.Cache"))
                .registerType(TypeReference.of("com.github.benmanes.caffeine.cache.Caffeine"));

            // Register hints for virtual threads (Java 25)
            hints.reflection()
                .registerType(TypeReference.of("java.lang.VirtualThread"))
                .registerType(TypeReference.of("java.util.concurrent.StructuredTaskScope"));
        }
    }
}
