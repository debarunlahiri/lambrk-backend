package com.lambrk.dto;

import com.lambrk.domain.Post.PostType;

import java.util.List;

public record FeedRequest(
    Long userId,
    Integer limit,
    String sortBy,
    List<PostType> postTypes,
    Boolean includeNsfw,
    Boolean includeFromFollowingOnly,
    Double timeDecayFactor
) {
    public FeedRequest {
        if (limit == null || limit < 1 || limit > 100) {
            limit = 20;
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "algorithm";
        }
        if (includeNsfw == null) {
            includeNsfw = false;
        }
        if (includeFromFollowingOnly == null) {
            includeFromFollowingOnly = false;
        }
        if (timeDecayFactor == null) {
            timeDecayFactor = 1.0;
        }
    }

    public static FeedRequest defaultRequest(Long userId) {
        return new FeedRequest(userId, 20, "algorithm", null, false, false, 1.0);
    }
}
