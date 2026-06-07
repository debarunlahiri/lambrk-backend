package com.lambrk.service;

import com.lambrk.domain.User;
import com.lambrk.domain.UserFollow;
import com.lambrk.domain.UserFriendship;
import com.lambrk.dto.FriendRequestCreateRequest;
import com.lambrk.dto.FriendRequestResponse;
import com.lambrk.dto.SocialStatsResponse;
import com.lambrk.dto.SocialUserResponse;
import com.lambrk.exception.ResourceNotFoundException;
import com.lambrk.exception.UnauthorizedActionException;
import com.lambrk.repository.UserFollowRepository;
import com.lambrk.repository.UserFriendshipRepository;
import com.lambrk.repository.UserRepository;
import com.lambrk.util.UuidV7Generator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserSocialService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserFriendshipRepository userFriendshipRepository;
    private final NotificationService notificationService;

    public UserSocialService(
        UserRepository userRepository,
        UserFollowRepository userFollowRepository,
        UserFriendshipRepository userFriendshipRepository,
        NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.userFriendshipRepository = userFriendshipRepository;
        this.notificationService = notificationService;
    }

    public void follow(UUID followerId, UUID followingId, String source) {
        if (followerId.equals(followingId)) {
            throw new UnauthorizedActionException("You cannot follow yourself");
        }

        User follower = getUser(followerId);
        User following = getUser(followingId);
        if (following.isHideFollowButton()) {
            throw new UnauthorizedActionException("Follow is not available for this user");
        }

        Optional<UserFollow> existingFollow = userFollowRepository.findByFollowerAndFollowing(follower, following);
        boolean shouldNotify = existingFollow.isEmpty();

        UserFollow follow = existingFollow
            .orElseGet(() -> new UserFollow(UuidV7Generator.generate(), follower, following, source));
        follow.activate(source);
        userFollowRepository.save(follow);
        if (shouldNotify) {
            notifySafely(() -> notificationService.createFollowNotification(followerId, followingId));
        }
    }

    public void unfollow(UUID followerId, UUID followingId) {
        User follower = getUser(followerId);
        User following = getUser(followingId);
        userFollowRepository.findByFollowerAndFollowing(follower, following).ifPresent(follow -> {
            follow.remove();
            userFollowRepository.save(follow);
        });
    }

    public UserFriendship sendFriendRequest(UUID requesterId, UUID addresseeId, FriendRequestCreateRequest request) {
        if (requesterId.equals(addresseeId)) {
            throw new UnauthorizedActionException("You cannot add yourself as a friend");
        }

        User requester = getUser(requesterId);
        User addressee = getUser(addresseeId);
        if (addressee.isHideAddFriendButton()) {
            throw new UnauthorizedActionException("Friend requests are not available for this user");
        }
        OrderedUsers orderedUsers = orderUsers(requester, addressee);

        Optional<UserFriendship> existing = userFriendshipRepository.findByUserOneAndUserTwo(
            orderedUsers.userOne(),
            orderedUsers.userTwo()
        );

        if (existing.isPresent()) {
            UserFriendship friendship = existing.get();
            if (friendship.getStatus() == UserFriendship.FriendshipStatus.ACCEPTED) {
                return friendship;
            }
            if (friendship.getStatus() == UserFriendship.FriendshipStatus.BLOCKED) {
                throw new UnauthorizedActionException("Friend request is not available for this user");
            }
            friendship.requestAgain(requester, addressee, request != null ? request.source() : null, request != null ? request.message() : null);
            UserFriendship saved = userFriendshipRepository.save(friendship);
            notifySafely(() -> notificationService.createFriendRequestNotification(requesterId, addresseeId));
            return saved;
        }

        UserFriendship friendship = new UserFriendship(
            UuidV7Generator.generate(),
            orderedUsers.userOne(),
            orderedUsers.userTwo(),
            requester,
            addressee,
            request != null ? request.source() : null,
            request != null ? request.message() : null
        );
        UserFriendship saved = userFriendshipRepository.save(friendship);
        notifySafely(() -> notificationService.createFriendRequestNotification(requesterId, addresseeId));
        return saved;
    }

    public UserFriendship acceptFriendRequest(UUID currentUserId, UUID requesterId) {
        User currentUser = getUser(currentUserId);
        User requester = getUser(requesterId);
        UserFriendship friendship = getFriendship(currentUser, requester);
        requirePendingAddressee(friendship, currentUserId);
        friendship.accept(currentUser);
        UserFriendship saved = userFriendshipRepository.save(friendship);
        notifySafely(() -> notificationService.createFriendRequestAcceptedNotification(currentUserId, requesterId));
        return saved;
    }

    public UserFriendship declineFriendRequest(UUID currentUserId, UUID requesterId) {
        User currentUser = getUser(currentUserId);
        User requester = getUser(requesterId);
        UserFriendship friendship = getFriendship(currentUser, requester);
        requirePendingAddressee(friendship, currentUserId);
        friendship.decline(currentUser);
        return userFriendshipRepository.save(friendship);
    }

    public UserFriendship cancelFriendRequest(UUID currentUserId, UUID addresseeId) {
        User currentUser = getUser(currentUserId);
        User addressee = getUser(addresseeId);
        UserFriendship friendship = getFriendship(currentUser, addressee);
        if (friendship.getStatus() != UserFriendship.FriendshipStatus.PENDING
            || !friendship.getRequester().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("Only the requester can cancel this friend request");
        }
        friendship.cancel(currentUser);
        return userFriendshipRepository.save(friendship);
    }

    public void removeFriend(UUID currentUserId, UUID friendId) {
        User currentUser = getUser(currentUserId);
        User friend = getUser(friendId);
        UserFriendship friendship = getFriendship(currentUser, friend);
        if (friendship.getStatus() != UserFriendship.FriendshipStatus.ACCEPTED) {
            return;
        }
        friendship.remove(currentUser);
        userFriendshipRepository.save(friendship);
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getFollowers(UUID userId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        if (!canViewFollowerList(user, currentUserId)) {
            return Page.empty(pageable);
        }
        return filterHiddenMutualUsers(userFollowRepository.findFollowers(user, pageable), currentUserId, pageable)
            .map(follower -> toSocialUserResponse(follower, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getFollowing(UUID userId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        if (!canViewFollowingList(user, currentUserId)) {
            return Page.empty(pageable);
        }
        return filterHiddenMutualUsers(userFollowRepository.findFollowing(user, pageable), currentUserId, pageable)
            .map(following -> toSocialUserResponse(following, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getFriends(UUID userId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        return userFriendshipRepository.findFriends(user, pageable)
            .map(friend -> toSocialUserResponse(friend, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getMutualFollowers(UUID userId, UUID withUserId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        User withUser = getUser(withUserId);
        if (!canViewFollowerList(user, currentUserId) || !canViewFollowerList(withUser, currentUserId)) {
            return Page.empty(pageable);
        }
        return filterHiddenMutualUsers(userFollowRepository.findMutualFollowers(user, withUser, pageable), currentUserId, pageable)
            .map(mutualUser -> toSocialUserResponse(mutualUser, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getMutualFollowing(UUID userId, UUID withUserId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        User withUser = getUser(withUserId);
        if (!canViewFollowingList(user, currentUserId) || !canViewFollowingList(withUser, currentUserId)) {
            return Page.empty(pageable);
        }
        return filterHiddenMutualUsers(userFollowRepository.findMutualFollowing(user, withUser, pageable), currentUserId, pageable)
            .map(mutualUser -> toSocialUserResponse(mutualUser, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getMutualFriends(UUID userId, UUID withUserId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        User withUser = getUser(withUserId);
        return filterHiddenMutualUsers(userFriendshipRepository.findMutualFriends(user, withUser, pageable), currentUserId, pageable)
            .map(mutualUser -> toSocialUserResponse(mutualUser, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<FriendRequestResponse> getIncomingFriendRequests(UUID currentUserId, Pageable pageable) {
        User user = getUser(currentUserId);
        return userFriendshipRepository.findIncomingRequests(user, pageable)
            .map(FriendRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<FriendRequestResponse> getOutgoingFriendRequests(UUID currentUserId, Pageable pageable) {
        User user = getUser(currentUserId);
        return userFriendshipRepository.findOutgoingRequests(user, pageable)
            .map(FriendRequestResponse::from);
    }

    @Transactional(readOnly = true)
    public SocialStatsResponse getStats(UUID userId) {
        User user = getUser(userId);
        return new SocialStatsResponse(
            userFollowRepository.countByFollowingAndStatus(user, UserFollow.FollowStatus.ACTIVE),
            userFollowRepository.countByFollowerAndStatus(user, UserFollow.FollowStatus.ACTIVE),
            userFriendshipRepository.countByUserOneAndStatusOrUserTwoAndStatus(
                user,
                UserFriendship.FriendshipStatus.ACCEPTED,
                user,
                UserFriendship.FriendshipStatus.ACCEPTED
            )
        );
    }

    @Transactional(readOnly = true)
    public SocialStatsResponse getStats(UUID userId, UUID currentUserId) {
        User user = getUser(userId);
        SocialStatsResponse stats = getStats(userId);
        boolean owner = currentUserId != null && currentUserId.equals(user.getId());
        boolean friend = isFriend(currentUserId, user);
        return new SocialStatsResponse(
            canViewFollowerCount(user, currentUserId, friend, owner) ? stats.followerCount() : 0,
            canViewFollowingCount(user, currentUserId, friend, owner) ? stats.followingCount() : 0,
            stats.friendCount()
        );
    }

    @Transactional(readOnly = true)
    public SocialUserResponse toSocialUserResponse(User user, UUID currentUserId) {
        SocialStatsResponse stats = getStats(user.getId());
        boolean followedByCurrentUser = false;
        boolean followingCurrentUser = false;
        boolean friend = false;
        String friendshipStatus = null;
        boolean owner = currentUserId != null && currentUserId.equals(user.getId());

        if (currentUserId != null) {
            User currentUser = getUser(currentUserId);
            followedByCurrentUser = userFollowRepository.existsByFollowerAndFollowingAndStatus(
                currentUser,
                user,
                UserFollow.FollowStatus.ACTIVE
            );
            followingCurrentUser = userFollowRepository.existsByFollowerAndFollowingAndStatus(
                user,
                currentUser,
                UserFollow.FollowStatus.ACTIVE
            );

            if (!currentUserId.equals(user.getId())) {
                Optional<UserFriendship> friendship = findFriendship(currentUser, user);
                if (friendship.isPresent()) {
                    friendshipStatus = friendship.get().getStatus().name();
                    friend = friendship.get().getStatus() == UserFriendship.FriendshipStatus.ACCEPTED;
                }
            }
        }

        return SocialUserResponse.from(
            user,
            stats,
            followedByCurrentUser,
            followingCurrentUser,
            friend,
            friendshipStatus
            ,
            canViewFollowerCount(user, currentUserId, friend, owner),
            canViewFollowingCount(user, currentUserId, friend, owner),
            canViewFollowerList(user, currentUserId, friend, owner),
            canViewFollowingList(user, currentUserId, friend, owner),
            canShowAddFriendButton(user, currentUserId, friend, owner),
            canShowFollowButton(user, currentUserId, owner),
            canShowInMutualLists(user, currentUserId)
        );
    }

    public User updatePrivacySettings(UUID userId, com.lambrk.dto.UserPrivacySettingsRequest request) {
        User user = getUser(userId);
        if (request.privateAccount() != null) {
            applyPrivateAccountPreset(user, request.privateAccount());
        } else {
            if (request.hideFollowerCount() != null) user.setHideFollowerCount(request.hideFollowerCount());
            if (request.hideFollowingCount() != null) user.setHideFollowingCount(request.hideFollowingCount());
            if (request.hideFollowerList() != null) user.setHideFollowerList(request.hideFollowerList());
            if (request.hideFollowingList() != null) user.setHideFollowingList(request.hideFollowingList());
            if (request.hideAddFriendButton() != null) user.setHideAddFriendButton(request.hideAddFriendButton());
            if (request.hideFollowButton() != null) user.setHideFollowButton(request.hideFollowButton());
            if (request.hideFromMutualList() != null) user.setHideFromMutualList(request.hideFromMutualList());
            if (request.messageButtonEnabled() != null) user.setMessageButtonEnabled(request.messageButtonEnabled());
        }
        user.setUpdatedAt(java.time.Instant.now());
        return userRepository.save(user);
    }

    private void applyPrivateAccountPreset(User user, boolean privateAccount) {
        user.setPrivateAccount(privateAccount);
        user.setHideFollowerCount(privateAccount);
        user.setHideFollowingCount(privateAccount);
        user.setHideFollowerList(privateAccount);
        user.setHideFollowingList(privateAccount);
        user.setHideAddFriendButton(privateAccount);
        user.setHideFollowButton(privateAccount);
        user.setHideFromMutualList(privateAccount);
        user.setMessageButtonEnabled(!privateAccount);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private UserFriendship getFriendship(User first, User second) {
        return findFriendship(first, second)
            .orElseThrow(() -> new ResourceNotFoundException("Friendship", "users", first.getId() + "," + second.getId()));
    }

    private Optional<UserFriendship> findFriendship(User first, User second) {
        OrderedUsers orderedUsers = orderUsers(first, second);
        return userFriendshipRepository.findByUserOneAndUserTwo(orderedUsers.userOne(), orderedUsers.userTwo());
    }

    private boolean canViewFollowerCount(User user, UUID currentUserId, boolean friend, boolean owner) {
        return canViewPrivateAccount(user, currentUserId, friend, owner) && (owner || !user.isHideFollowerCount());
    }

    private boolean canViewFollowingCount(User user, UUID currentUserId, boolean friend, boolean owner) {
        return canViewPrivateAccount(user, currentUserId, friend, owner) && (owner || !user.isHideFollowingCount());
    }

    private boolean canViewFollowerList(User user, UUID currentUserId) {
        boolean owner = currentUserId != null && currentUserId.equals(user.getId());
        return canViewFollowerList(user, currentUserId, isFriend(currentUserId, user), owner);
    }

    private boolean canViewFollowerList(User user, UUID currentUserId, boolean friend, boolean owner) {
        return canViewPrivateAccount(user, currentUserId, friend, owner) && (owner || !user.isHideFollowerList());
    }

    private boolean canViewFollowingList(User user, UUID currentUserId) {
        boolean owner = currentUserId != null && currentUserId.equals(user.getId());
        return canViewFollowingList(user, currentUserId, isFriend(currentUserId, user), owner);
    }

    private boolean canViewFollowingList(User user, UUID currentUserId, boolean friend, boolean owner) {
        return canViewPrivateAccount(user, currentUserId, friend, owner) && (owner || !user.isHideFollowingList());
    }

    private boolean canShowAddFriendButton(User user, UUID currentUserId, boolean friend, boolean owner) {
        return !owner && !friend && canViewPrivateAccount(user, currentUserId, friend, owner) && !user.isHideAddFriendButton();
    }

    private boolean canShowFollowButton(User user, UUID currentUserId, boolean owner) {
        return !owner && !user.isHideFollowButton();
    }

    private boolean canShowInMutualLists(User user, UUID currentUserId) {
        return (currentUserId != null && currentUserId.equals(user.getId())) || !user.isHideFromMutualList();
    }

    private Page<User> filterHiddenMutualUsers(Page<User> users, UUID currentUserId, Pageable pageable) {
        List<User> visibleUsers = users.getContent().stream()
            .filter(user -> canShowInMutualLists(user, currentUserId))
            .toList();
        return new PageImpl<>(visibleUsers, pageable, visibleUsers.size());
    }

    private boolean canViewPrivateAccount(User user, UUID currentUserId, boolean friend, boolean owner) {
        return owner || !user.isPrivateAccount() || friend;
    }

    private boolean isFriend(UUID currentUserId, User user) {
        if (currentUserId == null || currentUserId.equals(user.getId())) {
            return false;
        }
        User currentUser = getUser(currentUserId);
        return findFriendship(currentUser, user)
            .map(friendship -> friendship.getStatus() == UserFriendship.FriendshipStatus.ACCEPTED)
            .orElse(false);
    }

    private void requirePendingAddressee(UserFriendship friendship, UUID currentUserId) {
        if (friendship.getStatus() != UserFriendship.FriendshipStatus.PENDING
            || !friendship.getAddressee().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("Only the request recipient can respond");
        }
    }

    private OrderedUsers orderUsers(User first, User second) {
        return first.getId().toString().compareTo(second.getId().toString()) < 0
            ? new OrderedUsers(first, second)
            : new OrderedUsers(second, first);
    }

    private record OrderedUsers(User userOne, User userTwo) {}

    private void notifySafely(Runnable notificationAction) {
        try {
            notificationAction.run();
        } catch (Exception e) {
            System.err.println("Failed to create social notification: " + e.getMessage());
        }
    }
}
