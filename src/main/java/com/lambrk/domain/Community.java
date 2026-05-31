package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "communities", indexes = {
    @Index(name = "idx_community_name", columnList = "name"),
    @Index(name = "idx_community_created_at", columnList = "created_at"),
    @Index(name = "idx_community_member_count", columnList = "member_count")
})
@EntityListeners(AuditingEntityListener.class)
public class Community {

    @Id
    private UUID id;

    @NotBlank(message = "Community name is required")
    @Size(min = 3, max = 21, message = "Community name must be between 3 and 21 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Community name can only contain letters, numbers, and underscores")
    @Column(unique = true, nullable = false, length = 21)
    private String name;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sidebar_text", columnDefinition = "TEXT")
    private String sidebarText;

    @Column(name = "header_image_url", length = 500)
    private String headerImageUrl;

    @Column(name = "icon_image_url", length = 500)
    private String iconImageUrl;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "is_restricted", nullable = false)
    private boolean isRestricted = false;

    @Column(name = "is_over_18", nullable = false)
    private boolean isOver18 = false;

    @Column(name = "member_count", nullable = false)
    private int memberCount = 0;

    @Column(name = "subscriber_count", nullable = false)
    private int subscriberCount = 0;

    @Column(name = "active_user_count", nullable = false)
    private int activeUserCount = 0;

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCommunityMembership> memberships = new HashSet<>();

    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCommunityModerator> moderators = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "community_categories",
        joinColumns = @JoinColumn(name = "community_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Community() {}

    public Community(UUID id, String name, String title, String description, String sidebarText,
                     String headerImageUrl, String iconImageUrl, boolean isPublic, boolean isRestricted,
                     boolean isOver18, int memberCount, int subscriberCount, int activeUserCount,
                     Set<Post> posts, Set<UserCommunityMembership> memberships, Set<UserCommunityModerator> moderators, Set<Category> categories,
                     User createdBy, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.description = description;
        this.sidebarText = sidebarText;
        this.headerImageUrl = headerImageUrl;
        this.iconImageUrl = iconImageUrl;
        this.isPublic = isPublic;
        this.isRestricted = isRestricted;
        this.isOver18 = isOver18;
        this.memberCount = memberCount;
        this.subscriberCount = subscriberCount;
        this.activeUserCount = activeUserCount;
        this.posts = posts != null ? posts : new HashSet<>();
        this.memberships = memberships != null ? memberships : new HashSet<>();
        this.moderators = moderators != null ? moderators : new HashSet<>();
        this.categories = categories != null ? categories : new HashSet<>();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Community(String name, String title, User createdBy) {
        this(com.lambrk.util.UuidV7Generator.generate(), name, title, null, null, null, null, true, false, false, 0, 0, 0,
             new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), createdBy, Instant.now(), Instant.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSidebarText() { return sidebarText; }
    public void setSidebarText(String sidebarText) { this.sidebarText = sidebarText; }
    public String getHeaderImageUrl() { return headerImageUrl; }
    public void setHeaderImageUrl(String headerImageUrl) { this.headerImageUrl = headerImageUrl; }
    public String getIconImageUrl() { return iconImageUrl; }
    public void setIconImageUrl(String iconImageUrl) { this.iconImageUrl = iconImageUrl; }
    public boolean isPublic() { return isPublic; }
    public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
    public boolean isRestricted() { return isRestricted; }
    public void setRestricted(boolean restricted) { this.isRestricted = restricted; }
    public boolean isOver18() { return isOver18; }
    public void setOver18(boolean over18) { this.isOver18 = over18; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public int getSubscriberCount() { return subscriberCount; }
    public void setSubscriberCount(int subscriberCount) { this.subscriberCount = subscriberCount; }
    public int getActiveUserCount() { return activeUserCount; }
    public void setActiveUserCount(int activeUserCount) { this.activeUserCount = activeUserCount; }
    public Set<Post> getPosts() { return posts; }
    public void setPosts(Set<Post> posts) { this.posts = posts; }
    public Set<UserCommunityMembership> getMemberships() { return memberships; }
    public void setMemberships(Set<UserCommunityMembership> memberships) { this.memberships = memberships; }
    public Set<UserCommunityModerator> getModerators() { return moderators; }
    public void setModerators(Set<UserCommunityModerator> moderators) { this.moderators = moderators; }

    // Convenience methods
    public Set<User> getActiveMembers() {
        return memberships.stream()
            .filter(m -> m.getStatus() == UserCommunityMembership.MembershipStatus.ACTIVE)
            .map(UserCommunityMembership::getUser)
            .collect(java.util.stream.Collectors.toSet());
    }

    public Set<User> getActiveModeratorUsers() {
        return moderators.stream()
            .filter(UserCommunityModerator::isActive)
            .map(UserCommunityModerator::getUser)
            .collect(java.util.stream.Collectors.toSet());
    }
    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
