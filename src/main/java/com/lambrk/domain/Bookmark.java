package com.lambrk.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookmarks", indexes = {
    @Index(name = "idx_bookmark_user", columnList = "user_id"),
    @Index(name = "idx_bookmark_post", columnList = "post_id"),
    @Index(name = "idx_bookmark_user_post", columnList = "user_id, post_id", unique = true),
    @Index(name = "idx_bookmark_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Bookmark {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Bookmark() {}

    public Bookmark(UUID id, User user, Post post) {
        this.id = id;
        this.user = user;
        this.post = post;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
