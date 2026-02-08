package com.lambrk.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordPostCreated(String subreddit) {
        meterRegistry.counter("reddit.posts.created", "subreddit", subreddit).increment();
    }

    public void recordCommentCreated(String subreddit) {
        meterRegistry.counter("reddit.comments.created", "subreddit", subreddit).increment();
    }

    public void recordVoteCast(String voteType) {
        meterRegistry.counter("reddit.votes.cast", "type", voteType).increment();
    }

    public void recordUserLogin(String userId) {
        meterRegistry.counter("reddit.users.login", "userId", userId).increment();
    }

    public void recordUserRegistration() {
        meterRegistry.counter("reddit.users.registered").increment();
    }

    public void recordModeration(String contentType, boolean approved) {
        meterRegistry.counter("reddit.moderation.result",
            "type", contentType,
            "approved", String.valueOf(approved)).increment();
    }

    public void recordModerationError(String contentType) {
        meterRegistry.counter("reddit.moderation.error", "type", contentType).increment();
    }

    public void recordRecommendationError() {
        meterRegistry.counter("lambrk.recommendation.error").increment();
    }

    public void recordFeedGeneration(int postCount, int userCount) {
        meterRegistry.counter("lambrk.feed.generated", "posts", String.valueOf(postCount), "users", String.valueOf(userCount)).increment();
    }

    public void recordFeedError() {
        meterRegistry.counter("lambrk.feed.error").increment();
    }

    public void recordSubredditCreated() {
        meterRegistry.counter("reddit.subreddits.created").increment();
    }

    public void recordSubredditSubscription(boolean subscribed) {
        meterRegistry.counter("reddit.subreddits.subscription",
            "action", subscribed ? "subscribe" : "unsubscribe").increment();
    }

    public void recordSearchQuery(String type) {
        meterRegistry.counter("reddit.search.queries", "type", type).increment();
    }

    public void recordNotificationCreated(String type) {
        meterRegistry.counter("reddit.notifications.created", "type", type).increment();
    }

    public void recordNotificationRead() {
        meterRegistry.counter("reddit.notifications.read").increment();
    }

    public void recordAdminAction(String action) {
        meterRegistry.counter("reddit.admin.actions", "type", action).increment();
    }

    public void recordFileUpload(String type) {
        meterRegistry.counter("reddit.files.uploaded", "type", type).increment();
    }

    public void recordWebSocketConnection(String type) {
        meterRegistry.counter("reddit.websocket.connections", "type", type).increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String name) {
        sample.stop(meterRegistry.timer(name));
    }
}
