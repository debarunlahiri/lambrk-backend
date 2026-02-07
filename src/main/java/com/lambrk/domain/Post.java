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
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_author", columnList = "author_id"),
    @Index(name = "idx_post_subreddit", columnList = "subreddit_id"),
    @Index(name = "idx_post_created_at", columnList = "created_at"),
    @Index(name = "idx_post_score", columnList = "score"),
    @Index(name = "idx_post_title", columnList = "title")
})
@EntityListeners(AuditingEntityListener.class)
public record Post(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
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
    
    @Column(name = "is_over_18", nullable = false)
    boolean isOver18,
    
    @Column(name = "score", nullable = false)
    int score,
    
    @Column(name = "upvote_count", nullable = false)
    int upvoteCount,
    
    @Column(name = "downvote_count", nullable = false)
    int downvoteCount,
    
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
    @JoinColumn(name = "subreddit_id", nullable = false)
    Subreddit subreddit,
    
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
        isOver18 = false;
        score = 1;
        upvoteCount = 1;
        downvoteCount = 0;
        commentCount = 0;
        viewCount = 0;
        awardCount = 0;
    }
    
    public Post(String title, String content, String url, PostType postType, User author, Subreddit subreddit) {
        this(null, title, content, url, postType, null, null, null, false, false, false, false, false,
             1, 1, 0, 0, 0, 0, author, subreddit, new HashSet<>(), new HashSet<>(),
             Instant.now(), Instant.now(), null);
    }
    
    public Post(Long id, String title, String content, String url, PostType postType,
                String thumbnailUrl, String flairText, String flairCssClass, boolean isSpoiler,
                boolean isStickied, boolean isLocked, boolean isArchived, boolean isOver18,
                int score, int upvoteCount, int downvoteCount, int commentCount, int viewCount,
                int awardCount, User author, Subreddit subreddit, Set<Comment> comments, Set<Vote> votes,
                Instant createdAt, Instant updatedAt, Instant archivedAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.url = url;
        this.postType = postType;
        this.thumbnailUrl = thumbnailUrl;
        this.flairText = flairText;
        this.flairCssClass = flairCssClass;
        this.isSpoiler = isSpoiler;
        this.isStickied = isStickied;
        this.isLocked = isLocked;
        this.isArchived = isArchived;
        this.isOver18 = isOver18;
        this.score = score;
        this.upvoteCount = upvoteCount;
        this.downvoteCount = downvoteCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.awardCount = awardCount;
        this.author = author;
        this.subreddit = subreddit;
        this.comments = comments;
        this.votes = votes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
    }
    
    public enum PostType {
        TEXT, LINK, IMAGE, VIDEO, POLL
    }
}
