package com.lambrk.dto;

import com.lambrk.domain.Post;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PostCreateRequest(
    @Size(max = 300, message = "Title must be less than 300 characters") String title,
    String content,
    String url,
    Post.PostType postType,
    String flairText,
    String flairCssClass,
    boolean isSpoiler,
    boolean isOver18,
    UUID communityId,
    List<UUID> mediaIds) {

  public PostCreateRequest {
    if (postType == null) {
      postType = Post.PostType.TEXT;
    }
    isSpoiler = false;
    isOver18 = false;
    mediaIds = mediaIds != null ? mediaIds : List.of();
  }

  public PostCreateRequest(
      String title, String content, String url, Post.PostType postType, UUID communityId) {
    this(title, content, url, postType, null, null, false, false, communityId, List.of());
  }
}
