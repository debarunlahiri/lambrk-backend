package com.lambrk.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats());
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager postCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("posts", "hotPosts", "newPosts", "topPosts");
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=10000,expireAfterWrite=5m,recordStats"));
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager userCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("users", "userProfiles");
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=5000,expireAfterWrite=10m,recordStats"));
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager subredditCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("subreddits", "trendingSubreddits");
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=2000,expireAfterWrite=15m,recordStats"));
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager commentCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("comments", "commentTrees");
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=20000,expireAfterWrite=3m,recordStats"));
        return cacheManager;
    }

    @Bean
    public CaffeineCacheManager searchCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("searchResults");
        cacheManager.setCaffeineSpec(CaffeineSpec.parse("maximumSize=1000,expireAfterWrite=2m,recordStats"));
        return cacheManager;
    }
}
