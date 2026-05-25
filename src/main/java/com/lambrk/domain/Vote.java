package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "votes", indexes = {
    @Index(name = "idx_vote_user", columnList = "user_id"),
    @Index(name = "idx_vote_post", columnList = "post_id"),
    @Index(name = "idx_vote_comment", columnList = "comment_id"),
    @Index(name = "idx_vote_created_at", columnList = "created_at"),
    @Index(name = "idx_vote_user_post", columnList = "user_id, post_id"),
    @Index(name = "idx_vote_user_comment", columnList = "user_id, comment_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Vote {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    private VoteType voteType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Vote() {}

    public Vote(UUID id, VoteType voteType, User user, Post post, Comment comment,
                String ipAddress, String userAgent, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.voteType = voteType;
        this.user = user;
        this.post = post;
        this.comment = comment;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Vote(VoteType voteType, User user, Post post, Comment comment) {
        this(com.lambrk.util.UuidV7Generator.generate(), voteType, user, post, comment, null, null, Instant.now(), Instant.now());
    }

    public enum VoteType {
        LIKE, DISLIKE
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public VoteType getVoteType() { return voteType; }
    public void setVoteType(VoteType voteType) { this.voteType = voteType; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }
    public Comment getComment() { return comment; }
    public void setComment(Comment comment) { this.comment = comment; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
