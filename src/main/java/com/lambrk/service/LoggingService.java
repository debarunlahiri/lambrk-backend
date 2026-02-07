package com.lambrk.service;

import com.lambrk.domain.LogEntry;
import com.lambrk.repository.LogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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

    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRequestResponse(HttpServletRequest request, HttpServletResponse response,
                                   ContentCachingRequestWrapper requestWrapper,
                                   ContentCachingResponseWrapper responseWrapper,
                                   Long userId, String username, boolean isAuthenticated,
                                   Long startTime, Throwable exception) {
        if (!loggingEnabled) {
            return;
        }

        try {
            LogEntry logEntry = buildLogEntry(request, response, requestWrapper, responseWrapper,
                userId, username, isAuthenticated, startTime, exception);

            // Save to database asynchronously
            logRepository.save(logEntry);

            // Also log to file
            logToFile(logEntry);

        } catch (Exception e) {
            logger.error("Failed to log request/response", e);
        }
    }

    private LogEntry buildLogEntry(HttpServletRequest request, HttpServletResponse response,
                                   ContentCachingRequestWrapper requestWrapper,
                                   ContentCachingResponseWrapper responseWrapper,
                                   Long userId, String username, boolean isAuthenticated,
                                   Long startTime, Throwable exception) {

        Long responseTimeMs = System.currentTimeMillis() - startTime;
        String correlationId = getCorrelationId(request);

        return LogEntry.builder()
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .logLevel(determineLogLevel(response, exception))
            .method(request.getMethod())
            .endpoint(request.getRequestURI())
            .fullUrl(request.getRequestURL().toString())
            .queryString(request.getQueryString())
            .requestHeaders(getHeadersAsString(request))
            .requestBody(logRequestBody ? getRequestBody(requestWrapper) : null)
            .responseHeaders(getResponseHeadersAsString(response))
            .responseBody(logResponseBody ? getResponseBody(responseWrapper) : null)
            .statusCode(response.getStatus())
            .responseTimeMs(responseTimeMs)
            .ipAddress(getClientIpAddress(request))
            .userAgent(request.getHeader("User-Agent"))
            .userId(userId)
            .username(username)
            .isAuthenticated(isAuthenticated)
            .exceptionMessage(exception != null ? exception.getMessage() : null)
            .exceptionStackTrace(exception != null ? getStackTraceAsString(exception) : null)
            .source("API")
            .serviceName("reddit-backend")
            .build();
    }

    private void logToFile(LogEntry entry) {
        try {
            String exceptionPart = entry.exceptionMessage() != null ? ERROR_PREFIX + entry.exceptionMessage() : "";
            fileLogger.info(LOG_FORMAT,
                entry.timestamp(),
                entry.method(),
                entry.endpoint(),
                entry.correlationId(),
                entry.statusCode(),
                entry.responseTimeMs(),
                entry.ipAddress(),
                entry.isAuthenticated() ? entry.username() : ANONYMOUS,
                exceptionPart
            );

            if (logger.isDebugEnabled()) {
                logger.debug("API Log Entry: {}", objectMapper.writeValueAsString(entry));
            }
        } catch (Exception e) {
            logger.error("Failed to write to file logger", e);
        }
    }

    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private String determineLogLevel(HttpServletResponse response, Throwable exception) {
        if (exception != null) {
            return "ERROR";
        }
        int status = response.getStatus();
        if (status >= 500) {
            return "ERROR";
        } else if (status >= 400) {
            return "WARN";
        }
        return "INFO";
    }

    private String getHeadersAsString(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, isSensitiveHeader(headerName) ? REDACTED : request.getHeader(headerName));
        }
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (IOException e) {
            return "{}";
        }
    }

    private String getResponseHeadersAsString(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (IOException e) {
            return "{}";
        }
    }

    private boolean isSensitiveHeader(String headerName) {
        return sensitiveHeaders.contains(headerName);
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        try {
            String body = new String(content, request.getCharacterEncoding());
            return truncateIfNeeded(body);
        } catch (UnsupportedEncodingException e) {
            return "[Unable to read request body]";
        }
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        try {
            String body = new String(content, response.getCharacterEncoding());
            return truncateIfNeeded(body);
        } catch (UnsupportedEncodingException e) {
            return "[Unable to read response body]";
        }
    }

    private String truncateIfNeeded(String body) {
        if (body.length() > maxBodySize) {
            return body.substring(0, maxBodySize) + TRUNCATED;
        }
        return body;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    private String getStackTraceAsString(Throwable exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.toString()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > maxBodySize) {
                sb.append("...[truncated]");
                break;
            }
        }
        return sb.toString();
    }

    // Public API methods for querying logs

    public Page<LogEntry> getAllLogs(Pageable pageable) {
        return logRepository.findByOrderByTimestampDesc(pageable);
    }

    public Page<LogEntry> getLogsByUser(Long userId, Pageable pageable) {
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
