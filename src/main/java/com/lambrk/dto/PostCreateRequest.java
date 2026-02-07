package com.lambrk.dto;

import com.lambrk.domain.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
    
    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title must be less than 300 characters")
    String title,
    
    String content,
    
    String url,
    
    Post.PostType postType,
    
    String flairText,
    
    String flairCssClass,
    
    boolean isSpoiler,
    
    boolean isOver18,
    
    Long subredditId
) {
    
    public PostCreateRequest {
        if (postType == null) {
            postType = Post.PostType.TEXT;
        }
        isSpoiler = false;
        isOver18 = false;
    }
    
    public PostCreateRequest(String title, String content, String url, Post.PostType postType, Long subredditId) {
        this(title, content, url, postType, null, null, false, false, subredditId);
    }
}
