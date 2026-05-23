package com.lambrk.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_slug", columnList = "slug"),
    @Index(name = "idx_category_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public record Category(

    @Id
    @GeneratedValue(generator = "uuid7")
    UUID id,

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Column(unique = true, nullable = false, length = 50)
    String name,

    @Column(columnDefinition = "TEXT")
    String description,

    @Column(name = "icon_url", length = 500)
    String iconUrl,

    @Column(name = "image_url", length = 500)
    String imageUrl,

    @Column(length = 7)
    String color,

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9\\-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    @Column(unique = true, nullable = false, length = 50)
    String slug,

    @Column(name = "sort_order", nullable = false)
    int sortOrder,

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    Set<Community> communities,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt
) {

    public Category {
        if (communities == null) communities = new HashSet<>();
        sortOrder = 0;
    }

    public Category(String name, String slug) {
        this(null, name, null, null, null, null, slug, 0, new HashSet<>(), Instant.now(), Instant.now());
    }
}
