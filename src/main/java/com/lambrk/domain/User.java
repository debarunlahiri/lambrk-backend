package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public record User(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id,
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    String username,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false, length = 100)
    String email,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    String password,
    
    @Column(name = "display_name", length = 100)
    String displayName,
    
    @Column(columnDefinition = "TEXT")
    String bio,
    
    @Column(name = "avatar_url", length = 500)
    String avatarUrl,
    
    @Column(name = "is_active", nullable = false)
    boolean isActive,
    
    @Column(name = "is_verified", nullable = false)
    boolean isVerified,
    
    @Column(name = "karma", nullable = false)
    int karma,
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Post> posts,
    
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Comment> comments,
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Vote> votes,
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_subreddit_memberships",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "subreddit_id")
    )
    Set<Subreddit> subscribedSubreddits,
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_subreddit_moderators",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "subreddit_id")
    )
    Set<Subreddit> moderatedSubreddits,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt
) {
    
    public User {
        if (posts == null) posts = new HashSet<>();
        if (comments == null) comments = new HashSet<>();
        if (votes == null) votes = new HashSet<>();
        if (subscribedSubreddits == null) subscribedSubreddits = new HashSet<>();
        if (moderatedSubreddits == null) moderatedSubreddits = new HashSet<>();
        isActive = true;
        isVerified = false;
        karma = 0;
    }
    
    public User(String username, String email, String password) {
        this(null, username, email, password, null, null, null, true, false, 0,
             new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(),
             Instant.now(), Instant.now());
    }
    
    public User(Long id, String username, String email, String password, String displayName, String bio,
                String avatarUrl, boolean isActive, boolean isVerified, int karma,
                Set<Post> posts, Set<Comment> comments, Set<Vote> votes,
                Set<Subreddit> subscribedSubreddits, Set<Subreddit> moderatedSubreddits,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
        this.isVerified = isVerified;
        this.karma = karma;
        this.posts = posts;
        this.comments = comments;
        this.votes = votes;
        this.subscribedSubreddits = subscribedSubreddits;
        this.moderatedSubreddits = moderatedSubreddits;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
