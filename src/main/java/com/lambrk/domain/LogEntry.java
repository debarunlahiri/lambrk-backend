package com.lambrk.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

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
public class LogEntry {

    @Id
    private UUID id;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "log_level", length = 20, nullable = false)
    private String logLevel;

    @Column(name = "method", length = 10, nullable = false)
    private String method;

    @Column(name = "endpoint", length = 500, nullable = false)
    private String endpoint;

    @Column(name = "full_url", length = 2000)
    private String fullUrl;

    @Column(name = "query_string", length = 2000)
    private String queryString;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_headers", columnDefinition = "TEXT")
    private String responseHeaders;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "is_authenticated", nullable = false)
    private boolean isAuthenticated;

    @Column(name = "exception_message", length = 1000)
    private String exceptionMessage;

    @Column(name = "exception_stack_trace", columnDefinition = "TEXT")
    private String exceptionStackTrace;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LogEntry() {}

    private LogEntry(UUID id, String correlationId, Instant timestamp, String logLevel, String method,
                     String endpoint, String fullUrl, String queryString, String requestHeaders,
                     String requestBody, String responseHeaders, String responseBody,
                     Integer statusCode, Long responseTimeMs, String ipAddress, String userAgent,
                     UUID userId, String username, boolean isAuthenticated, String exceptionMessage,
                     String exceptionStackTrace, String source, String serviceName, Instant createdAt) {
        this.id = id;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
        this.logLevel = logLevel;
        this.method = method;
        this.endpoint = endpoint;
        this.fullUrl = fullUrl;
        this.queryString = queryString;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.responseHeaders = responseHeaders;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.responseTimeMs = responseTimeMs;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.userId = userId;
        this.username = username;
        this.isAuthenticated = isAuthenticated;
        this.exceptionMessage = exceptionMessage;
        this.exceptionStackTrace = exceptionStackTrace;
        this.source = source;
        this.serviceName = serviceName;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getFullUrl() { return fullUrl; }
    public void setFullUrl(String fullUrl) { this.fullUrl = fullUrl; }
    public String getQueryString() { return queryString; }
    public void setQueryString(String queryString) { this.queryString = queryString; }
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public String getResponseHeaders() { return responseHeaders; }
    public void setResponseHeaders(String responseHeaders) { this.responseHeaders = responseHeaders; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isAuthenticated() { return isAuthenticated; }
    public void setAuthenticated(boolean authenticated) { isAuthenticated = authenticated; }
    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
    public String getExceptionStackTrace() { return exceptionStackTrace; }
    public void setExceptionStackTrace(String exceptionStackTrace) { this.exceptionStackTrace = exceptionStackTrace; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static LogEntryBuilder builder() {
        return new LogEntryBuilder();
    }

    public static class LogEntryBuilder {
        private UUID id = com.lambrk.util.UuidV7Generator.generate();
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
        private UUID userId;
        private String username;
        private boolean isAuthenticated;
        private String exceptionMessage;
        private String exceptionStackTrace;
        private String source = "API";
        private String serviceName = "lambrk-backend";
        private Instant createdAt = Instant.now();

        public LogEntryBuilder id(UUID id) {
            this.id = id;
            return this;
        }
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
        public LogEntryBuilder userId(UUID userId) {
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
            return new LogEntry(id, correlationId, timestamp, logLevel, method, endpoint, fullUrl,
                queryString, requestHeaders, requestBody, responseHeaders, responseBody,
                statusCode, responseTimeMs, ipAddress, userAgent, userId, username,
                isAuthenticated, exceptionMessage, exceptionStackTrace, source, serviceName, createdAt);
        }
    }
}
