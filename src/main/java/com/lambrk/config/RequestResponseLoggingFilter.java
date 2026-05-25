package com.lambrk.config;

import com.lambrk.service.LoggingService;
import com.lambrk.service.LoggingService.LogContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private final LoggingService loggingService;

    public RequestResponseLoggingFilter(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (shouldNotLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        Long startTime = System.currentTimeMillis();
        Throwable exception = null;

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            wrappedResponse.copyBodyToResponse();

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID userId = null;
            String username = null;
            boolean isAuthenticated = false;

            if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {
                isAuthenticated = true;
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User) {
                    username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                } else if (principal instanceof String) {
                    username = (String) principal;
                }
            }

            long responseTimeMs = System.currentTimeMillis() - startTime;
            String correlationId = request.getHeader("X-Correlation-Id");
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            LogContext context = new LogContext(
                request.getMethod(),
                request.getRequestURI(),
                request.getRequestURL().toString(),
                request.getQueryString(),
                getHeadersAsString(request),
                getRequestBody(wrappedRequest),
                getResponseHeadersAsString(wrappedResponse),
                getResponseBody(wrappedResponse),
                response.getStatus(),
                responseTimeMs,
                getClientIpAddress(request),
                request.getHeader("User-Agent"),
                correlationId,
                userId,
                username,
                isAuthenticated,
                exception != null ? exception.getMessage() : null,
                exception != null ? getStackTraceAsString(exception) : null
            );

            loggingService.logRequestResponse(context);
        }
    }

    private boolean shouldNotLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        // Skip CORS preflight and health/static endpoints
        if ("OPTIONS".equals(method)) return true;
        return path.startsWith("/actuator") ||
               path.startsWith("/health") ||
               path.startsWith("/favicon") ||
               path.startsWith("/static") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               (path.equals("/") && "GET".equals(method));
    }

    private String getHeadersAsString(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(headers);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String getResponseHeadersAsString(ContentCachingResponseWrapper response) {
        Map<String, String> headers = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            headers.put(headerName, response.getHeader(headerName));
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(headers);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) return null;
        try {
            return new String(content, request.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length == 0) return null;
        try {
            return new String(content, response.getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            return null;
        }
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
            if (sb.length() > 5000) {
                sb.append("...[truncated]");
                break;
            }
        }
        return sb.toString();
    }
}
