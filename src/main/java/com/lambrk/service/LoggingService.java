package com.lambrk.service;

import com.lambrk.domain.LogEntry;
import com.lambrk.repository.LogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
    private static final Logger fileLogger = LoggerFactory.getLogger("API_LOGGER");
    private static final String LOG_FORMAT = "[{}] {} {} {} {} {}ms {} {} {}";
    private static final String TRUNCATED = "...[truncated]";
    private static final String REDACTED = "***REDACTED***";
    private static final String ANONYMOUS = "anonymous";
    private static final String ERROR_PREFIX = "ERROR: ";

    private final LogRepository logRepository;
    private final ObjectMapper objectMapper;
    private final boolean loggingEnabled;
    private final boolean logRequestBody;
    private final boolean logResponseBody;
    private final int maxBodySize;
    private final Set<String> sensitiveHeaders;

    public LoggingService(
            LogRepository logRepository,
            ObjectMapper objectMapper,
            @Value("${app.logging.enabled:true}") boolean loggingEnabled,
            @Value("${app.logging.log-request-body:true}") boolean logRequestBody,
            @Value("${app.logging.log-response-body:true}") boolean logResponseBody,
            @Value("${app.logging.max-body-size:10000}") int maxBodySize,
            @Value("${app.logging.sensitive-headers:authorization,cookie,x-api-key}") String[] sensitiveHeaders) {
        this.logRepository = logRepository;
        this.objectMapper = objectMapper;
        this.loggingEnabled = loggingEnabled;
        this.logRequestBody = logRequestBody;
        this.logResponseBody = logResponseBody;
        this.maxBodySize = maxBodySize;
        this.sensitiveHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.sensitiveHeaders.addAll(Arrays.asList(sensitiveHeaders));
    }

    public record LogContext(
        String method,
        String endpoint,
        String fullUrl,
        String queryString,
        String requestHeaders,
        String requestBody,
        String responseHeaders,
        String responseBody,
        int statusCode,
        Long responseTimeMs,
        String ipAddress,
        String userAgent,
        String correlationId,
        UUID userId,
        String username,
        boolean isAuthenticated,
        String exceptionMessage,
        String exceptionStackTrace
    ) {}

    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequestResponse(LogContext context) {
        if (!loggingEnabled) {
            return;
        }

        try {
            LogEntry logEntry = LogEntry.builder()
                .correlationId(context.correlationId())
                .timestamp(Instant.now())
                .logLevel(determineLogLevel(context.statusCode(), context.exceptionMessage()))
                .method(context.method())
                .endpoint(context.endpoint())
                .fullUrl(context.fullUrl())
                .queryString(context.queryString())
                .requestHeaders(context.requestHeaders())
                .requestBody(context.requestBody())
                .responseHeaders(context.responseHeaders())
                .responseBody(context.responseBody())
                .statusCode(context.statusCode())
                .responseTimeMs(context.responseTimeMs())
                .ipAddress(context.ipAddress())
                .userAgent(context.userAgent())
                .userId(context.userId())
                .username(context.username())
                .isAuthenticated(context.isAuthenticated())
                .exceptionMessage(context.exceptionMessage())
                .exceptionStackTrace(context.exceptionStackTrace())
                .source("API")
                .serviceName("lambrk-backend")
                .build();

            // Save to database asynchronously
            logRepository.save(logEntry);

            // Also log to file
            logToFile(logEntry);

        } catch (Exception e) {
            logger.error("Failed to log request/response", e);
        }
    }

    private void logToFile(LogEntry entry) {
        try {
            String exceptionPart = entry.getExceptionMessage() != null ? ERROR_PREFIX + entry.getExceptionMessage() : "";
            fileLogger.info(LOG_FORMAT,
                entry.getTimestamp(),
                entry.getMethod(),
                entry.getEndpoint(),
                entry.getCorrelationId(),
                entry.getStatusCode(),
                entry.getResponseTimeMs(),
                entry.getIpAddress(),
                entry.isAuthenticated() ? entry.getUsername() : ANONYMOUS,
                exceptionPart
            );

            if (logger.isDebugEnabled()) {
                logger.debug("API Log Entry: {}", objectMapper.writeValueAsString(entry));
            }
        } catch (Exception e) {
            logger.error("Failed to write to file logger", e);
        }
    }

    private String determineLogLevel(int statusCode, String exceptionMessage) {
        if (exceptionMessage != null) {
            return "ERROR";
        }
        if (statusCode >= 500) {
            return "ERROR";
        } else if (statusCode >= 400) {
            return "WARN";
        }
        return "INFO";
    }

    private String truncateIfNeeded(String body) {
        if (body == null) return null;
        if (body.length() > maxBodySize) {
            return body.substring(0, maxBodySize) + TRUNCATED;
        }
        return body;
    }

    // Public API methods for querying logs

    public Page<LogEntry> getAllLogs(Pageable pageable) {
        return logRepository.findByOrderByTimestampDesc(pageable);
    }

    public Page<LogEntry> getLogsByUser(UUID userId, Pageable pageable) {
        return logRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    public Page<LogEntry> getLogsByEndpoint(String endpoint, Pageable pageable) {
        return logRepository.findByEndpointContainingIgnoreCaseOrderByTimestampDesc(endpoint, pageable);
    }

    public Page<LogEntry> getLogsByMethod(String method, Pageable pageable) {
        return logRepository.findByMethodOrderByTimestampDesc(method.toUpperCase(), pageable);
    }

    public Page<LogEntry> getLogsByStatusCode(Integer statusCode, Pageable pageable) {
        return logRepository.findByStatusCodeOrderByTimestampDesc(statusCode, pageable);
    }

    public Page<LogEntry> getErrorLogs(Pageable pageable) {
        return logRepository.findErrors(pageable);
    }

    public Page<LogEntry> getExceptionLogs(Pageable pageable) {
        return logRepository.findExceptions(pageable);
    }

    public Page<LogEntry> getAnonymousLogs(Pageable pageable) {
        return logRepository.findAnonymousRequests(pageable);
    }

    public Page<LogEntry> getAuthenticatedLogs(Pageable pageable) {
        return logRepository.findAuthenticatedRequests(pageable);
    }
}
