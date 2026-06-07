package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
    name = "file_uploads",
    indexes = {
      @Index(name = "idx_file_upload_type", columnList = "type"),
      @Index(name = "idx_file_upload_uploaded_by", columnList = "uploaded_by"),
      @Index(name = "idx_file_upload_uploaded_at", columnList = "uploaded_at"),
      @Index(name = "idx_file_upload_is_public", columnList = "is_public"),
      @Index(name = "idx_file_upload_is_nsfw", columnList = "is_nsfw"),
      @Index(name = "idx_file_upload_post", columnList = "post_id")
    })
@EntityListeners(AuditingEntityListener.class)
public class FileUpload {

  @Id private UUID id;

  @NotBlank(message = "File name is required")
  @Size(max = 255, message = "File name must be less than 255 characters")
  @Column(name = "file_name", nullable = false, unique = true)
  private String fileName;

  @NotBlank(message = "Original file name is required")
  @Size(max = 255, message = "Original file name must be less than 255 characters")
  @Column(name = "original_file_name", nullable = false)
  private String originalFileName;

  @NotBlank(message = "File URL is required")
  @Size(max = 500, message = "File URL must be less than 500 characters")
  @Column(name = "file_url", nullable = false)
  private String fileUrl;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private FileUploadType type;

  @Column(name = "file_size", nullable = false)
  private long fileSize;

  @NotBlank(message = "MIME type is required")
  @Size(max = 100, message = "MIME type must be less than 100 characters")
  @Column(name = "mime_type", nullable = false, length = 100)
  private String mimeType;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic;

  @Column(name = "is_nsfw", nullable = false)
  private boolean isNSFW;

  @Column(name = "alt_text", length = 500)
  private String altText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by", nullable = false)
  private User uploadedBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id")
  private Post post;

  @CreatedDate
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private Instant uploadedAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @NotBlank(message = "Checksum is required")
  @Size(max = 64, message = "Checksum must be less than 64 characters")
  @Column(name = "checksum", nullable = false, length = 64)
  private String checksum;

  protected FileUpload() {}

  public FileUpload(
      UUID id,
      String fileName,
      String originalFileName,
      String fileUrl,
      String thumbnailUrl,
      FileUploadType type,
      long fileSize,
      String mimeType,
      String description,
      boolean isPublic,
      boolean isNSFW,
      String altText,
      User uploadedBy,
      Instant uploadedAt,
      Instant updatedAt,
      String checksum) {
    this.id = id;
    this.fileName = fileName;
    this.originalFileName = originalFileName;
    this.fileUrl = fileUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.type = type;
    this.fileSize = fileSize;
    this.mimeType = mimeType;
    this.description = description;
    this.isPublic = isPublic;
    this.isNSFW = isNSFW;
    this.altText = altText;
    this.uploadedBy = uploadedBy;
    this.uploadedAt = uploadedAt;
    this.updatedAt = updatedAt;
    this.checksum = checksum;
  }

  public FileUpload(
      FileUploadType type,
      String fileName,
      String originalFileName,
      String fileUrl,
      String thumbnailUrl,
      long fileSize,
      String mimeType,
      String description,
      boolean isPublic,
      boolean isNSFW,
      String altText,
      User uploadedBy,
      Instant uploadedAt,
      Instant updatedAt,
      String checksum) {
    this(
        com.lambrk.util.UuidV7Generator.generate(),
        fileName,
        originalFileName,
        fileUrl,
        thumbnailUrl,
        type,
        fileSize,
        mimeType,
        description,
        isPublic,
        isNSFW,
        altText,
        uploadedBy,
        uploadedAt,
        updatedAt,
        checksum);
  }

  public enum FileUploadType {
    AVATAR,
    POST_IMAGE,
    POST_VIDEO,
    COMMUNITY_ICON,
    COMMUNITY_HEADER,
    BANNER,
    PROFILE_IMAGE,
    COVER_IMAGE
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public void setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public FileUploadType getType() {
    return type;
  }

  public void setType(FileUploadType type) {
    this.type = type;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isPublic() {
    return isPublic;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public boolean isNSFW() {
    return isNSFW;
  }

  public void setNSFW(boolean nsfw) {
    this.isNSFW = nsfw;
  }

  public String getAltText() {
    return altText;
  }

  public void setAltText(String altText) {
    this.altText = altText;
  }

  public User getUploadedBy() {
    return uploadedBy;
  }

  public void setUploadedBy(User uploadedBy) {
    this.uploadedBy = uploadedBy;
  }

  public Post getPost() {
    return post;
  }

  public void setPost(Post post) {
    this.post = post;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(Instant uploadedAt) {
    this.uploadedAt = uploadedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getChecksum() {
    return checksum;
  }

  public void setChecksum(String checksum) {
    this.checksum = checksum;
  }
}
