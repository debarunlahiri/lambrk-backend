package com.lambrk.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CustomMetrics {

  private final MeterRegistry meterRegistry;

  public CustomMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void recordPostCreated(String community) {
    meterRegistry.counter("lambrk.posts.created", "community", community).increment();
  }

  public void recordCommentCreated(String community) {
    meterRegistry.counter("lambrk.comments.created", "community", community).increment();
  }

  public void recordVoteCast(String voteType) {
    meterRegistry.counter("lambrk.votes.cast", "type", voteType).increment();
  }

  public void recordUserLogin(String userId) {
    meterRegistry.counter("lambrk.users.login", "userId", userId).increment();
  }

  public void recordUserRegistration() {
    meterRegistry.counter("lambrk.users.registered").increment();
  }

  public void recordModeration(String contentType, boolean approved) {
    meterRegistry
        .counter(
            "lambrk.moderation.result", "type", contentType, "approved", String.valueOf(approved))
        .increment();
  }

  public void recordModerationError(String contentType) {
    meterRegistry.counter("lambrk.moderation.error", "type", contentType).increment();
  }

  public void recordRecommendationError() {
    meterRegistry.counter("lambrk.recommendation.error").increment();
  }

  public void recordFeedGeneration(int postCount, int userCount) {
    meterRegistry
        .counter(
            "lambrk.feed.generated",
            "posts",
            String.valueOf(postCount),
            "users",
            String.valueOf(userCount))
        .increment();
  }

  public void recordFeedError() {
    meterRegistry.counter("lambrk.feed.error").increment();
  }

  public void recordCommunityCreated() {
    meterRegistry.counter("lambrk.communities.created").increment();
  }

  public void recordCommunitySubscription(boolean subscribed) {
    meterRegistry
        .counter(
            "lambrk.communities.subscription", "action", subscribed ? "subscribe" : "unsubscribe")
        .increment();
  }

  public void recordSearchQuery(String type) {
    meterRegistry.counter("lambrk.search.queries", "type", type).increment();
  }

  public void recordNotificationCreated(String type) {
    meterRegistry.counter("lambrk.notifications.created", "type", type).increment();
  }

  public void recordNotificationRead() {
    meterRegistry.counter("lambrk.notifications.read").increment();
  }

  public void recordAdminAction(String action) {
    meterRegistry.counter("lambrk.admin.actions", "type", action).increment();
  }

  public void recordFileUpload(String type) {
    meterRegistry.counter("lambrk.files.uploaded", "type", type).increment();
  }

  public void recordWebSocketConnection(String type) {
    meterRegistry.counter("lambrk.websocket.connections", "type", type).increment();
  }

  public Timer.Sample startTimer() {
    return Timer.start(meterRegistry);
  }

  public void stopTimer(Timer.Sample sample, String name) {
    sample.stop(meterRegistry.timer(name));
  }
}
