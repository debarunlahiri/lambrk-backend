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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
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
    
    @Column(name = "upvote_count", nullable = false)
    int upvoteCount,
    
    @Column(name = "downvote_count", nullable = false)
    int downvoteCount,
    
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
        upvoteCount = 1;
        downvoteCount = 0;
        replyCount = 0;
        awardCount = 0;
        depthLevel = 0;
    }
    
    public Comment(String content, User author, Post post, Comment parent) {
        this(null, content, null, false, false, false, false, false, false, 1, 1, 0, 0, 0,
             parent != null ? parent.depthLevel() + 1 : 0, author, post, parent, new HashSet<>(),
             new HashSet<>(), Instant.now(), Instant.now(), null, null, null);
    }
    
    public Comment(Long id, String content, String flairText, boolean isEdited, boolean isDeleted,
                   boolean isRemoved, boolean isCollapsed, boolean isStickied, boolean isOver18,
                   int score, int upvoteCount, int downvoteCount, int replyCount, int awardCount,
                   int depthLevel, User author, Post post, Comment parent, Set<Comment> replies,
                   Set<Vote> votes, Instant createdAt, Instant updatedAt, Instant editedAt,
                   Instant deletedAt, Instant removedAt) {
        this.id = id;
        this.content = content;
        this.flairText = flairText;
        this.isEdited = isEdited;
        this.isDeleted = isDeleted;
        this.isRemoved = isRemoved;
        this.isCollapsed = isCollapsed;
        this.isStickied = isStickied;
        this.isOver18 = isOver18;
        this.score = score;
        this.upvoteCount = upvoteCount;
        this.downvoteCount = downvoteCount;
        this.replyCount = replyCount;
        this.awardCount = awardCount;
        this.depthLevel = depthLevel;
        this.author = author;
        this.post = post;
        this.parent = parent;
        this.replies = replies;
        this.votes = votes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.removedAt = removedAt;
    }
}
