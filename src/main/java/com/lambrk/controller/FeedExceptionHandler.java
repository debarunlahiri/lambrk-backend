package com.lambrk.controller;

import com.lambrk.dto.FeedResponse;
import com.lambrk.exception.DuplicateResourceException;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.service.ContentModerationException;
import com.lambrk.service.FeedService.FeedGenerationException;
import com.lambrk.service.FeedService.UserNotFoundException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Global exception handler for Feed API endpoints */
@RestControllerAdvice(basePackages = "com.lambrk.controller")
public class FeedExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(FeedExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(error.getField(), error.getDefaultMessage());
    }
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.BAD_REQUEST.value());
    error.put("error", "Validation Error");
    error.put("message", "Validation failed");
    error.put("fieldErrors", fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    logger.warn("Invalid request: {}", ex.getMessage());

    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.BAD_REQUEST.value());
    error.put("error", "Bad Request");
    error.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
    logger.warn("User not found: {}", ex.getMessage());

    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.NOT_FOUND.value());
    error.put("error", "Not Found");
    error.put("message", ex.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.NOT_FOUND.value());
    error.put("error", "Not Found");
    error.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateResource(
      DuplicateResourceException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.CONFLICT.value());
    error.put("error", "Conflict");
    error.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
  }

  @ExceptionHandler(UnauthorizedActionException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorizedAction(
      UnauthorizedActionException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.FORBIDDEN.value());
    error.put("error", "Forbidden");
    error.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(ContentModerationException.class)
  public ResponseEntity<Map<String, Object>> handleContentModeration(
      ContentModerationException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
    error.put("error", "Unprocessable Entity");
    error.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
  }

  @ExceptionHandler(FeedGenerationException.class)
  public ResponseEntity<FeedResponse> handleFeedGenerationException(FeedGenerationException ex) {
    logger.error("Feed generation failed: {}", ex.getMessage(), ex);

    // Return empty but valid response instead of error
    return ResponseEntity.ok(FeedResponse.empty());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.UNAUTHORIZED.value());
    error.put("error", "Unauthorized");
    error.put("message", "Invalid credentials");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.UNAUTHORIZED.value());
    error.put("error", "Unauthorized");
    error.put("message", "Invalid credentials");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
    logger.warn("Access denied: {}", ex.getMessage());

    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.FORBIDDEN.value());
    error.put("error", "Forbidden");
    error.put("message", "You don't have permission to access this resource");

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.BAD_REQUEST.value());
    error.put("error", "Bad Request");
    String message =
        ex.getRequiredType() != null && ex.getRequiredType().equals(java.util.UUID.class)
            ? ex.getName() + " must be a valid UUID"
            : "Invalid value for " + ex.getName();
    error.put("message", message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    logger.error("Unexpected error in feed API: {}", ex.getMessage(), ex);

    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", Instant.now().toString());
    error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    error.put("error", "Internal Server Error");
    error.put("message", "An unexpected error occurred. Please try again later.");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
