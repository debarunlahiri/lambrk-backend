package com.lambrk.controller;

import com.lambrk.dto.UserResponse;
import com.lambrk.dto.UserUpdateRequest;
import com.lambrk.dto.FriendRequestCreateRequest;
import com.lambrk.dto.FriendRequestResponse;
import com.lambrk.dto.SocialStatsResponse;
import com.lambrk.dto.SocialUserResponse;
import com.lambrk.dto.UserPrivacySettingsRequest;
import com.lambrk.dto.UserPrivacySettingsResponse;
import com.lambrk.domain.User;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.repository.UserRepository;
import com.lambrk.service.UserSocialService;
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
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserSocialService userSocialService;

    public UserController(UserRepository userRepository, UserSocialService userSocialService) {
        this.userRepository = userRepository;
        this.userSocialService = userSocialService;
    }

    @GetMapping("/{userId}")
    @NewSpan("get-user")
    @Timed(value = "users.get.duration")
    public ResponseEntity<SocialUserResponse> getUser(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return ResponseEntity.ok(userSocialService.toSocialUserResponse(user, getUserId(userPrincipal)));
    }

    @GetMapping("/username/{username}")
    @NewSpan("get-user-by-username")
    @Timed(value = "users.getByUsername.duration")
    public ResponseEntity<SocialUserResponse> getUserByUsername(
            @PathVariable @SpanTag String username,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return ResponseEntity.ok(userSocialService.toSocialUserResponse(user, getUserId(userPrincipal)));
    }

    @GetMapping("/me")
    @NewSpan("get-current-user")
    @Timed(value = "users.me.duration")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findByUsername(userPrincipal.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", userPrincipal.getUsername()));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PutMapping("/me")
    @NewSpan("update-current-user")
    @Timed(value = "users.me.update.duration")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findByUsername(userPrincipal.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", userPrincipal.getUsername()));

        if (request.displayName() != null) user.setDisplayName(request.displayName());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.location() != null) user.setLocation(request.location());
        if (request.website() != null) user.setWebsite(request.website());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.headerImageUrl() != null) user.setHeaderImageUrl(request.headerImageUrl());
        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);
        return ResponseEntity.ok(UserResponse.from(saved));
    }

    @GetMapping("/me/privacy")
    @NewSpan("get-current-user-privacy-settings")
    @Timed(value = "users.me.privacy.duration")
    public ResponseEntity<UserPrivacySettingsResponse> getCurrentUserPrivacySettings(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findByUsername(userPrincipal.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", userPrincipal.getUsername()));
        return ResponseEntity.ok(UserPrivacySettingsResponse.from(user));
    }

    @PutMapping("/me/privacy")
    @NewSpan("update-current-user-privacy-settings")
    @Timed(value = "users.me.privacy.update.duration")
    public ResponseEntity<UserPrivacySettingsResponse> updateCurrentUserPrivacySettings(
            @Valid @RequestBody UserPrivacySettingsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        User saved = userSocialService.updatePrivacySettings(userPrincipal.getUserId(), request);
        return ResponseEntity.ok(UserPrivacySettingsResponse.from(saved));
    }

    @GetMapping("/top")
    @NewSpan("get-top-users")
    @Timed(value = "users.top.duration")
    public ResponseEntity<Page<UserResponse>> getTopUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "karma"));
        return ResponseEntity.ok(userRepository.findTopUsersByKarma(pageable).map(UserResponse::from));
    }

    @GetMapping("/search")
    @NewSpan("search-users")
    @Timed(value = "users.search.duration")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam @SpanTag String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userRepository.searchActiveUsers(query, pageable).map(UserResponse::from));
    }

    @PostMapping("/{userId}/follow")
    @NewSpan("follow-user")
    @Timed(value = "users.follow.duration")
    public ResponseEntity<Void> followUser(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(required = false) String source,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userSocialService.follow(userPrincipal.getUserId(), userId, source);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/follow")
    @NewSpan("unfollow-user")
    @Timed(value = "users.unfollow.duration")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userSocialService.unfollow(userPrincipal.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    @NewSpan("get-user-followers")
    @Timed(value = "users.followers.duration")
    public ResponseEntity<Page<SocialUserResponse>> getFollowers(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userSocialService.getFollowers(userId, getUserId(userPrincipal), pageable));
    }

    @GetMapping("/{userId}/following")
    @NewSpan("get-user-following")
    @Timed(value = "users.following.duration")
    public ResponseEntity<Page<SocialUserResponse>> getFollowing(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userSocialService.getFollowing(userId, getUserId(userPrincipal), pageable));
    }

    @GetMapping("/{userId}/friends")
    @NewSpan("get-user-friends")
    @Timed(value = "users.friends.duration")
    public ResponseEntity<Page<SocialUserResponse>> getFriends(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userSocialService.getFriends(userId, getUserId(userPrincipal), pageable));
    }

    @GetMapping("/{userId}/mutual/followers")
    @NewSpan("get-mutual-followers")
    @Timed(value = "users.mutual.followers.duration")
    public ResponseEntity<Page<SocialUserResponse>> getMutualFollowers(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(required = false) UUID withUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID comparisonUserId = getComparisonUserId(withUserId, userPrincipal);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userSocialService.getMutualFollowers(userId, comparisonUserId, getUserId(userPrincipal), pageable));
    }

    @GetMapping("/{userId}/mutual/following")
    @NewSpan("get-mutual-following")
    @Timed(value = "users.mutual.following.duration")
    public ResponseEntity<Page<SocialUserResponse>> getMutualFollowing(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(required = false) UUID withUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID comparisonUserId = getComparisonUserId(withUserId, userPrincipal);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userSocialService.getMutualFollowing(userId, comparisonUserId, getUserId(userPrincipal), pageable));
    }

    @GetMapping("/{userId}/mutual/friends")
    @NewSpan("get-mutual-friends")
    @Timed(value = "users.mutual.friends.duration")
    public ResponseEntity<Page<SocialUserResponse>> getMutualFriends(
            @PathVariable @SpanTag UUID userId,
            @RequestParam(required = false) UUID withUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        UUID comparisonUserId = getComparisonUserId(withUserId, userPrincipal);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userSocialService.getMutualFriends(userId, comparisonUserId, getUserId(userPrincipal), pageable));
    }

    @GetMapping("/{userId}/social-stats")
    @NewSpan("get-user-social-stats")
    @Timed(value = "users.socialStats.duration")
    public ResponseEntity<SocialStatsResponse> getSocialStats(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(userSocialService.getStats(userId, getUserId(userPrincipal)));
    }

    @PostMapping("/{userId}/friend-request")
    @NewSpan("send-friend-request")
    @Timed(value = "users.friendRequest.duration")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @PathVariable @SpanTag UUID userId,
            @Valid @RequestBody(required = false) FriendRequestCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(FriendRequestResponse.from(
            userSocialService.sendFriendRequest(userPrincipal.getUserId(), userId, request)
        ));
    }

    @PostMapping("/{userId}/friend-request/accept")
    @NewSpan("accept-friend-request")
    @Timed(value = "users.friendRequest.accept.duration")
    public ResponseEntity<FriendRequestResponse> acceptFriendRequest(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(FriendRequestResponse.from(
            userSocialService.acceptFriendRequest(userPrincipal.getUserId(), userId)
        ));
    }

    @PostMapping("/{userId}/friend-request/decline")
    @NewSpan("decline-friend-request")
    @Timed(value = "users.friendRequest.decline.duration")
    public ResponseEntity<FriendRequestResponse> declineFriendRequest(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(FriendRequestResponse.from(
            userSocialService.declineFriendRequest(userPrincipal.getUserId(), userId)
        ));
    }

    @DeleteMapping("/{userId}/friend-request")
    @NewSpan("cancel-friend-request")
    @Timed(value = "users.friendRequest.cancel.duration")
    public ResponseEntity<FriendRequestResponse> cancelFriendRequest(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(FriendRequestResponse.from(
            userSocialService.cancelFriendRequest(userPrincipal.getUserId(), userId)
        ));
    }

    @DeleteMapping("/{userId}/friend")
    @NewSpan("remove-friend")
    @Timed(value = "users.friend.remove.duration")
    public ResponseEntity<Void> removeFriend(
            @PathVariable @SpanTag UUID userId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userSocialService.removeFriend(userPrincipal.getUserId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/friend-requests/incoming")
    @NewSpan("get-incoming-friend-requests")
    @Timed(value = "users.friendRequests.incoming.duration")
    public ResponseEntity<Page<FriendRequestResponse>> getIncomingFriendRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userSocialService.getIncomingFriendRequests(userPrincipal.getUserId(), pageable));
    }

    @GetMapping("/me/friend-requests/outgoing")
    @NewSpan("get-outgoing-friend-requests")
    @Timed(value = "users.friendRequests.outgoing.duration")
    public ResponseEntity<Page<FriendRequestResponse>> getOutgoingFriendRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(userSocialService.getOutgoingFriendRequests(userPrincipal.getUserId(), pageable));
    }

    @DeleteMapping("/{userId}")
    @NewSpan("delete-user")
    @Timed(value = "users.delete.duration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable @SpanTag UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    private UUID getUserId(UserPrincipal userPrincipal) {
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }

    private UUID getComparisonUserId(UUID withUserId, UserPrincipal userPrincipal) {
        if (withUserId != null) {
            return withUserId;
        }
        if (userPrincipal == null) {
            throw new UnauthorizedActionException("withUserId is required for anonymous requests");
        }
        return userPrincipal.getUserId();
    }
}
