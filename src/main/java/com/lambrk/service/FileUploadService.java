package com.lambrk.service;

import com.lambrk.domain.FileUpload;
import com.lambrk.domain.User;
import com.lambrk.dto.FileUploadRequest;
import com.lambrk.dto.FileUploadResponse;
import com.lambrk.repository.FileUploadRepository;
import com.lambrk.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileUploadService {

    private static final String ERROR_FILE_EMPTY = "File is empty";
    private static final String ERROR_FILE_TOO_LARGE = "File size exceeds maximum allowed size: %d bytes";
    private static final String ERROR_FILE_TYPE_NOT_ALLOWED = "File type not allowed: %s";
    private static final String ERROR_FILE_NAME_REQUIRED = "File name is required";
    private static final String ERROR_USER_NOT_FOUND = "User not found: %d";
    private static final String ERROR_FILE_NOT_FOUND = "File not found: %s";
    private static final String ERROR_ACCESS_DENIED = "Access denied to file: %s";
    private static final String ERROR_UPLOAD_FAILED = "Failed to upload file";
    private static final String ERROR_CHECKSUM_FAILED = "Failed to calculate checksum";
    private static final String SHA_256 = "SHA-256";
    private static final String FILE_URL_PREFIX = "/api/files/";
    private static final String THUMBNAIL_URL_PREFIX = "/api/files/thumbnails/";
    private static final int CHECKSUM_STRING_LENGTH = 2;

    private final FileUploadRepository fileUploadRepository;
    private final UserRepository userRepository;
    private final CustomMetrics customMetrics;
    private final S3StorageService s3StorageService;
    private final FreeTierLimitService freeTierLimitService;
    private final String uploadDirectory;
    private final long maxFileSize;
    private final List<String> allowedTypes;
    private final boolean s3Enabled;

    public FileUploadService(
            FileUploadRepository fileUploadRepository,
            UserRepository userRepository,
            CustomMetrics customMetrics,
            S3StorageService s3StorageService,
            FreeTierLimitService freeTierLimitService,
            @Value("${app.upload.directory:uploads}") String uploadDirectory,
            @Value("${app.upload.max-file-size:10485760}") long maxFileSize,
            @Value("${app.upload.allowed-types:image/jpeg,image/png,image/gif,video/mp4}") List<String> allowedTypes,
            @Value("${aws.s3.enabled:true}") boolean s3Enabled) {
        this.fileUploadRepository = fileUploadRepository;
        this.userRepository = userRepository;
        this.customMetrics = customMetrics;
        this.s3StorageService = s3StorageService;
        this.freeTierLimitService = freeTierLimitService;
        this.uploadDirectory = uploadDirectory;
        this.maxFileSize = maxFileSize;
        this.allowedTypes = allowedTypes;
        this.s3Enabled = s3Enabled;

        ensureLocalDirectoryExists();
    }

    private void ensureLocalDirectoryExists() {
        if (!s3Enabled) {
            try {
                Files.createDirectories(Paths.get(uploadDirectory));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create upload directory", e);
            }
        }
    }

    @CacheEvict(value = "fileUploads", allEntries = true)
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest request, Long userId) {
        validateFile(file, request);

        // Check free tier limits before upload
        freeTierLimitService.checkUploadAllowed(userId, file.getSize());

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException(String.format(ERROR_USER_NOT_FOUND, userId)));

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            String checksum;

            if (s3Enabled) {
                // Upload to S3
                String directory = getDirectoryForType(request.type());
                String s3Key = s3StorageService.uploadFile(file, directory);
                uniqueFilename = s3Key;
                checksum = calculateChecksum(file);
            } else {
                // Local storage fallback
                Path filePath = Paths.get(uploadDirectory, uniqueFilename);
                Files.copy(file.getInputStream(), filePath);
                checksum = calculateChecksum(filePath);
            }

            // Create file upload record using custom constructor (type first, no id)
            FileUpload fileUpload = new FileUpload(
                FileUpload.FileUploadType.valueOf(request.type().name()),
                uniqueFilename,
                originalFilename,
                getFileUrl(uniqueFilename),
                getThumbnailUrl(uniqueFilename),
                file.getSize(),
                file.getContentType(),
                request.description(),
                request.isPublic(),
                request.isNSFW(),
                request.altText(),
                user,
                Instant.now(),
                Instant.now(),
                checksum
            );

            FileUpload saved = fileUploadRepository.save(fileUpload);

            // Record usage for free tier tracking
            freeTierLimitService.recordUpload(userId, file.getSize());

            customMetrics.recordFileUpload(request.type().name());

            return FileUploadResponse.from(saved);

        } catch (IOException e) {
            throw new RuntimeException(ERROR_UPLOAD_FAILED, e);
        }
    }

    @Cacheable(value = "fileUploads", key = "#fileId")
    public FileUploadResponse getFile(Long fileId, Long userId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, fileId)));
        
        // Check if user has access to this file
        if (!fileUpload.isPublic() && !fileUpload.uploadedBy().id().equals(userId)) {
            throw new RuntimeException(String.format(ERROR_ACCESS_DENIED, fileId));
        }
        
        return FileUploadResponse.from(fileUpload);
    }

    @Cacheable(value = "fileUploads", key = "#userId + '-' + #page")
    public Page<FileUploadResponse> getUserFiles(Long userId, Pageable pageable) {
        Page<FileUpload> files = fileUploadRepository.findByUploadedByOrderByCreatedAtDesc(userId, pageable);
        return files.map(FileUploadResponse::from);
    }

    @Cacheable(value = "fileUploads", key = "#type + '-' + #page")
    public Page<FileUploadResponse> getFilesByType(FileUploadRequest.FileType type, Pageable pageable) {
        FileUpload.FileUploadType entityType = FileUpload.FileUploadType.valueOf(type.name());
        Page<FileUpload> files = fileUploadRepository.findByTypeAndIsPublicOrderByCreatedAtDesc(entityType, true, pageable);
        return files.map(FileUploadResponse::from);
    }

    @CacheEvict(value = "fileUploads", allEntries = true)
    public void deleteFile(Long fileId, Long userId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, fileId)));

        if (!fileUpload.uploadedBy().id().equals(userId)) {
            throw new RuntimeException(String.format(ERROR_ACCESS_DENIED, fileId));
        }

        // Delete from S3 or local storage
        if (s3Enabled) {
            s3StorageService.deleteFile(fileUpload.fileName());
        } else {
            try {
                Path filePath = Paths.get(uploadDirectory, fileUpload.fileName());
                Files.deleteIfExists(filePath);

                Path thumbnailPath = Paths.get(uploadDirectory, "thumbnails", fileUpload.fileName());
                Files.deleteIfExists(thumbnailPath);
            } catch (IOException e) {
                System.err.println("Failed to delete file from disk: " + e.getMessage());
            }
        }

        // Record file deletion for free tier tracking
        freeTierLimitService.recordFileDeletion(userId, fileUpload.fileSize());

        // Delete database record
        fileUploadRepository.delete(fileUpload);
    }

    @CacheEvict(value = "fileUploads", allEntries = true)
    public FileUploadResponse updateFileMetadata(Long fileId, FileUploadRequest request, Long userId) {
        FileUpload fileUpload = fileUploadRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, fileId)));
        
        if (!fileUpload.uploadedBy().id().equals(userId)) {
            throw new RuntimeException(String.format(ERROR_ACCESS_DENIED, fileId));
        }
        
        FileUpload updated = new FileUpload(
            fileUpload.id(),
            fileUpload.fileName(),
            fileUpload.originalFileName(),
            fileUpload.fileUrl(),
            fileUpload.thumbnailUrl(),
            fileUpload.type(),
            fileUpload.fileSize(),
            fileUpload.mimeType(),
            request.description(),
            request.isPublic(),
            request.isNSFW(),
            request.altText(),
            fileUpload.uploadedBy(),
            fileUpload.uploadedAt(),
            Instant.now(),
            fileUpload.checksum()
        );
        
        FileUpload saved = fileUploadRepository.save(updated);
        return FileUploadResponse.from(saved);
    }

    public byte[] getFileContent(String filename, Long userId) throws IOException {
        FileUpload fileUpload = fileUploadRepository.findByFileName(filename)
            .orElseThrow(() -> new RuntimeException(String.format(ERROR_FILE_NOT_FOUND, filename)));

        if (!fileUpload.isPublic() && !fileUpload.uploadedBy().id().equals(userId)) {
            throw new RuntimeException(String.format(ERROR_ACCESS_DENIED, filename));
        }

        byte[] content;
        if (s3Enabled) {
            content = s3StorageService.downloadFile(fileUpload.fileName());
        } else {
            Path filePath = Paths.get(uploadDirectory, filename);
            content = Files.readAllBytes(filePath);
        }

        // Record bandwidth usage for free tier tracking
        freeTierLimitService.recordBandwidthUsage(userId, content.length);

        return content;
    }

    private void validateFile(MultipartFile file, FileUploadRequest request) {
        if (file.isEmpty()) {
            throw new RuntimeException(ERROR_FILE_EMPTY);
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException(String.format(ERROR_FILE_TOO_LARGE, maxFileSize));
        }
        
        String contentType = file.getContentType();
        if (!allowedTypes.contains(contentType)) {
            throw new RuntimeException(String.format(ERROR_FILE_TYPE_NOT_ALLOWED, contentType));
        }
        
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new RuntimeException(ERROR_FILE_NAME_REQUIRED);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) 
            ? "" 
            : filename.substring(lastDotIndex);
    }

    private String getFileUrl(String filename) {
        return FILE_URL_PREFIX + filename;
    }

    private String getThumbnailUrl(String filename) {
        return THUMBNAIL_URL_PREFIX + filename;
    }

    private String calculateChecksum(byte[] fileBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_256);
            byte[] hashBytes = md.digest(fileBytes);
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ERROR_CHECKSUM_FAILED, e);
        }
    }

    private String calculateChecksum(Path filePath) {
        try {
            return calculateChecksum(Files.readAllBytes(filePath));
        } catch (IOException e) {
            throw new RuntimeException(ERROR_CHECKSUM_FAILED, e);
        }
    }

    private String calculateChecksum(MultipartFile file) {
        try {
            return calculateChecksum(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(ERROR_CHECKSUM_FAILED, e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%0" + CHECKSUM_STRING_LENGTH + "x", b));
        }
        return sb.toString();
    }

    private String getDirectoryForType(FileUploadRequest.FileType type) {
        return switch (type) {
            case AVATAR -> "avatars";
            case POST_IMAGE -> "posts/images";
            case POST_VIDEO -> "posts/videos";
            case SUBREDDIT_ICON -> "subreddits/icons";
            case SUBREDDIT_HEADER -> "subreddits/headers";
            case BANNER -> "banners";
        };
    }

    public FreeTierLimitService.FreeTierStatus getUserFreeTierStatus(Long userId) {
        return freeTierLimitService.getUserStatus(userId);
    }
}
