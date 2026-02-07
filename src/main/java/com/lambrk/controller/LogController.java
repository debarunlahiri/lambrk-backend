package com.lambrk.controller;

import com.lambrk.domain.LogEntry;
import com.lambrk.service.LoggingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@PreAuthorize("hasRole('ADMIN')")
public class LogController {

    private final LoggingService loggingService;

    public LogController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @GetMapping
    public ResponseEntity<Page<LogEntry>> getAllLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getAllLogs(pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<LogEntry>> getLogsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getLogsByUser(userId, pageable));
    }

    @GetMapping("/endpoint")
    public ResponseEntity<Page<LogEntry>> getLogsByEndpoint(
            @RequestParam String path,
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getLogsByEndpoint(path, pageable));
    }

    @GetMapping("/method/{method}")
    public ResponseEntity<Page<LogEntry>> getLogsByMethod(
            @PathVariable String method,
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getLogsByMethod(method, pageable));
    }

    @GetMapping("/status/{statusCode}")
    public ResponseEntity<Page<LogEntry>> getLogsByStatusCode(
            @PathVariable Integer statusCode,
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getLogsByStatusCode(statusCode, pageable));
    }

    @GetMapping("/errors")
    public ResponseEntity<Page<LogEntry>> getErrorLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getErrorLogs(pageable));
    }

    @GetMapping("/exceptions")
    public ResponseEntity<Page<LogEntry>> getExceptionLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getExceptionLogs(pageable));
    }

    @GetMapping("/anonymous")
    public ResponseEntity<Page<LogEntry>> getAnonymousLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getAnonymousLogs(pageable));
    }

    @GetMapping("/authenticated")
    public ResponseEntity<Page<LogEntry>> getAuthenticatedLogs(
            @PageableDefault(size = 50, sort = "timestamp", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(loggingService.getAuthenticatedLogs(pageable));
    }
}
