package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "header_image_url", length = 500)
    private String headerImageUrl;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "karma", nullable = false)
    private int karma = 0;

    @Column(name = "private_account", nullable = false)
    private boolean privateAccount = false;

    @Column(name = "hide_follower_count", nullable = false)
    private boolean hideFollowerCount = false;

    @Column(name = "hide_following_count", nullable = false)
    private boolean hideFollowingCount = false;

    @Column(name = "hide_follower_list", nullable = false)
    private boolean hideFollowerList = false;

    @Column(name = "hide_following_list", nullable = false)
    private boolean hideFollowingList = false;

    @Column(name = "hide_add_friend_button", nullable = false)
    private boolean hideAddFriendButton = false;

    @Column(name = "hide_follow_button", nullable = false)
    private boolean hideFollowButton = false;

    @Column(name = "hide_from_mutual_list", nullable = false)
    private boolean hideFromMutualList = false;

    @Column(name = "message_button_enabled", nullable = false)
    private boolean messageButtonEnabled = true;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Vote> votes = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCommunityMembership> memberships = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCommunityModerator> moderatorRoles = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {}

    public User(UUID id, String username, String email, String password, String displayName, String bio,
                String avatarUrl, String headerImageUrl, String location, String website,
                boolean isActive, boolean isVerified, int karma, Set<Post> posts,
                Set<Comment> comments, Set<Vote> votes, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.headerImageUrl = headerImageUrl;
        this.location = location;
        this.website = website;
        this.isActive = isActive;
        this.isVerified = isVerified;
        this.karma = karma;
        this.posts = posts != null ? posts : new HashSet<>();
        this.comments = comments != null ? comments : new HashSet<>();
        this.votes = votes != null ? votes : new HashSet<>();
        this.memberships = new HashSet<>();
        this.moderatorRoles = new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public User(String username, String email, String password) {
        this(com.lambrk.util.UuidV7Generator.generate(), username, email, password, null, null, null, null, null, null, true, false, 0,
             new HashSet<>(), new HashSet<>(), new HashSet<>(), Instant.now(), Instant.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getHeaderImageUrl() { return headerImageUrl; }
    public void setHeaderImageUrl(String headerImageUrl) { this.headerImageUrl = headerImageUrl; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { this.isVerified = verified; }
    public int getKarma() { return karma; }
    public void setKarma(int karma) { this.karma = karma; }
    public boolean isPrivateAccount() { return privateAccount; }
    public void setPrivateAccount(boolean privateAccount) { this.privateAccount = privateAccount; }
    public boolean isHideFollowerCount() { return hideFollowerCount; }
    public void setHideFollowerCount(boolean hideFollowerCount) { this.hideFollowerCount = hideFollowerCount; }
    public boolean isHideFollowingCount() { return hideFollowingCount; }
    public void setHideFollowingCount(boolean hideFollowingCount) { this.hideFollowingCount = hideFollowingCount; }
    public boolean isHideFollowerList() { return hideFollowerList; }
    public void setHideFollowerList(boolean hideFollowerList) { this.hideFollowerList = hideFollowerList; }
    public boolean isHideFollowingList() { return hideFollowingList; }
    public void setHideFollowingList(boolean hideFollowingList) { this.hideFollowingList = hideFollowingList; }
    public boolean isHideAddFriendButton() { return hideAddFriendButton; }
    public void setHideAddFriendButton(boolean hideAddFriendButton) { this.hideAddFriendButton = hideAddFriendButton; }
    public boolean isHideFollowButton() { return hideFollowButton; }
    public void setHideFollowButton(boolean hideFollowButton) { this.hideFollowButton = hideFollowButton; }
    public boolean isHideFromMutualList() { return hideFromMutualList; }
    public void setHideFromMutualList(boolean hideFromMutualList) { this.hideFromMutualList = hideFromMutualList; }
    public boolean isMessageButtonEnabled() { return messageButtonEnabled; }
    public void setMessageButtonEnabled(boolean messageButtonEnabled) { this.messageButtonEnabled = messageButtonEnabled; }
    public Set<Post> getPosts() { return posts; }
    public void setPosts(Set<Post> posts) { this.posts = posts; }
    public Set<Comment> getComments() { return comments; }
    public void setComments(Set<Comment> comments) { this.comments = comments; }
    public Set<Vote> getVotes() { return votes; }
    public void setVotes(Set<Vote> votes) { this.votes = votes; }
    public Set<UserCommunityMembership> getMemberships() { return memberships; }
    public void setMemberships(Set<UserCommunityMembership> memberships) { this.memberships = memberships; }
    public Set<UserCommunityModerator> getModeratorRoles() { return moderatorRoles; }
    public void setModeratorRoles(Set<UserCommunityModerator> moderatorRoles) { this.moderatorRoles = moderatorRoles; }

    // Convenience methods for active relationships
    public Set<Community> getActiveSubscribedCommunities() {
        return memberships.stream()
            .filter(m -> m.getStatus() == UserCommunityMembership.MembershipStatus.ACTIVE)
            .map(UserCommunityMembership::getCommunity)
            .collect(java.util.stream.Collectors.toSet());
    }

    public Set<Community> getActiveModeratedCommunities() {
        return moderatorRoles.stream()
            .filter(UserCommunityModerator::isActive)
            .map(UserCommunityModerator::getCommunity)
            .collect(java.util.stream.Collectors.toSet());
    }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
