package com.lambrk.service;

import com.lambrk.domain.Category;
import com.lambrk.domain.Community;
import com.lambrk.domain.User;
import com.lambrk.dto.CommunityCreateRequest;
import com.lambrk.dto.CommunityResponse;
import com.lambrk.exception.DuplicateResourceException;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.repository.CategoryRepository;
import com.lambrk.repository.CommunityRepository;
import com.lambrk.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lambrk.util.UuidV7Generator;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CustomMetrics customMetrics;

    public CommunityService(CommunityRepository communityRepository,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           CustomMetrics customMetrics) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.customMetrics = customMetrics;
    }

    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    @CacheEvict(value = {"communities", "trendingCommunities"}, allEntries = true)
    public CommunityResponse createCommunity(CommunityCreateRequest request, UUID creatorId) {
        if (communityRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Community", "name", request.name());
        }

        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", creatorId));

        Set<Category> categorySet = request.categoryIds() != null
            ? request.categoryIds().stream()
                .map(id -> categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id)))
                .collect(Collectors.toSet())
            : new HashSet<>();

        Community community = new Community(
            UuidV7Generator.generate(), request.name(), request.title(), request.description(), request.sidebarText(),
            null, null, request.isPublic(), request.isRestricted(), request.isOver18(),
            1, 1, 0, new HashSet<>(), Set.of(creator), Set.of(creator), categorySet, creator,
            Instant.now(), Instant.now()
        );

        Community saved = communityRepository.save(community);
        customMetrics.recordCommunityCreated();
        return CommunityResponse.from(saved, true, true);
    }

    @Cacheable(value = "communities", key = "#communityId")
    @Transactional(readOnly = true)
    public CommunityResponse getCommunity(UUID communityId, UUID currentUserId) {
        Community sub = communityRepository.findById(communityId)
            .orElseThrow(() -> new ResourceNotFoundException("Community", "id", communityId));
        boolean subscribed = false;
        boolean moderator = false;
        if (currentUserId != null) {
            Set<Community> userSubs = communityRepository.findSubscribedCommunitiesByUser(currentUserId);
            subscribed = userSubs.stream().anyMatch(s -> s.getId().equals(communityId));
            Set<Community> modSubs = communityRepository.findModeratedCommunitiesByUser(currentUserId);
            moderator = modSubs.stream().anyMatch(s -> s.getId().equals(communityId));
        }
        return CommunityResponse.from(sub, subscribed, moderator);
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityByName(String name, UUID currentUserId) {
        Community sub = communityRepository.findByName(name)
            .orElseThrow(() -> new ResourceNotFoundException("Community", "name", name));
        return getCommunity(sub.getId(), currentUserId);
    }

    @Cacheable(value = "trendingCommunities", key = "#pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<CommunityResponse> getTrendingCommunities(Pageable pageable) {
        return communityRepository.findTrendingCommunities(pageable)
            .map(CommunityResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<CommunityResponse> getPublicCommunities(Pageable pageable) {
        return communityRepository.findPublicCommunities(pageable)
            .map(CommunityResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<CommunityResponse> searchCommunities(String query, Pageable pageable) {
        customMetrics.recordSearchQuery("community");
        return communityRepository.searchCommunities(query, pageable)
            .map(CommunityResponse::from);
    }

    @CacheEvict(value = {"communities", "trendingCommunities"}, allEntries = true)
    public CommunityResponse updateCommunity(UUID communityId, CommunityCreateRequest request, UUID currentUserId) {
        Community sub = communityRepository.findById(communityId)
            .orElseThrow(() -> new ResourceNotFoundException("Community", "id", communityId));

        Set<Community> modSubs = communityRepository.findModeratedCommunitiesByUser(currentUserId);
        boolean isMod = modSubs.stream().anyMatch(s -> s.getId().equals(communityId));
        if (!isMod) {
            throw new UnauthorizedActionException("Only moderators can update community settings");
        }

        Set<Category> categorySet = request.categoryIds() != null
            ? request.categoryIds().stream()
                .map(id -> categoryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id)))
                .collect(Collectors.toSet())
            : sub.getCategories();

        Community updated = new Community(
            sub.getId(), sub.getName(), request.title(), request.description(), request.sidebarText(),
            sub.getHeaderImageUrl(), sub.getIconImageUrl(), request.isPublic(), request.isRestricted(),
            request.isOver18(), sub.getMemberCount(), sub.getSubscriberCount(), sub.getActiveUserCount(),
            sub.getPosts(), sub.getMembers(), sub.getModerators(), categorySet, sub.getCreatedBy(),
            sub.getCreatedAt(), Instant.now()
        );
        Community saved = communityRepository.save(updated);
        return CommunityResponse.from(saved, true, true);
    }

    @CacheEvict(value = {"communities"}, allEntries = true)
    public void subscribe(UUID communityId, UUID userId) {
        communityRepository.updateSubscriberCount(communityId, 1);
        communityRepository.updateMemberCount(communityId, 1);
        customMetrics.recordCommunitySubscription(true);
    }

    @CacheEvict(value = {"communities"}, allEntries = true)
    public void unsubscribe(UUID communityId, UUID userId) {
        communityRepository.updateSubscriberCount(communityId, -1);
        communityRepository.updateMemberCount(communityId, -1);
        customMetrics.recordCommunitySubscription(false);
    }

    @Transactional(readOnly = true)
    public Set<CommunityResponse> getUserSubscriptions(UUID userId) {
        return communityRepository.findSubscribedCommunitiesByUser(userId).stream()
            .map(s -> CommunityResponse.from(s, true, false))
            .collect(java.util.stream.Collectors.toSet());
    }
}
