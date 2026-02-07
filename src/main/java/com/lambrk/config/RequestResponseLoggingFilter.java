package com.lambrk.config;

import com.lambrk.service.LoggingService;
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

        // Skip logging for certain paths
        if (shouldNotLog(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to capture content
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
            // Get user details from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;
            String username = null;
            boolean isAuthenticated = false;

            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                isAuthenticated = true;
                // Extract user details - adjust based on your UserDetails implementation
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User) {
                    username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                } else if (principal instanceof String) {
                    username = (String) principal;
                }
                // Note: userId extraction depends on your UserDetails implementation
                // You may need to cast to your custom UserDetails class
            }

            // Copy content for response
            wrappedResponse.copyBodyToResponse();

            // Log asynchronously
            loggingService.logRequestResponse(request, response, wrappedRequest, wrappedResponse,
                userId, username, isAuthenticated, startTime, exception);
        }
    }

    private boolean shouldNotLog(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip health checks, actuator endpoints, and static resources
        return path.startsWith("/actuator") ||
               path.startsWith("/health") ||
               path.startsWith("/favicon") ||
               path.startsWith("/static") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/") && "GET".equals(request.getMethod());
    }
}
