package com.lambrk.repository;

import com.lambrk.domain.Bookmark;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

    Optional<Bookmark> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    Page<Bookmark> findByUser(User user, Pageable pageable);

    long countByUser(User user);

    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId")
    java.util.Set<UUID> findBookmarkedPostIdsByUserId(@Param("userId") UUID userId);
}
