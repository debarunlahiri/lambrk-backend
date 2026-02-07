package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "file_uploads", indexes = {
    @Index(name = "idx_file_upload_type", columnList = "type"),
    @Index(name = "idx_file_upload_uploaded_by", columnList = "uploaded_by"),
    @Index(name = "idx_file_upload_created_at", columnList = "created_at"),
    @Index(name = "idx_file_upload_is_public", columnList = "is_public"),
    @Index(name = "idx_file_upload_is_nsfw", columnList = "is_nsfw")
})
@EntityListeners(AuditingEntityListener.class)
public record FileUpload(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must be less than 255 characters")
    @Column(name = "file_name", nullable = false, unique = true)
    String fileName,
    
    @NotBlank(message = "Original file name is required")
    @Size(max = 255, message = "Original file name must be less than 255 characters")
    @Column(name = "original_file_name", nullable = false)
    String originalFileName,
    
    @NotBlank(message = "File URL is required")
    @Size(max = 500, message = "File URL must be less than 500 characters")
    @Column(name = "file_url", nullable = false)
    String fileUrl,
    
    @Column(name = "thumbnail_url", length = 500)
    String thumbnailUrl,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    FileUploadType type,
    
    @Column(name = "file_size", nullable = false)
    long fileSize,
    
    @NotBlank(message = "MIME type is required")
    @Size(max = 100, message = "MIME type must be less than 100 characters")
    @Column(name = "mime_type", nullable = false, length = 100)
    String mimeType,
    
    @Column(name = "description", columnDefinition = "TEXT")
    String description,
    
    @Column(name = "is_public", nullable = false)
    boolean isPublic,
    
    @Column(name = "is_nsfw", nullable = false)
    boolean isNSFW,
    
    @Column(name = "alt_text", length = 500)
    String altText,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    User uploadedBy,
    
    @CreatedDate
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    Instant uploadedAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt,
    
    @NotBlank(message = "Checksum is required")
    @Size(max = 64, message = "Checksum must be less than 64 characters")
    @Column(name = "checksum", nullable = false, length = 64)
    String checksum
) {
    
    public FileUpload(FileUploadType type, String fileName, String originalFileName,
                      String fileUrl, String thumbnailUrl, long fileSize, String mimeType,
                      String description, boolean isPublic, boolean isNSFW, String altText,
                      User uploadedBy, Instant uploadedAt, Instant updatedAt, String checksum) {
        this(null, fileName, originalFileName, fileUrl, thumbnailUrl, type, fileSize, mimeType,
             description, isPublic, isNSFW, altText, uploadedBy, uploadedAt, updatedAt, checksum);
    }
    
    public enum FileUploadType {
        AVATAR, POST_IMAGE, POST_VIDEO, SUBREDDIT_ICON, SUBREDDIT_HEADER, BANNER
    }
}
