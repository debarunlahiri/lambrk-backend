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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        UserFollow follow = userFollowRepository.findByFollowerAndFollowing(follower, following)
            .orElseGet(() -> new UserFollow(UuidV7Generator.generate(), follower, following, source));
        follow.activate(source);
        userFollowRepository.save(follow);
        notificationService.createFollowNotification(followerId, followingId);
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
            notificationService.createFriendRequestNotification(requesterId, addresseeId);
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
        notificationService.createFriendRequestNotification(requesterId, addresseeId);
        return saved;
    }

    public UserFriendship acceptFriendRequest(UUID currentUserId, UUID requesterId) {
        User currentUser = getUser(currentUserId);
        User requester = getUser(requesterId);
        UserFriendship friendship = getFriendship(currentUser, requester);
        requirePendingAddressee(friendship, currentUserId);
        friendship.accept(currentUser);
        UserFriendship saved = userFriendshipRepository.save(friendship);
        notificationService.createFriendRequestAcceptedNotification(currentUserId, requesterId);
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
        return userFollowRepository.findFollowers(user, pageable)
            .map(follower -> toSocialUserResponse(follower, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getFollowing(UUID userId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        return userFollowRepository.findFollowing(user, pageable)
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
        return userFollowRepository.findMutualFollowers(user, withUser, pageable)
            .map(mutualUser -> toSocialUserResponse(mutualUser, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getMutualFollowing(UUID userId, UUID withUserId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        User withUser = getUser(withUserId);
        return userFollowRepository.findMutualFollowing(user, withUser, pageable)
            .map(mutualUser -> toSocialUserResponse(mutualUser, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<SocialUserResponse> getMutualFriends(UUID userId, UUID withUserId, UUID currentUserId, Pageable pageable) {
        User user = getUser(userId);
        User withUser = getUser(withUserId);
        return userFriendshipRepository.findMutualFriends(user, withUser, pageable)
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
    public SocialUserResponse toSocialUserResponse(User user, UUID currentUserId) {
        SocialStatsResponse stats = getStats(user.getId());
        boolean followedByCurrentUser = false;
        boolean followingCurrentUser = false;
        boolean friend = false;
        String friendshipStatus = null;

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
        );
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
}
