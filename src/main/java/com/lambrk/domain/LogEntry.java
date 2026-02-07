package com.lambrk.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "api_logs", indexes = {
    @Index(name = "idx_api_logs_timestamp", columnList = "timestamp"),
    @Index(name = "idx_api_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_api_logs_method", columnList = "method"),
    @Index(name = "idx_api_logs_endpoint", columnList = "endpoint"),
    @Index(name = "idx_api_logs_status_code", columnList = "status_code"),
    @Index(name = "idx_api_logs_ip_address", columnList = "ip_address"),
    @Index(name = "idx_api_logs_log_level", columnList = "log_level"),
    @Index(name = "idx_api_logs_correlation_id", columnList = "correlation_id"),
    @Index(name = "idx_api_logs_timestamp_endpoint", columnList = "timestamp, endpoint")
})
@EntityListeners(AuditingEntityListener.class)
public record LogEntry(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,

    @Column(name = "correlation_id", length = 100)
    String correlationId,

    @Column(name = "timestamp", nullable = false)
    Instant timestamp,

    @Column(name = "log_level", length = 20, nullable = false)
    String logLevel,

    @Column(name = "method", length = 10, nullable = false)
    String method,

    @Column(name = "endpoint", length = 500, nullable = false)
    String endpoint,

    @Column(name = "full_url", length = 2000)
    String fullUrl,

    @Column(name = "query_string", length = 2000)
    String queryString,

    @Column(name = "request_headers", columnDefinition = "TEXT")
    String requestHeaders,

    @Column(name = "request_body", columnDefinition = "TEXT")
    String requestBody,

    @Column(name = "response_headers", columnDefinition = "TEXT")
    String responseHeaders,

    @Column(name = "response_body", columnDefinition = "TEXT")
    String responseBody,

    @Column(name = "status_code")
    Integer statusCode,

    @Column(name = "response_time_ms")
    Long responseTimeMs,

    @Column(name = "ip_address", length = 45)
    String ipAddress,

    @Column(name = "user_agent", length = 500)
    String userAgent,

    @Column(name = "user_id")
    Long userId,

    @Column(name = "username", length = 100)
    String username,

    @Column(name = "is_authenticated", nullable = false)
    boolean isAuthenticated,

    @Column(name = "exception_message", length = 1000)
    String exceptionMessage,

    @Column(name = "exception_stack_trace", columnDefinition = "TEXT")
    String exceptionStackTrace,

    @Column(name = "source", length = 50)
    String source,

    @Column(name = "service_name", length = 100)
    String serviceName,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt
) {

    public LogEntry(String correlationId, Instant timestamp, String logLevel, String method,
                    String endpoint, String fullUrl, String queryString, String requestHeaders,
                    String requestBody, String responseHeaders, String responseBody,
                    Integer statusCode, Long responseTimeMs, String ipAddress, String userAgent,
                    Long userId, String username, boolean isAuthenticated, String exceptionMessage,
                    String exceptionStackTrace, String source, String serviceName, Instant createdAt) {
        this(null, correlationId, timestamp, logLevel, method, endpoint, fullUrl, queryString,
             requestHeaders, requestBody, responseHeaders, responseBody, statusCode, responseTimeMs,
             ipAddress, userAgent, userId, username, isAuthenticated, exceptionMessage,
             exceptionStackTrace, source, serviceName, createdAt);
    }

    public LogEntry withResponseDetails(String responseHeaders, String responseBody,
                                        Integer statusCode, Long responseTimeMs,
                                        String exceptionMessage, String exceptionStackTrace) {
        return new LogEntry(
            id, correlationId, timestamp, logLevel, method, endpoint, fullUrl, queryString,
            requestHeaders, requestBody, responseHeaders, responseBody, statusCode,
            responseTimeMs, ipAddress, userAgent, userId, username, isAuthenticated,
            exceptionMessage, exceptionStackTrace, source, serviceName, createdAt
        );
    }

    public static LogEntryBuilder builder() {
        return new LogEntryBuilder();
    }

    public static class LogEntryBuilder {
        private String correlationId;
        private Instant timestamp = Instant.now();
        private String logLevel = "INFO";
        private String method;
        private String endpoint;
        private String fullUrl;
        private String queryString;
        private String requestHeaders;
        private String requestBody;
        private String responseHeaders;
        private String responseBody;
        private Integer statusCode;
        private Long responseTimeMs;
        private String ipAddress;
        private String userAgent;
        private Long userId;
        private String username;
        private boolean isAuthenticated;
        private String exceptionMessage;
        private String exceptionStackTrace;
        private String source = "API";
        private String serviceName = "reddit-backend";
        private Instant createdAt = Instant.now();

        public LogEntryBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public LogEntryBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public LogEntryBuilder logLevel(String logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public LogEntryBuilder method(String method) {
            this.method = method;
            return this;
        }

        public LogEntryBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public LogEntryBuilder fullUrl(String fullUrl) {
            this.fullUrl = fullUrl;
            return this;
        }

        public LogEntryBuilder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public LogEntryBuilder requestHeaders(String requestHeaders) {
            this.requestHeaders = requestHeaders;
            return this;
        }

        public LogEntryBuilder requestBody(String requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public LogEntryBuilder responseHeaders(String responseHeaders) {
            this.responseHeaders = responseHeaders;
            return this;
        }

        public LogEntryBuilder responseBody(String responseBody) {
            this.responseBody = responseBody;
            return this;
        }

        public LogEntryBuilder statusCode(Integer statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public LogEntryBuilder responseTimeMs(Long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }

        public LogEntryBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public LogEntryBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public LogEntryBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public LogEntryBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LogEntryBuilder isAuthenticated(boolean isAuthenticated) {
            this.isAuthenticated = isAuthenticated;
            return this;
        }

        public LogEntryBuilder exceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
            return this;
        }

        public LogEntryBuilder exceptionStackTrace(String exceptionStackTrace) {
            this.exceptionStackTrace = exceptionStackTrace;
            return this;
        }

        public LogEntryBuilder source(String source) {
            this.source = source;
            return this;
        }

        public LogEntryBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public LogEntry build() {
            return new LogEntry(correlationId, timestamp, logLevel, method, endpoint, fullUrl,
                queryString, requestHeaders, requestBody, responseHeaders, responseBody,
                statusCode, responseTimeMs, ipAddress, userAgent, userId, username,
                isAuthenticated, exceptionMessage, exceptionStackTrace, source, serviceName, createdAt);
        }
    }
}
