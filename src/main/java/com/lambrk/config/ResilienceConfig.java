package com.lambrk.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(MeterRegistry meterRegistry) {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordExceptions(Exception.class)
            .ignoreExceptions(IllegalArgumentException.class)
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(defaultConfig);
        
        // Register custom circuit breakers
        registry.circuitBreaker("postService", CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build());

        registry.circuitBreaker("commentService", CircuitBreakerConfig.custom()
            .failureRateThreshold(60)
            .waitDurationInOpenState(Duration.ofSeconds(45))
            .slidingWindowSize(20)
            .minimumNumberOfCalls(10)
            .permittedNumberOfCallsInHalfOpenState(5)
            .build());

        registry.circuitBreaker("userService", CircuitBreakerConfig.custom()
            .failureRateThreshold(40)
            .waitDurationInOpenState(Duration.ofSeconds(20))
            .slidingWindowSize(15)
            .minimumNumberOfCalls(8)
            .permittedNumberOfCallsInHalfOpenState(4)
            .build());

        registry.circuitBreaker("kafkaProducer", CircuitBreakerConfig.custom()
            .failureRateThreshold(70)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowSize(5)
            .minimumNumberOfCalls(3)
            .permittedNumberOfCallsInHalfOpenState(2)
            .build());

        // Register metrics for all circuit breakers
        registry.getAllCircuitBreakers().forEach(cb -> 
            io.github.resilience4j.micrometer.CircuitBreakerMetrics.of(cb).bindTo(meterRegistry));

        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry(MeterRegistry meterRegistry) {
        RetryConfig defaultConfig = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryExceptions(Exception.class)
            .ignoreExceptions(IllegalArgumentException.class)
            .build();

        RetryRegistry registry = RetryRegistry.of(defaultConfig);

        // Register custom retry configurations
        registry.retry("postService", RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .retryExceptions(org.springframework.dao.DataAccessException.class, 
                           org.springframework.web.client.ResourceAccessException.class)
            .ignoreExceptions(org.springframework.web.client.HttpClientErrorException.BadRequest.class)
            .build());

        registry.retry("commentService", RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(500))
            .retryExceptions(org.springframework.dao.DataAccessException.class)
            .build());

        registry.retry("userService", RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(2))
            .retryExceptions(org.springframework.dao.DataAccessException.class,
                           org.springframework.web.client.ResourceAccessException.class)
            .build());

        registry.retry("externalApi", RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofSeconds(2))
            .exponentialBackoffMultiplier(2)
            .retryExceptions(org.springframework.web.client.ResourceAccessException.class,
                           java.net.SocketTimeoutException.class)
            .build());

        // Register metrics for all retry instances
        registry.getAllRetries().forEach(retry -> 
            io.github.resilience4j.micrometer.RetryMetrics.of(retry).bindTo(meterRegistry));

        return registry;
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry(MeterRegistry meterRegistry) {
        RateLimiterConfig defaultConfig = RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(0))
            .build();

        RateLimiterRegistry registry = RateLimiterRegistry.of(defaultConfig);

        // Register custom rate limiters
        registry.rateLimiter("postCreation", RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(0))
            .build());

        registry.rateLimiter("commentCreation", RateLimiterConfig.custom()
            .limitForPeriod(500)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(0))
            .build());

        registry.rateLimiter("voteCasting", RateLimiterConfig.custom()
            .limitForPeriod(1000)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(0))
            .build());

        registry.rateLimiter("userRegistration", RateLimiterConfig.custom()
            .limitForPeriod(10)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(5))
            .build());

        registry.rateLimiter("search", RateLimiterConfig.custom()
            .limitForPeriod(50)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(2))
            .build());

        registry.rateLimiter("fileUpload", RateLimiterConfig.custom()
            .limitForPeriod(20)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(10))
            .build());

        // Register metrics for all rate limiters
        registry.getAllRateLimiters().forEach(rateLimiter -> 
            io.github.resilience4j.micrometer.RateLimiterMetrics.of(rateLimiter).bindTo(meterRegistry));

        return registry;
    }

    @Bean
    public BulkheadRegistry bulkheadRegistry(MeterRegistry meterRegistry) {
        BulkheadConfig defaultConfig = BulkheadConfig.custom()
            .maxConcurrentCalls(10)
            .maxWaitDuration(Duration.ofMillis(100))
            .build();

        BulkheadRegistry registry = BulkheadRegistry.of(defaultConfig);

        // Register custom bulkheads
        registry.bulkhead("postService", BulkheadConfig.custom()
            .maxConcurrentCalls(10)
            .maxWaitDuration(Duration.ofMillis(100))
            .build());

        registry.bulkhead("commentService", BulkheadConfig.custom()
            .maxConcurrentCalls(20)
            .maxWaitDuration(Duration.ofMillis(50))
            .build());

        registry.bulkhead("userService", BulkheadConfig.custom()
            .maxConcurrentCalls(15)
            .maxWaitDuration(Duration.ofMillis(75))
            .build());

        registry.bulkhead("fileProcessing", BulkheadConfig.custom()
            .maxConcurrentCalls(5)
            .maxWaitDuration(Duration.ofSeconds(5))
            .build());

        registry.bulkhead("emailSending", BulkheadConfig.custom()
            .maxConcurrentCalls(3)
            .maxWaitDuration(Duration.ofSeconds(10))
            .build());

        // Register metrics for all bulkheads
        registry.getAllBulkheads().forEach(bulkhead -> 
            io.github.resilience4j.micrometer.BulkheadMetrics.of(bulkhead).bindTo(meterRegistry));

        return registry;
    }

    @Bean
    public TimeLimiterConfig defaultTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10))
            .cancelRunningFuture(true)
            .build();
    }

    @Bean
    public TimeLimiter postServiceTimeLimiter() {
        return TimeLimiter.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .cancelRunningFuture(true)
            .build());
    }

    @Bean
    public TimeLimiter commentServiceTimeLimiter() {
        return TimeLimiter.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(3))
            .cancelRunningFuture(true)
            .build());
    }

    @Bean
    public TimeLimiter userServiceTimeLimiter() {
        return TimeLimiter.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(8))
            .cancelRunningFuture(true)
            .build());
    }

    @Bean
    public TimeLimiter externalApiTimeLimiter() {
        return TimeLimiter.of(TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(15))
            .cancelRunningFuture(true)
            .build());
    }
}
