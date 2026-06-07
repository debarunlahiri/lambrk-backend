package com.lambrk.dto;

import com.lambrk.domain.FileUpload;

import java.util.UUID;

public record MediaResponse(
    UUID id,
    String url,
    String thumbnailUrl,
    String type,
    String mimeType,
    long fileSize,
    String altText
) {

    public static MediaResponse from(FileUpload file) {
        return new MediaResponse(
            file.getId(),
            com.lambrk.util.CdnUrlResolver.resolve(file.getFileUrl()),
            com.lambrk.util.CdnUrlResolver.resolve(file.getThumbnailUrl()),
            file.getType().name(),
            file.getMimeType(),
            file.getFileSize(),
            file.getAltText()
        );
    }
}
