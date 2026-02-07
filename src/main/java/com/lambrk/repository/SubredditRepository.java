package com.lambrk.repository;

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
import java.util.Set;

@Repository
public interface SubredditRepository extends JpaRepository<Subreddit, Long>, JpaSpecificationExecutor<Subreddit> {

    Optional<Subreddit> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true ORDER BY s.subscriberCount DESC")
    Page<Subreddit> findPublicSubreddits(Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true AND s.name LIKE %:query%")
    Page<Subreddit> searchPublicSubreddits(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true ORDER BY s.activeUserCount DESC")
    Page<Subreddit> findTrendingSubreddits(Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true AND s.createdAt >= :since ORDER BY s.subscriberCount DESC")
    List<Subreddit> findNewSubredditsSince(@Param("since") Instant since);

    @Query("SELECT COUNT(s) FROM Subreddit s WHERE s.isPublic = true")
    long countPublicSubreddits();

    @Query("SELECT COUNT(s) FROM Subreddit s WHERE s.createdAt >= :since")
    long countSubredditsCreatedSince(@Param("since") Instant since);

    @Query("UPDATE Subreddit s SET s.memberCount = s.memberCount + :delta WHERE s.id = :subredditId")
    @Modifying
    void updateMemberCount(@Param("subredditId") Long subredditId, @Param("delta") int delta);

    @Query("UPDATE Subreddit s SET s.subscriberCount = s.subscriberCount + :delta WHERE s.id = :subredditId")
    @Modifying
    void updateSubscriberCount(@Param("subredditId") Long subredditId, @Param("delta") int delta);

    @Query("UPDATE Subreddit s SET s.activeUserCount = s.activeUserCount + :delta WHERE s.id = :subredditId")
    @Modifying
    void updateActiveUserCount(@Param("subredditId") Long subredditId, @Param("delta") int delta);

    @Query("SELECT s FROM Subreddit s WHERE s.isRestricted = true")
    List<Subreddit> findRestrictedSubreddits();

    @Query("SELECT s FROM Subreddit s WHERE s.isOver18 = true AND s.isPublic = true")
    List<Subreddit> findNSFWSubreddits();

    @Query("SELECT s FROM Subreddit s WHERE s.subscriberCount >= :minSubscribers AND s.isPublic = true ORDER BY s.subscriberCount DESC")
    Page<Subreddit> findSubredditsByMinSubscribers(@Param("minSubscribers") int minSubscribers, Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.activeUserCount >= :minActiveUsers AND s.isPublic = true ORDER BY s.activeUserCount DESC")
    Page<Subreddit> findSubredditsByMinActiveUsers(@Param("minActiveUsers") int minActiveUsers, Pageable pageable);

    @Query("SELECT s FROM Subreddit s JOIN s.members m WHERE m.id = :userId")
    Set<Subreddit> findSubscribedSubredditsByUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Subreddit s JOIN s.moderators m WHERE m.id = :userId")
    Set<Subreddit> findModeratedSubredditsByUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Subreddit s WHERE s.createdBy.id = :userId")
    List<Subreddit> findCreatedSubredditsByUser(@Param("userId") Long userId);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true AND (s.name LIKE %:query% OR s.title LIKE %:query% OR s.description LIKE %:query%)")
    Page<Subreddit> searchSubreddits(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true ORDER BY s.createdAt DESC")
    Page<Subreddit> findNewestSubreddits(Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true ORDER BY s.memberCount DESC")
    Page<Subreddit> findLargestSubreddits(Pageable pageable);

    @Query("SELECT s FROM Subreddit s WHERE s.isPublic = true AND s.activeUserCount > 0 ORDER BY s.activeUserCount DESC")
    Page<Subreddit> findActiveSubreddits(Pageable pageable);
}
