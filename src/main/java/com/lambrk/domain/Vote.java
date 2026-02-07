package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

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
public record Vote(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false)
    VoteType voteType,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    Post post,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    Comment comment,
    
    @Column(name = "ip_address", length = 45)
    String ipAddress,
    
    @Column(name = "user_agent", length = 500)
    String userAgent,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt
) {
    
    public Vote(VoteType voteType, User user, Post post, Comment comment) {
        this(null, voteType, user, post, comment, null, null, Instant.now(), Instant.now());
    }
    
    public Vote(Long id, VoteType voteType, User user, Post post, Comment comment,
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
    
    public enum VoteType {
        UPVOTE, DOWNVOTE
    }
}
