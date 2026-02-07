package com.lambrk.repository;

import com.lambrk.domain.User;
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
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.karma DESC")
    Page<User> findTopUsersByKarma(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedSince(@Param("since") Instant since);

    @Query("SELECT u FROM User u WHERE u.isVerified = true ORDER BY u.createdAt DESC")
    List<User> findVerifiedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countUsersCreatedSince(@Param("since") Instant since);

    @Query("UPDATE User u SET u.karma = u.karma + :delta WHERE u.id = :userId")
    @Modifying
    void updateUserKarma(@Param("userId") Long userId, @Param("delta") int delta);

    @Query("SELECT u FROM User u JOIN u.subscribedSubreddits s WHERE s.id = :subredditId")
    Set<User> findSubscribersBySubredditId(@Param("subredditId") Long subredditId);

    @Query("SELECT u FROM User u JOIN u.moderatedSubreddits s WHERE s.id = :subredditId")
    Set<User> findModeratorsBySubredditId(@Param("subredditId") Long subredditId);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:query% OR u.email LIKE %:query%")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND (u.username LIKE %:query% OR u.displayName LIKE %:query%)")
    Page<User> searchActiveUsers(@Param("query") String query, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.karma >= :minKarma ORDER BY u.karma DESC")
    Page<User> findUsersByMinKarma(@Param("minKarma") int minKarma, Pageable pageable);
}
