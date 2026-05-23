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
import java.util.UUID;

@Entity
@org.hibernate.annotations.GenericGenerator(name = "uuid7", strategy = "com.lambrk.util.UuidV7Generator")
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_author", columnList = "author_id"),
    @Index(name = "idx_post_community", columnList = "community_id"),
    @Index(name = "idx_post_created_at", columnList = "created_at"),
    @Index(name = "idx_post_score", columnList = "score"),
    @Index(name = "idx_post_title", columnList = "title")
})
@EntityListeners(AuditingEntityListener.class)
public record Post(
    
    @Id
    @GeneratedValue(generator = "uuid7")
    UUID id,
    
    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title must be less than 300 characters")
    @Column(nullable = false, length = 300)
    String title,
    
    @Column(columnDefinition = "TEXT")
    String content,
    
    @Column(name = "url", length = 2000)
    String url,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    PostType postType,
    
    @Column(name = "thumbnail_url", length = 500)
    String thumbnailUrl,
    
    @Column(name = "flair_text", length = 64)
    String flairText,
    
    @Column(name = "flair_css_class", length = 64)
    String flairCssClass,
    
    @Column(name = "is_spoiler", nullable = false)
    boolean isSpoiler,
    
    @Column(name = "is_stickied", nullable = false)
    boolean isStickied,
    
    @Column(name = "is_locked", nullable = false)
    boolean isLocked,
    
    @Column(name = "is_archived", nullable = false)
    boolean isArchived,

    @Column(name = "is_removed", nullable = false)
    boolean isRemoved,
    
    @Column(name = "is_over_18", nullable = false)
    boolean isOver18,
    
    @Column(name = "score", nullable = false)
    int score,
    
    @Column(name = "like_count", nullable = false)
    int likeCount,
    
    @Column(name = "dislike_count", nullable = false)
    int dislikeCount,
    
    @Column(name = "comment_count", nullable = false)
    int commentCount,
    
    @Column(name = "view_count", nullable = false)
    int viewCount,
    
    @Column(name = "award_count", nullable = false)
    int awardCount,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    Community community,
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Comment> comments,
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Vote> votes,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt,
    
    @Column(name = "archived_at")
    Instant archivedAt
) {
    
    public Post {
        if (comments == null) comments = new HashSet<>();
        if (votes == null) votes = new HashSet<>();
        isSpoiler = false;
        isStickied = false;
        isLocked = false;
        isArchived = false;
        isRemoved = false;
        isOver18 = false;
        score = 1;
        likeCount = 1;
        dislikeCount = 0;
        commentCount = 0;
        viewCount = 0;
        awardCount = 0;
    }
    
    public Post(String title, String content, String url, PostType postType, User author, Community community) {
        this(null, title, content, url, postType, null, null, null, false, false, false, false, false,
             false, 1, 1, 0, 0, 0, 0, author, community, new HashSet<>(), new HashSet<>(),
             Instant.now(), Instant.now(), null);
    }
    
    public enum PostType {
        TEXT, LINK, IMAGE, VIDEO, POLL
    }
}
