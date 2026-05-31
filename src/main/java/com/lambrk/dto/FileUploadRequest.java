package com.lambrk.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FileUploadRequest(

    @NotNull
    FileType type,

    @Size(max = 255, message = "File name must be less than 255 characters")
    String fileName,

    @Size(max = 500, message = "Description must be less than 500 characters")
    String description,

    Boolean isPublic,

    Boolean isNSFW,

    String altText
) {

    public FileUploadRequest {
        if (isPublic == null) isPublic = true;
        if (isNSFW == null) isNSFW = false;
    }

    public enum FileType {
        AVATAR, POST_IMAGE, POST_VIDEO, COMMUNITY_ICON, COMMUNITY_HEADER, BANNER,
        PROFILE_IMAGE, COVER_IMAGE
    }
}
