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
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_author", columnList = "author_id"),
    @Index(name = "idx_comment_post", columnList = "post_id"),
    @Index(name = "idx_comment_parent", columnList = "parent_id"),
    @Index(name = "idx_comment_created_at", columnList = "created_at"),
    @Index(name = "idx_comment_score", columnList = "score")
})
@EntityListeners(AuditingEntityListener.class)
public record Comment(
    
    @Id
    @GeneratedValue(generator = "uuid7")
    UUID id,
    
    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Comment must be less than 10000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    String content,
    
    @Column(name = "flair_text", length = 64)
    String flairText,
    
    @Column(name = "is_edited", nullable = false)
    boolean isEdited,
    
    @Column(name = "is_deleted", nullable = false)
    boolean isDeleted,
    
    @Column(name = "is_removed", nullable = false)
    boolean isRemoved,
    
    @Column(name = "is_collapsed", nullable = false)
    boolean isCollapsed,
    
    @Column(name = "is_stickied", nullable = false)
    boolean isStickied,
    
    @Column(name = "is_over_18", nullable = false)
    boolean isOver18,
    
    @Column(name = "score", nullable = false)
    int score,
    
    @Column(name = "like_count", nullable = false)
    int likeCount,
    
    @Column(name = "dislike_count", nullable = false)
    int dislikeCount,
    
    @Column(name = "reply_count", nullable = false)
    int replyCount,
    
    @Column(name = "award_count", nullable = false)
    int awardCount,
    
    @Column(name = "depth_level", nullable = false)
    int depthLevel,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    User author,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    Post post,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Comment parent,
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Comment> replies,
    
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Vote> votes,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt,
    
    @Column(name = "edited_at")
    Instant editedAt,
    
    @Column(name = "deleted_at")
    Instant deletedAt,
    
    @Column(name = "removed_at")
    Instant removedAt
) {
    
    public Comment {
        if (replies == null) replies = new HashSet<>();
        if (votes == null) votes = new HashSet<>();
        isEdited = false;
        isDeleted = false;
        isRemoved = false;
        isCollapsed = false;
        isStickied = false;
        isOver18 = false;
        score = 1;
        likeCount = 1;
        dislikeCount = 0;
        replyCount = 0;
        awardCount = 0;
        depthLevel = 0;
    }
    
    public Comment(String content, User author, Post post, Comment parent) {
        this(null, content, null, false, false, false, false, false, false, 1, 1, 0, 0, 0,
             parent != null ? parent.depthLevel() + 1 : 0, author, post, parent, new HashSet<>(),
             new HashSet<>(), Instant.now(), Instant.now(), null, null, null);
    }
    
}
