package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subreddits", indexes = {
    @Index(name = "idx_subreddit_name", columnList = "name"),
    @Index(name = "idx_subreddit_created_at", columnList = "created_at"),
    @Index(name = "idx_subreddit_member_count", columnList = "member_count")
})
@EntityListeners(AuditingEntityListener.class)
public record Subreddit(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
    @NotBlank(message = "Subreddit name is required")
    @Size(min = 3, max = 21, message = "Subreddit name must be between 3 and 21 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Subreddit name can only contain letters, numbers, and underscores")
    @Column(unique = true, nullable = false, length = 21)
    String name,
    
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    @Column(nullable = false, length = 100)
    String title,
    
    @Column(columnDefinition = "TEXT")
    String description,
    
    @Column(name = "sidebar_text", columnDefinition = "TEXT")
    String sidebarText,
    
    @Column(name = "header_image_url", length = 500)
    String headerImageUrl,
    
    @Column(name = "icon_image_url", length = 500)
    String iconImageUrl,
    
    @Column(name = "is_public", nullable = false)
    boolean isPublic,
    
    @Column(name = "is_restricted", nullable = false)
    boolean isRestricted,
    
    @Column(name = "is_over_18", nullable = false)
    boolean isOver18,
    
    @Column(name = "member_count", nullable = false)
    int memberCount,
    
    @Column(name = "subscriber_count", nullable = false)
    int subscriberCount,
    
    @Column(name = "active_user_count", nullable = false)
    int activeUserCount,
    
    @OneToMany(mappedBy = "subreddit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Post> posts,
    
    @ManyToMany(mappedBy = "subscribedSubreddits", fetch = FetchType.LAZY)
    Set<User> members,
    
    @ManyToMany(mappedBy = "moderatedSubreddits", fetch = FetchType.LAZY)
    Set<User> moderators,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    User createdBy,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt
) {
    
    public Subreddit {
        if (posts == null) posts = new HashSet<>();
        if (members == null) members = new HashSet<>();
        if (moderators == null) moderators = new HashSet<>();
        isPublic = true;
        isRestricted = false;
        isOver18 = false;
        memberCount = 0;
        subscriberCount = 0;
        activeUserCount = 0;
    }
    
    public Subreddit(String name, String title, User createdBy) {
        this(null, name, title, null, null, null, null, true, false, false, 0, 0, 0,
             new HashSet<>(), new HashSet<>(), new HashSet<>(), createdBy, Instant.now(), Instant.now());
    }
    
    public Subreddit(Long id, String name, String title, String description, String sidebarText,
                     String headerImageUrl, String iconImageUrl, boolean isPublic, boolean isRestricted,
                     boolean isOver18, int memberCount, int subscriberCount, int activeUserCount,
                     Set<Post> posts, Set<User> members, Set<User> moderators, User createdBy,
                     Instant createdAt, Instant updatedAt) {
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
        this.posts = posts;
        this.members = members;
        this.moderators = moderators;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
