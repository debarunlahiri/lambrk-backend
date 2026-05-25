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
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_author", columnList = "author_id"),
    @Index(name = "idx_comment_post", columnList = "post_id"),
    @Index(name = "idx_comment_parent", columnList = "parent_id"),
    @Index(name = "idx_comment_created_at", columnList = "created_at"),
    @Index(name = "idx_comment_score", columnList = "score")
})
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    private UUID id;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Comment must be less than 10000 characters")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "flair_text", length = 64)
    private String flairText;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "is_removed", nullable = false)
    private boolean isRemoved = false;

    @Column(name = "is_collapsed", nullable = false)
    private boolean isCollapsed = false;

    @Column(name = "is_stickied", nullable = false)
    private boolean isStickied = false;

    @Column(name = "is_over_18", nullable = false)
    private boolean isOver18 = false;

    @Column(name = "score", nullable = false)
    private int score = 1;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 1;

    @Column(name = "dislike_count", nullable = false)
    private int dislikeCount = 0;

    @Column(name = "reply_count", nullable = false)
    private int replyCount = 0;

    @Column(name = "award_count", nullable = false)
    private int awardCount = 0;

    @Column(name = "depth_level", nullable = false)
    private int depthLevel = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> replies = new HashSet<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Vote> votes = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    protected Comment() {}

    public Comment(UUID id, String content, String flairText, boolean isEdited, boolean isDeleted, boolean isRemoved,
                   boolean isCollapsed, boolean isStickied, boolean isOver18, int score, int likeCount, int dislikeCount,
                   int replyCount, int awardCount, int depthLevel, User author, Post post, Comment parent,
                   Set<Comment> replies, Set<Vote> votes, Instant createdAt, Instant updatedAt, Instant editedAt,
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
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.replyCount = replyCount;
        this.awardCount = awardCount;
        this.depthLevel = depthLevel;
        this.author = author;
        this.post = post;
        this.parent = parent;
        this.replies = replies != null ? replies : new HashSet<>();
        this.votes = votes != null ? votes : new HashSet<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.editedAt = editedAt;
        this.deletedAt = deletedAt;
        this.removedAt = removedAt;
    }

    public Comment(String content, User author, Post post, Comment parent) {
        this(com.lambrk.util.UuidV7Generator.generate(), content, null, false, false, false, false, false, false, 1, 1, 0, 0, 0,
             parent != null ? parent.getDepthLevel() + 1 : 0, author, post, parent, new HashSet<>(),
             new HashSet<>(), Instant.now(), Instant.now(), null, null, null);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getFlairText() { return flairText; }
    public void setFlairText(String flairText) { this.flairText = flairText; }
    public boolean isEdited() { return isEdited; }
    public void setEdited(boolean edited) { this.isEdited = edited; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
    public boolean isRemoved() { return isRemoved; }
    public void setRemoved(boolean removed) { this.isRemoved = removed; }
    public boolean isCollapsed() { return isCollapsed; }
    public void setCollapsed(boolean collapsed) { this.isCollapsed = collapsed; }
    public boolean isStickied() { return isStickied; }
    public void setStickied(boolean stickied) { this.isStickied = stickied; }
    public boolean isOver18() { return isOver18; }
    public void setOver18(boolean over18) { this.isOver18 = over18; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }
    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }
    public int getAwardCount() { return awardCount; }
    public void setAwardCount(int awardCount) { this.awardCount = awardCount; }
    public int getDepthLevel() { return depthLevel; }
    public void setDepthLevel(int depthLevel) { this.depthLevel = depthLevel; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public Comment getParent() { return parent; }
    public void setParent(Comment parent) { this.parent = parent; }
    public Set<Comment> getReplies() { return replies; }
    public void setReplies(Set<Comment> replies) { this.replies = replies; }
    public Set<Vote> getVotes() { return votes; }
    public void setVotes(Set<Vote> votes) { this.votes = votes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getEditedAt() { return editedAt; }
    public void setEditedAt(Instant editedAt) { this.editedAt = editedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public Instant getRemovedAt() { return removedAt; }
    public void setRemovedAt(Instant removedAt) { this.removedAt = removedAt; }
}
