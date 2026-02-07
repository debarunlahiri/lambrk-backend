package com.lambrk.service;

import com.lambrk.domain.Subreddit;
import com.lambrk.domain.User;
import com.lambrk.dto.SubredditCreateRequest;
import com.lambrk.dto.SubredditResponse;
import com.lambrk.exception.DuplicateResourceException;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.repository.SubredditRepository;
import com.lambrk.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final CustomMetrics customMetrics;

    public SubredditService(SubredditRepository subredditRepository,
                           UserRepository userRepository,
                           CustomMetrics customMetrics) {
        this.subredditRepository = subredditRepository;
        this.userRepository = userRepository;
        this.customMetrics = customMetrics;
    }

    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    @CacheEvict(value = {"subreddits", "trendingSubreddits"}, allEntries = true)
    public SubredditResponse createSubreddit(SubredditCreateRequest request, Long creatorId) {
        if (subredditRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Subreddit", "name", request.name());
        }

        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId));

        Subreddit subreddit = new Subreddit(
            null, request.name(), request.title(), request.description(), request.sidebarText(),
            null, null, request.isPublic(), request.isRestricted(), request.isOver18(),
            1, 1, 0, new HashSet<>(), Set.of(creator), Set.of(creator), creator,
            Instant.now(), Instant.now()
        );

        Subreddit saved = subredditRepository.save(subreddit);
        customMetrics.recordSubredditCreated();
        return SubredditResponse.from(saved, true, true);
    }

    @Cacheable(value = "subreddits", key = "#subredditId")
    @Transactional(readOnly = true)
    public SubredditResponse getSubreddit(Long subredditId, Long currentUserId) {
        Subreddit sub = subredditRepository.findById(subredditId)
            .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "id", subredditId));
        boolean subscribed = false;
        boolean moderator = false;
        if (currentUserId != null) {
            Set<Subreddit> userSubs = subredditRepository.findSubscribedSubredditsByUser(currentUserId);
            subscribed = userSubs.stream().anyMatch(s -> s.id().equals(subredditId));
            Set<Subreddit> modSubs = subredditRepository.findModeratedSubredditsByUser(currentUserId);
            moderator = modSubs.stream().anyMatch(s -> s.id().equals(subredditId));
        }
        return SubredditResponse.from(sub, subscribed, moderator);
    }

    @Transactional(readOnly = true)
    public SubredditResponse getSubredditByName(String name, Long currentUserId) {
        Subreddit sub = subredditRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "name", name));
        return getSubreddit(sub.id(), currentUserId);
    }

    @Cacheable(value = "trendingSubreddits", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<SubredditResponse> getTrendingSubreddits(Pageable pageable) {
        return subredditRepository.findTrendingSubreddits(pageable)
            .map(SubredditResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<SubredditResponse> getPublicSubreddits(Pageable pageable) {
        return subredditRepository.findPublicSubreddits(pageable)
            .map(SubredditResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<SubredditResponse> searchSubreddits(String query, Pageable pageable) {
        customMetrics.recordSearchQuery("subreddit");
        return subredditRepository.searchSubreddits(query, pageable)
            .map(SubredditResponse::from);
    }

    @CacheEvict(value = {"subreddits", "trendingSubreddits"}, allEntries = true)
    public SubredditResponse updateSubreddit(Long subredditId, SubredditCreateRequest request, Long currentUserId) {
        Subreddit sub = subredditRepository.findById(subredditId)
            .orElseThrow(() -> new ResourceNotFoundException("Subreddit", "id", subredditId));

        Set<Subreddit> modSubs = subredditRepository.findModeratedSubredditsByUser(currentUserId);
        boolean isMod = modSubs.stream().anyMatch(s -> s.id().equals(subredditId));
        if (!isMod) {
            throw new UnauthorizedActionException("Only moderators can update subreddit settings");
        }

        Subreddit updated = new Subreddit(
            sub.id(), sub.name(), request.title(), request.description(), request.sidebarText(),
            sub.headerImageUrl(), sub.iconImageUrl(), request.isPublic(), request.isRestricted(),
            request.isOver18(), sub.memberCount(), sub.subscriberCount(), sub.activeUserCount(),
            sub.posts(), sub.members(), sub.moderators(), sub.createdBy(),
            sub.createdAt(), Instant.now()
        );
        Subreddit saved = subredditRepository.save(updated);
        return SubredditResponse.from(saved, true, true);
    }

    @CacheEvict(value = {"subreddits"}, allEntries = true)
    public void subscribe(Long subredditId, Long userId) {
        subredditRepository.updateSubscriberCount(subredditId, 1);
        subredditRepository.updateMemberCount(subredditId, 1);
        customMetrics.recordSubredditSubscription(true);
    }

    @CacheEvict(value = {"subreddits"}, allEntries = true)
    public void unsubscribe(Long subredditId, Long userId) {
        subredditRepository.updateSubscriberCount(subredditId, -1);
        subredditRepository.updateMemberCount(subredditId, -1);
        customMetrics.recordSubredditSubscription(false);
    }

    @Transactional(readOnly = true)
    public Set<SubredditResponse> getUserSubscriptions(Long userId) {
        return subredditRepository.findSubscribedSubredditsByUser(userId).stream()
            .map(s -> SubredditResponse.from(s, true, false))
            .collect(java.util.stream.Collectors.toSet());
    }
}
