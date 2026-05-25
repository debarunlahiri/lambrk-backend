package com.lambrk.dto;

import com.lambrk.domain.FileUpload;
import java.time.Instant;
import java.util.UUID;

public record FileUploadResponse(
    
    UUID fileId,
    
    String fileName,
    
    String originalFileName,
    
    String fileUrl,
    
    String thumbnailUrl,
    
    FileUpload.FileUploadType type,
    
    long fileSize,
    
    String mimeType,
    
    String description,
    
    boolean isPublic,
    
    boolean isNSFW,
    
    String altText,
    
    UUID uploadedBy,
    
    Instant uploadedAt,
    
    String checksum
) {
    
    public static FileUploadResponse from(FileUpload fileUpload) {
        return new FileUploadResponse(
            fileUpload.getId(),
            fileUpload.getFileName(),
            fileUpload.getOriginalFileName(),
            fileUpload.getFileUrl(),
            fileUpload.getThumbnailUrl(),
            fileUpload.getType(),
            fileUpload.getFileSize(),
            fileUpload.getMimeType(),
            fileUpload.getDescription(),
            fileUpload.isPublic(),
            fileUpload.isNSFW(),
            fileUpload.getAltText(),
            fileUpload.getUploadedBy().getId(),
            fileUpload.getUploadedAt(),
            fileUpload.getChecksum()
        );
    }
}
