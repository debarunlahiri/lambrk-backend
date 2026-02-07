package com.lambrk.exception;

import com.lambrk.service.ContentModerationException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        meterRegistry.counter("errors", "type", "not_found").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/not-found"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResource(DuplicateResourceException ex) {
        meterRegistry.counter("errors", "type", "duplicate").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Duplicate Resource");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/duplicate"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ProblemDetail handleUnauthorizedAction(UnauthorizedActionException ex) {
        meterRegistry.counter("errors", "type", "unauthorized_action").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Unauthorized Action");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/unauthorized-action"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        meterRegistry.counter("errors", "type", "validation").increment();
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Validation Error");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/validation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(ContentModerationException.class)
    public ProblemDetail handleContentModeration(ContentModerationException ex) {
        meterRegistry.counter("errors", "type", "content_moderation").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Content Moderation Violation");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/content-moderation"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("violationCategories", ex.getViolationCategories());
        return problem;
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ProblemDetail handleCircuitBreakerOpen(CallNotPermittedException ex) {
        meterRegistry.counter("errors", "type", "circuit_breaker").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
            "Service temporarily unavailable. Please try again later.");
        problem.setTitle("Service Unavailable");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/circuit-breaker"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ProblemDetail handleRateLimitExceeded(RequestNotPermitted ex) {
        meterRegistry.counter("errors", "type", "rate_limit").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
            "Rate limit exceeded. Please slow down.");
        problem.setTitle("Rate Limit Exceeded");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/rate-limit"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(BulkheadFullException.class)
    public ProblemDetail handleBulkheadFull(BulkheadFullException ex) {
        meterRegistry.counter("errors", "type", "bulkhead").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
            "Too many concurrent requests. Please try again.");
        problem.setTitle("Bulkhead Full");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/bulkhead"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        meterRegistry.counter("errors", "type", "access_denied").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Access denied");
        problem.setTitle("Access Denied");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/access-denied"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        meterRegistry.counter("errors", "type", "bad_credentials").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        problem.setTitle("Authentication Failed");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/bad-credentials"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        meterRegistry.counter("errors", "type", "internal").increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://api.reddit-backend.com/errors/internal"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
