package com.lambrk.repository;

import com.lambrk.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "p.createdAt >= :since AND " +
           "p.isArchived = false " +
           "ORDER BY p.score DESC")
    Page<Post> searchPosts(@Param("query") String query, @Param("since") java.time.Instant since, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.subreddit s WHERE " +
           "s.name IN :subreddits AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "p.createdAt >= :since AND " +
           "p.isArchived = false " +
           "ORDER BY p.score DESC")
    Page<Post> searchPostsBySubreddits(@Param("subreddits") List<String> subreddits, 
                                           @Param("query") String query, 
                                           @Param("since") java.time.Instant since, 
                                           Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) AND " +
           "p.createdAt >= :since AND " +
           "p.isArchived = false AND " +
           "p.flairText IN :flairs " +
           "ORDER BY p.score DESC")
    Page<Post> searchPostsByFlairs(@Param("query") String query, 
                                       @Param("since") java.time.Instant since,
                                       @Param("flairs") List<String> flairs,
                                       Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
           "p.score >= :minScore AND " +
           "p.createdAt >= :since AND " +
           "p.isArchived = false " +
           "ORDER BY p.score DESC")
    Page<Post> findPostsByMinScore(@Param("minScore") int minScore,
                                       @Param("since") java.time.Instant since,
                                       Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
           "p.commentCount >= :minComments AND " +
           "p.createdAt >= :since AND " +
           "p.isArchived = false " +
           "ORDER BY p.commentCount DESC")
    Page<Post> findPostsByMinComments(@Param("minComments") int minComments,
                                           @Param("since") java.time.Instant since,
                                           Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
           "p.isOver18 = :isOver18 AND " +
           "p.createdAt >= :since AND " +
           "p.isArchived = false " +
           "ORDER BY p.score DESC")
    Page<Post> findNSFWPosts(@Param("isOver18") boolean isOver18,
                                   @Param("since") java.time.Instant since,
                                   Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
           "p.isStickied = true AND " +
           "p.isArchived = false " +
           "ORDER BY p.createdAt DESC")
    List<Post> findStickiedPosts();

    @Query("SELECT p FROM Post p WHERE " +
           "p.createdAt >= :since AND " +
           "p.isArchived = true")
    List<Post> findPostsToArchive(@Param("since") java.time.Instant since);

    @Query("SELECT DISTINCT p.title FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY LENGTH(p.title) ASC " +
           "LIMIT 10")
    List<String> findTitleSuggestions(@Param("query") String query);

    @Query("SELECT DISTINCT s.name FROM Subreddit s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY LENGTH(s.name) ASC " +
           "LIMIT 10")
    List<String> findSubredditSuggestions(@Param("query") String query);

    @Query("SELECT DISTINCT u.username FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY LENGTH(u.username) ASC " +
           "LIMIT 10")
    List<String> findUsernameSuggestions(@Param("query") String query);
}
