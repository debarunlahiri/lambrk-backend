package com.lambrk.controller;

import com.lambrk.dto.CommunityCreateRequest;
import com.lambrk.dto.CommunityResponse;
import com.lambrk.service.CommunityService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.lambrk.config.UserPrincipal;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;

    public CommunityController(CommunityService communityService) {
        this.communityService = communityService;
    }

    @PostMapping
    @NewSpan("create-community")
    @Counted(value = "communities.created")
    @Timed(value = "communities.create.duration")
    public ResponseEntity<CommunityResponse> createCommunity(
            @Valid @RequestBody CommunityCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.createCommunity(request, getUserId(userDetails)));
    }

    @GetMapping("/{communityId}")
    @NewSpan("get-community")
    @Timed(value = "communities.get.duration")
    public ResponseEntity<CommunityResponse> getCommunity(
            @PathVariable @SpanTag UUID communityId,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.getCommunity(communityId, getUserId(userDetails)));
    }

    @GetMapping("/r/{name}")
    @NewSpan("get-community-by-name")
    @Timed(value = "communities.getByName.duration")
    public ResponseEntity<CommunityResponse> getCommunityByName(
            @PathVariable @SpanTag String name,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.getCommunityByName(name, getUserId(userDetails)));
    }

    @GetMapping("/trending")
    @NewSpan("get-trending-communities")
    @Timed(value = "communities.trending.duration")
    public ResponseEntity<Page<CommunityResponse>> getTrendingCommunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(communityService.getTrendingCommunities(pageable));
    }

    @GetMapping
    @NewSpan("get-public-communities")
    @Timed(value = "communities.public.duration")
    public ResponseEntity<Page<CommunityResponse>> getPublicCommunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "subscriberCount"));
        return ResponseEntity.ok(communityService.getPublicCommunities(pageable));
    }

    @GetMapping("/search")
    @NewSpan("search-communities")
    @Timed(value = "communities.search.duration")
    public ResponseEntity<Page<CommunityResponse>> searchCommunities(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(communityService.searchCommunities(query, pageable));
    }

    @PutMapping("/{communityId}")
    @NewSpan("update-community")
    @Timed(value = "communities.update.duration")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public ResponseEntity<CommunityResponse> updateCommunity(
            @PathVariable @SpanTag UUID communityId,
            @Valid @RequestBody CommunityCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.updateCommunity(communityId, request, getUserId(userDetails)));
    }

    @PostMapping("/{communityId}/subscribe")
    @NewSpan("subscribe-community")
    @Timed(value = "communities.subscribe.duration")
    public ResponseEntity<CommunityResponse> subscribe(
            @PathVariable @SpanTag UUID communityId,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.subscribe(communityId, getUserId(userDetails)));
    }

    @PostMapping("/{communityId}/unsubscribe")
    @NewSpan("unsubscribe-community")
    @Timed(value = "communities.unsubscribe.duration")
    public ResponseEntity<CommunityResponse> unsubscribe(
            @PathVariable @SpanTag UUID communityId,
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.unsubscribe(communityId, getUserId(userDetails)));
    }

    @GetMapping("/user/subscriptions")
    @NewSpan("get-user-subscriptions")
    @Timed(value = "communities.subscriptions.duration")
    public ResponseEntity<Set<CommunityResponse>> getUserSubscriptions(
            @AuthenticationPrincipal UserPrincipal userDetails) {
        return ResponseEntity.ok(communityService.getUserSubscriptions(getUserId(userDetails)));
    }

    private UUID getUserId(UserPrincipal userPrincipal) {
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }
}
