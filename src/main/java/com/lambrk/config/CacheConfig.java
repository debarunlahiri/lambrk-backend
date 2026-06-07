package com.lambrk.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  @Primary
  public CacheManager caffeineCacheManager() {
    CaffeineCacheManager cacheManager =
        new CaffeineCacheManager(
            "posts",
            "hotPosts",
            "newPosts",
            "topPosts",
            "searchPosts",
            "users",
            "userProfiles",
            "communities",
            "trendingCommunities",
            "comments",
            "commentTrees",
            "searchResults",
            "feed",
            "categories",
            "fileUploads",
            "notifications",
            "recommendations",
            "contentModeration",
            "contentRecommendations");
    cacheManager.setCaffeine(
        Caffeine.newBuilder().maximumSize(5000).expireAfterWrite(5, TimeUnit.MINUTES));
    return cacheManager;
  }
}
