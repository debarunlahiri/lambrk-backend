package com.lambrk.util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CdnUrlResolver {

  private static final Logger logger = LoggerFactory.getLogger(CdnUrlResolver.class);
  private static String staticCdnBaseUrl;
  private static final String FALLBACK_CDN = "https://d2l1t2x4807mjw.cloudfront.net";
  private static final String S3_HOST_PATTERN = ".amazonaws.com";

  @Value("${aws.s3.cdn-base-url:}")
  private String cdnBaseUrl;

  @PostConstruct
  public void init() {
    if (cdnBaseUrl != null && !cdnBaseUrl.isBlank()) {
      staticCdnBaseUrl = cdnBaseUrl;
    } else {
      staticCdnBaseUrl = FALLBACK_CDN;
      logger.warn("aws.s3.cdn-base-url not configured, using fallback: {}", FALLBACK_CDN);
    }
    logger.info("CDN URL resolver initialized with base: {}", staticCdnBaseUrl);
  }

  /**
   * Resolves any stored URL to a full public CDN URL. Handles: raw S3 keys, full S3 URLs, local
   * paths, already-CDN URLs, nulls.
   */
  public static String resolve(String url) {
    if (url == null || url.isBlank()) {
      return url;
    }

    String base =
        staticCdnBaseUrl != null
            ? (staticCdnBaseUrl.endsWith("/")
                ? staticCdnBaseUrl.substring(0, staticCdnBaseUrl.length() - 1)
                : staticCdnBaseUrl)
            : FALLBACK_CDN;

    // Already CloudFront — return as-is
    if (url.contains("cloudfront.net")) {
      return url;
    }

    // Full S3 URL (https://bucket.s3.region.amazonaws.com/key) — extract path, prepend CDN
    if (url.contains(S3_HOST_PATTERN)) {
      int pathStart = url.indexOf("/", url.indexOf(S3_HOST_PATTERN));
      if (pathStart != -1) {
        return base + url.substring(pathStart);
      }
      return url;
    }

    // Local API path — return as-is (non-S3 mode)
    if (url.startsWith("/api/")) {
      return url;
    }

    // Raw S3 key — prepend CDN base
    return base + "/" + url;
  }
}
