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
@Table(name = "posts", indexes = {
    @Index(name = "idx_post_author", columnList = "author_id"),
    @Index(name = "idx_post_community", columnList = "community_id"),
    @Index(name = "idx_post_created_at", columnList = "created_at"),
    @Index(name = "idx_post_score", columnList = "score"),
    @Index(name = "idx_post_title", columnList = "title")
})
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    private UUID id;

    @Size(max = 300, message = "Title must be less than 300 characters")
    @Column(length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "url", length = 2000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "flair_text", length = 64)
    private String flairText;

    @Column(name = "flair_css_class", length = 64)
    private String flairCssClass;

    @Column(name = "is_spoiler", nullable = false)
    private boolean isSpoiler = false;

    @Column(name = "is_stickied", nullable = false)
    private boolean isStickied = false;

    @Column(name = "is_locked", nullable = false)
    private boolean isLocked = false;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Column(name = "is_removed", nullable = false)
    private boolean isRemoved = false;

    @Column(name = "is_over_18", nullable = false)
    private boolean isOver18 = false;

    @Column(name = "score", nullable = false)
    private int score = 1;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 1;

    @Column(name = "dislike_count", nullable = false)
    private int dislikeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "award_count", nullable = false)
    private int awardCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Vote> votes = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    protected Post() {}

    public Post(UUID id, String title, String content, String url, PostType postType, String thumbnailUrl,
                String flairText, String flairCssClass, boolean isSpoiler, boolean isStickied, boolean isLocked,
                boolean isArchived, boolean isRemoved, boolean isOver18, int score, int likeCount, int dislikeCount,
                int commentCount, int viewCount, int awardCount, User author, Community community,
                Set<Comment> comments, Set<Vote> votes, Instant createdAt, Instant updatedAt, Instant archivedAt) {
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
        this.isRemoved = isRemoved;
        this.isOver18 = isOver18;
        this.score = score;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.awardCount = awardCount;
        this.author = author;
        this.community = community;
        this.comments = comments != null ? comments : new HashSet<>();
        this.votes = votes != null ? votes : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
    }

    public Post(String title, String content, String url, PostType postType, User author, Community community) {
        this(com.lambrk.util.UuidV7Generator.generate(), title, content, url, postType, null, null, null, false, false, false, false, false,
             false, 1, 1, 0, 0, 0, 0, author, community, new HashSet<>(), new HashSet<>(),
             Instant.now(), Instant.now(), null);
    }

    public enum PostType {
        TEXT, LINK, IMAGE, VIDEO, POLL
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getFlairText() { return flairText; }
    public void setFlairText(String flairText) { this.flairText = flairText; }
    public String getFlairCssClass() { return flairCssClass; }
    public void setFlairCssClass(String flairCssClass) { this.flairCssClass = flairCssClass; }
    public boolean isSpoiler() { return isSpoiler; }
    public void setSpoiler(boolean spoiler) { this.isSpoiler = spoiler; }
    public boolean isStickied() { return isStickied; }
    public void setStickied(boolean stickied) { this.isStickied = stickied; }
    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { this.isLocked = locked; }
    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { this.isArchived = archived; }
    public boolean isRemoved() { return isRemoved; }
    public void setRemoved(boolean removed) { this.isRemoved = removed; }
    public boolean isOver18() { return isOver18; }
    public void setOver18(boolean over18) { this.isOver18 = over18; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getAwardCount() { return awardCount; }
    public void setAwardCount(int awardCount) { this.awardCount = awardCount; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Community getCommunity() { return community; }
    public void setCommunity(Community community) { this.community = community; }
    public Set<Comment> getComments() { return comments; }
    public void setComments(Set<Comment> comments) { this.comments = comments; }
    public Set<Vote> getVotes() { return votes; }
    public void setVotes(Set<Vote> votes) { this.votes = votes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getArchivedAt() { return archivedAt; }
    public void setArchivedAt(Instant archivedAt) { this.archivedAt = archivedAt; }
}
