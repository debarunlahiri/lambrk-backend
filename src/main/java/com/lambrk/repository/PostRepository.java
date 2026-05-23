package com.lambrk.repository;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Community;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID>, JpaSpecificationExecutor<Post> {

    Page<Post> findByCommunity(Community community, Pageable pageable);

    Page<Post> findByAuthor(User author, Pageable pageable);

    Page<Post> findByCommunityAndIsArchivedFalse(Community community, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isOver18 = false AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findHotPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findHotPostsSince(@Param("since") Instant since, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findNewPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findNewPostsSince(@Param("since") Instant since, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.commentCount > 0 AND p.isArchived = false ORDER BY (p.likeCount + p.commentCount) DESC")
    Page<Post> findTopPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isStickied = true AND p.isArchived = false ORDER BY p.createdAt DESC")
    List<Post> findStickiedPosts();

    @Query("SELECT p FROM Post p WHERE p.community.id = :communityId AND p.isStickied = true AND p.isArchived = false")
    List<Post> findStickiedPostsByCommunity(@Param("communityId") UUID communityId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isArchived = false")
    long countActivePosts();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.community.id = :communityId AND p.isArchived = false")
    long countActivePostsByCommunity(@Param("communityId") UUID communityId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId AND p.isArchived = false")
    long countActivePostsByAuthor(@Param("authorId") UUID authorId);

    @Query("UPDATE Post p SET p.score = p.score + :delta, p.likeCount = p.likeCount + :likeDelta, p.dislikeCount = p.dislikeCount + :dislikeDelta WHERE p.id = :postId")
    @Modifying
    void updatePostScore(@Param("postId") UUID postId, @Param("delta") int delta,
                        @Param("likeDelta") int likeDelta, @Param("dislikeDelta") int dislikeDelta);

    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    @Modifying
    void updatePostCommentCount(@Param("postId") UUID postId, @Param("delta") int delta);

    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    @Modifying
    void incrementPostViewCount(@Param("postId") UUID postId);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:query% OR p.content LIKE %:query% AND p.isArchived = false")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.community.id = :communityId AND (p.title LIKE %:query% OR p.content LIKE %:query%) AND p.isArchived = false")
    Page<Post> searchPostsByCommunity(@Param("communityId") UUID communityId, @Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.community.name IN :communityNames AND (p.title LIKE %:query% OR p.content LIKE %:query%) AND p.isArchived = false")
    Page<Post> searchPostsByCommunities(@Param("communityNames") List<String> communityNames, @Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postType = :postType AND p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findPostsByType(@Param("postType") com.lambrk.domain.Post.PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isOver18 = true AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findNSFWPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.isArchived = true")
    List<Post> findPostsToArchive(@Param("since") Instant since);

    @Query("UPDATE Post p SET p.isArchived = true, p.archivedAt = :archivedAt WHERE p.id IN :postIds")
    @Modifying
    void archivePosts(@Param("postIds") List<UUID> postIds, @Param("archivedAt") Instant archivedAt);

    @Query("SELECT p FROM Post p WHERE p.score >= :minScore AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findPostsByMinScore(@Param("minScore") int minScore, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.author.id = :authorId AND p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findUserPostsSince(@Param("authorId") UUID authorId, @Param("since") Instant since, Pageable pageable);
}
