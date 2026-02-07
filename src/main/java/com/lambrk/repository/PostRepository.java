package com.lambrk.repository;

import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Subreddit;
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

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findBySubreddit(Subreddit subreddit, Pageable pageable);

    Page<Post> findByAuthor(User author, Pageable pageable);

    Page<Post> findBySubredditAndIsArchivedFalse(Subreddit subreddit, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isOver18 = false AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findHotPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findHotPostsSince(@Param("since") Instant since, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findNewPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findNewPostsSince(@Param("since") Instant since, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.commentCount > 0 AND p.isArchived = false ORDER BY (p.upvoteCount + p.commentCount) DESC")
    Page<Post> findTopPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isStickied = true AND p.isArchived = false ORDER BY p.createdAt DESC")
    List<Post> findStickiedPosts();

    @Query("SELECT p FROM Post p WHERE p.subreddit.id = :subredditId AND p.isStickied = true AND p.isArchived = false")
    List<Post> findStickiedPostsBySubreddit(@Param("subredditId") Long subredditId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isArchived = false")
    long countActivePosts();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.subreddit.id = :subredditId AND p.isArchived = false")
    long countActivePostsBySubreddit(@Param("subredditId") Long subredditId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :authorId AND p.isArchived = false")
    long countActivePostsByAuthor(@Param("authorId") Long authorId);

    @Query("UPDATE Post p SET p.score = p.score + :delta, p.upvoteCount = p.upvoteCount + :upvoteDelta, p.downvoteCount = p.downvoteCount + :downvoteDelta WHERE p.id = :postId")
    @Modifying
    void updatePostScore(@Param("postId") Long postId, @Param("delta") int delta, 
                        @Param("upvoteDelta") int upvoteDelta, @Param("downvoteDelta") int downvoteDelta);

    @Query("UPDATE Post p SET p.commentCount = p.commentCount + :delta WHERE p.id = :postId")
    @Modifying
    void updatePostCommentCount(@Param("postId") Long postId, @Param("delta") int delta);

    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    @Modifying
    void incrementPostViewCount(@Param("postId") Long postId);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:query% OR p.content LIKE %:query% AND p.isArchived = false")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.subreddit.id = :subredditId AND (p.title LIKE %:query% OR p.content LIKE %:query%) AND p.isArchived = false")
    Page<Post> searchPostsBySubreddit(@Param("subredditId") Long subredditId, @Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.postType = :postType AND p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findPostsByType(@Param("postType") com.lambrk.domain.Post.PostType postType, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isOver18 = true AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findNSFWPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.isArchived = true")
    List<Post> findPostsToArchive(@Param("since") Instant since);

    @Query("UPDATE Post p SET p.isArchived = true, p.archivedAt = :archivedAt WHERE p.id IN :postIds")
    @Modifying
    void archivePosts(@Param("postIds") List<Long> postIds, @Param("archivedAt") Instant archivedAt);

    @Query("SELECT p FROM Post p WHERE p.score >= :minScore AND p.isArchived = false ORDER BY p.score DESC")
    Page<Post> findPostsByMinScore(@Param("minScore") int minScore, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.createdAt >= :since AND p.author.id = :authorId AND p.isArchived = false ORDER BY p.createdAt DESC")
    Page<Post> findUserPostsSince(@Param("authorId") Long authorId, @Param("since") Instant since, Pageable pageable);
}
