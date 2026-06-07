package com.lambrk.repository;

import com.lambrk.domain.User;
import com.lambrk.domain.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    Optional<UserFollow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowingAndStatus(User follower, User following, UserFollow.FollowStatus status);

    long countByFollowingAndStatus(User following, UserFollow.FollowStatus status);

    long countByFollowerAndStatus(User follower, UserFollow.FollowStatus status);

    @Query("SELECT f.follower FROM UserFollow f WHERE f.following = :user AND f.status = 'ACTIVE'")
    Page<User> findFollowers(@Param("user") User user, Pageable pageable);

    @Query("SELECT f.following FROM UserFollow f WHERE f.follower = :user AND f.status = 'ACTIVE'")
    Page<User> findFollowing(@Param("user") User user, Pageable pageable);

    @Query("""
        SELECT f1.follower
        FROM UserFollow f1
        WHERE f1.following = :firstUser
          AND f1.status = 'ACTIVE'
          AND EXISTS (
              SELECT 1
              FROM UserFollow f2
              WHERE f2.following = :secondUser
                AND f2.follower = f1.follower
                AND f2.status = 'ACTIVE'
          )
        """)
    Page<User> findMutualFollowers(
        @Param("firstUser") User firstUser,
        @Param("secondUser") User secondUser,
        Pageable pageable
    );

    @Query("""
        SELECT f1.following
        FROM UserFollow f1
        WHERE f1.follower = :firstUser
          AND f1.status = 'ACTIVE'
          AND EXISTS (
              SELECT 1
              FROM UserFollow f2
              WHERE f2.follower = :secondUser
                AND f2.following = f1.following
                AND f2.status = 'ACTIVE'
          )
        """)
    Page<User> findMutualFollowing(
        @Param("firstUser") User firstUser,
        @Param("secondUser") User secondUser,
        Pageable pageable
    );
}
