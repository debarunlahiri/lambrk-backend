package com.lambrk.controller;

import com.lambrk.dto.FileUploadRequest;
import com.lambrk.dto.FileUploadResponse;
import com.lambrk.service.FileUploadService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/upload")
    @NewSpan("upload-file")
    @Counted(value = "files.uploaded")
    @Timed(value = "files.upload.duration")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute FileUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        FileUploadResponse response = fileUploadService.uploadFile(file, request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    @NewSpan("get-file-metadata")
    @Counted(value = "files.metadata.viewed")
    @Timed(value = "files.metadata.duration")
    public ResponseEntity<FileUploadResponse> getFileMetadata(
            @PathVariable @SpanTag Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        FileUploadResponse response = fileUploadService.getFile(fileId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}/content")
    @NewSpan("download-file")
    @Counted(value = "files.downloaded")
    @Timed(value = "files.download.duration")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable @SpanTag Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        
        try {
            FileUploadResponse metadata = fileUploadService.getFile(fileId, userId);
            byte[] fileContent = fileUploadService.getFileContent(metadata.fileName(), userId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.originalFileName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, metadata.mimeType());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length));
            
            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(fileContent.length)
                .body(resource);
                
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @NewSpan("get-user-files")
    @Counted(value = "files.user.viewed")
    @Timed(value = "files.user.duration")
    public ResponseEntity<Page<FileUploadResponse>> getUserFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        Pageable pageable = PageRequest.of(page, size);
        Page<FileUploadResponse> files = fileUploadService.getUserFiles(userId, pageable);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/type/{type}")
    @NewSpan("get-files-by-type")
    @Counted(value = "files.type.viewed")
    @Timed(value = "files.type.duration")
    public ResponseEntity<Page<FileUploadResponse>> getFilesByType(
            @PathVariable @SpanTag FileUploadRequest.FileType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileUploadResponse> files = fileUploadService.getFilesByType(type, pageable);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/public")
    @NewSpan("get-public-files")
    @Counted(value = "files.public.viewed")
    @Timed(value = "files.public.duration")
    public ResponseEntity<Page<FileUploadResponse>> getPublicFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FileUploadResponse> files = fileUploadService.getFilesByType(FileUploadRequest.FileType.POST_IMAGE, pageable);
        return ResponseEntity.ok(files);
    }

    @PutMapping("/{fileId}")
    @NewSpan("update-file-metadata")
    @Counted(value = "files.updated")
    @Timed(value = "files.update.duration")
    public ResponseEntity<FileUploadResponse> updateFileMetadata(
            @PathVariable @SpanTag Long fileId,
            @Valid @RequestBody FileUploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        FileUploadResponse response = fileUploadService.updateFileMetadata(fileId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fileId}")
    @NewSpan("delete-file")
    @Counted(value = "files.deleted")
    @Timed(value = "files.delete.duration")
    public ResponseEntity<Void> deleteFile(
            @PathVariable @SpanTag Long fileId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        fileUploadService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @NewSpan("get-file-stats")
    @Counted(value = "files.stats.viewed")
    @Timed(value = "files.stats.duration")
    public ResponseEntity<Object> getFileStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Long userId = getUserId(userDetails);
        
        // This would need to be implemented in FileUploadService
        Object stats = new Object() {
            public final long totalFiles = 0;
            public final long totalSize = 0;
            public final long imageCount = 0;
            public final long videoCount = 0;
            public final long avatarCount = 0;
        };
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search")
    @NewSpan("search-files")
    @Counted(value = "files.searched")
    @Timed(value = "files.search.duration")
    public ResponseEntity<List<FileUploadResponse>> searchFiles(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // This would need to be implemented in FileUploadService
        List<FileUploadResponse> results = List.of(); // Placeholder
        return ResponseEntity.ok(results);
    }

    @GetMapping("/recent")
    @NewSpan("get-recent-files")
    @Counted(value = "files.recent.viewed")
    @Timed(value = "files.recent.duration")
    public ResponseEntity<List<FileUploadResponse>> getRecentFiles(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // This would need to be implemented in FileUploadService
        List<FileUploadResponse> results = List.of(); // Placeholder
        return ResponseEntity.ok(results);
    }

    private Long getUserId(UserDetails userDetails) {
        // In a real implementation, extract user ID from UserDetails
        return 1L; // Placeholder
    }
}
