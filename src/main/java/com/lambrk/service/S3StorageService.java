package com.lambrk.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);
    private static final String ERROR_S3_DISABLED = "S3 storage is disabled";
    private static final String ERROR_UPLOAD_FAILED = "Failed to upload file to S3";
    private static final String ERROR_DOWNLOAD_FAILED = "Failed to download file from S3";
    private static final String ERROR_DELETE_FAILED = "Failed to delete file from S3";
    private static final String LOG_FILE_UPLOADED = "File uploaded to S3: {}/{}";
    private static final String LOG_FILE_DELETED = "File deleted from S3: {}/{}";
    private static final String LOG_FILE_NOT_FOUND = "File not found in S3: {}/{}";
    private static final String URL_FORMAT = "https://%s.s3.%s.amazonaws.com/%s";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final int presignedUrlExpirySeconds;
    private final boolean s3Enabled;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            String bucketName,
            @Value("${aws.s3.presigned-url-expiry:3600}") int presignedUrlExpirySeconds,
            @Value("${aws.s3.enabled:true}") boolean s3Enabled) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.presignedUrlExpirySeconds = presignedUrlExpirySeconds;
        this.s3Enabled = s3Enabled;
    }

    private void ensureS3Enabled() {
        if (!s3Enabled) {
            throw new IllegalStateException(ERROR_S3_DISABLED);
        }
    }

    @CircuitBreaker(name = "s3Service")
    @Retry(name = "s3Service")
    public String uploadFile(MultipartFile file, String directory) {
        ensureS3Enabled();

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = directory + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFilename)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            logger.info(LOG_FILE_UPLOADED, bucketName, uniqueFilename);
            return uniqueFilename;

        } catch (IOException e) {
            logger.error("{}: {}", ERROR_UPLOAD_FAILED, e.getMessage());
            throw new RuntimeException(ERROR_UPLOAD_FAILED, e);
        }
    }

    @CircuitBreaker(name = "s3Service")
    public byte[] downloadFile(String key) {
        ensureS3Enabled();

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (NoSuchKeyException e) {
            logger.error(LOG_FILE_NOT_FOUND, bucketName, key);
            throw new RuntimeException("File not found", e);
        } catch (S3Exception e) {
            logger.error("{}: {}", ERROR_DOWNLOAD_FAILED, e.getMessage());
            throw new RuntimeException(ERROR_DOWNLOAD_FAILED, e);
        }
    }

    @CircuitBreaker(name = "s3Service")
    public void deleteFile(String key) {
        ensureS3Enabled();

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.deleteObject(deleteObjectRequest);
            logger.info(LOG_FILE_DELETED, bucketName, key);

        } catch (S3Exception e) {
            logger.error("{}: {}", ERROR_DELETE_FAILED, e.getMessage());
            throw new RuntimeException(ERROR_DELETE_FAILED, e);
        }
    }

    public String generatePresignedUrl(String key) {
        ensureS3Enabled();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofSeconds(presignedUrlExpirySeconds))
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    public boolean fileExists(String key) {
        if (!s3Enabled) {
            return false;
        }

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            logger.error("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    public String getPublicUrl(String key) {
        return String.format(URL_FORMAT, bucketName, s3Client.serviceClientConfiguration().region().id(), key);
    }

    public long getFileSize(String key) {
        if (!s3Enabled) {
            return 0;
        }

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();

        } catch (S3Exception e) {
            logger.error("Failed to get file size: {}", e.getMessage());
            return 0;
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
}
