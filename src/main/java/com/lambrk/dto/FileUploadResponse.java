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
            fileUpload.id(),
            fileUpload.fileName(),
            fileUpload.originalFileName(),
            fileUpload.fileUrl(),
            fileUpload.thumbnailUrl(),
            fileUpload.type(),
            fileUpload.fileSize(),
            fileUpload.mimeType(),
            fileUpload.description(),
            fileUpload.isPublic(),
            fileUpload.isNSFW(),
            fileUpload.altText(),
            fileUpload.uploadedBy().id(),
            fileUpload.uploadedAt(),
            fileUpload.checksum()
        );
    }
}
