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
@org.hibernate.annotations.GenericGenerator(name = "uuid7", strategy = "com.lambrk.util.UuidV7Generator")
@Table(name = "communities", indexes = {
    @Index(name = "idx_community_name", columnList = "name"),
    @Index(name = "idx_community_created_at", columnList = "created_at"),
    @Index(name = "idx_community_member_count", columnList = "member_count")
})
@EntityListeners(AuditingEntityListener.class)
public record Community(
    
    @Id
    @GeneratedValue(generator = "uuid7")
    UUID id,
    
    @NotBlank(message = "Community name is required")
    @Size(min = 3, max = 21, message = "Community name must be between 3 and 21 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Community name can only contain letters, numbers, and underscores")
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
    
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Post> posts,
    
    @ManyToMany(mappedBy = "subscribedCommunities", fetch = FetchType.LAZY)
    Set<User> members,

    @ManyToMany(mappedBy = "moderatedCommunities", fetch = FetchType.LAZY)
    Set<User> moderators,

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "community_categories",
        joinColumns = @JoinColumn(name = "community_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    Set<Category> categories,

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
    
    public Community {
        if (posts == null) posts = new HashSet<>();
        if (members == null) members = new HashSet<>();
        if (moderators == null) moderators = new HashSet<>();
        if (categories == null) categories = new HashSet<>();
        isPublic = true;
        isRestricted = false;
        isOver18 = false;
        memberCount = 0;
        subscriberCount = 0;
        activeUserCount = 0;
    }

    public Community(String name, String title, User createdBy) {
        this(null, name, title, null, null, null, null, true, false, false, 0, 0, 0,
             new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), createdBy, Instant.now(), Instant.now());
    }
    
}
