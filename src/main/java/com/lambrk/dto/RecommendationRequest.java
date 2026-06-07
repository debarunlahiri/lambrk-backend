package com.lambrk.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RecommendationRequest(
    @NotNull UUID userId,
    RecommendationType type,
    Integer limit,
    List<String> excludeCommunities,
    List<String> excludeUsers,
    boolean includeNSFW,
    boolean includeOver18,
    String contextCommunityId,
    String contextPostId) {

  public RecommendationRequest {
    type = type != null ? type : RecommendationType.POSTS;
    limit = limit != null ? limit : 20;
    excludeCommunities = excludeCommunities != null ? excludeCommunities : List.of();
    excludeUsers = excludeUsers != null ? excludeUsers : List.of();
  }

  public enum RecommendationType {
    POSTS,
    COMMUNITIES,
    USERS,
    COMMENTS
  }
}
