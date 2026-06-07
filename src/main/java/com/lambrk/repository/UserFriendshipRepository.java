package com.lambrk.repository;

import com.lambrk.domain.User;
import com.lambrk.domain.UserFriendship;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFriendshipRepository extends JpaRepository<UserFriendship, UUID> {

  Optional<UserFriendship> findByUserOneAndUserTwo(User userOne, User userTwo);

  long countByUserOneAndStatusOrUserTwoAndStatus(
      User userOne,
      UserFriendship.FriendshipStatus statusOne,
      User userTwo,
      UserFriendship.FriendshipStatus statusTwo);

  @Query(
      """
      SELECT CASE WHEN f.userOne = :user THEN f.userTwo ELSE f.userOne END
      FROM UserFriendship f
      WHERE (f.userOne = :user OR f.userTwo = :user)
        AND f.status = 'ACCEPTED'
      """)
  Page<User> findFriends(@Param("user") User user, Pageable pageable);

  @Query(
      """
      SELECT f
      FROM UserFriendship f
      WHERE f.addressee = :user
        AND f.status = 'PENDING'
      ORDER BY f.createdAt DESC
      """)
  Page<UserFriendship> findIncomingRequests(@Param("user") User user, Pageable pageable);

  @Query(
      """
      SELECT f
      FROM UserFriendship f
      WHERE f.requester = :user
        AND f.status = 'PENDING'
      ORDER BY f.createdAt DESC
      """)
  Page<UserFriendship> findOutgoingRequests(@Param("user") User user, Pageable pageable);

  @Query(
      """
SELECT CASE WHEN f1.userOne = :firstUser THEN f1.userTwo ELSE f1.userOne END
FROM UserFriendship f1
WHERE (f1.userOne = :firstUser OR f1.userTwo = :firstUser)
  AND f1.status = 'ACCEPTED'
  AND EXISTS (
      SELECT 1
      FROM UserFriendship f2
      WHERE (f2.userOne = :secondUser OR f2.userTwo = :secondUser)
        AND f2.status = 'ACCEPTED'
        AND (
            (f2.userOne = CASE WHEN f1.userOne = :firstUser THEN f1.userTwo ELSE f1.userOne END)
            OR
            (f2.userTwo = CASE WHEN f1.userOne = :firstUser THEN f1.userTwo ELSE f1.userOne END)
        )
  )
""")
  Page<User> findMutualFriends(
      @Param("firstUser") User firstUser, @Param("secondUser") User secondUser, Pageable pageable);
}
