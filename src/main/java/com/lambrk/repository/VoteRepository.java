package com.lambrk.repository;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
import com.lambrk.domain.User;
import com.lambrk.domain.Vote;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID>, JpaSpecificationExecutor<Vote> {

  Optional<Vote> findByUserAndPost(User user, Post post);

  Optional<Vote> findByUserAndComment(User user, Comment comment);

  List<Vote> findByUser(User user);

  List<Vote> findByPost(Post post);

  List<Vote> findByComment(Comment comment);

  @Query("SELECT v FROM Vote v WHERE v.user.id = :userId AND v.post IS NOT NULL")
  List<Vote> findPostVotesByUser(@Param("userId") UUID userId);

  @Query("SELECT v FROM Vote v WHERE v.user.id = :userId AND v.comment IS NOT NULL")
  List<Vote> findCommentVotesByUser(@Param("userId") UUID userId);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId AND v.voteType = 'LIKE'")
  long countLikesByPost(@Param("postId") UUID postId);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId AND v.voteType = 'DISLIKE'")
  long countDislikesByPost(@Param("postId") UUID postId);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.voteType = 'LIKE'")
  long countLikesByComment(@Param("commentId") UUID commentId);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.voteType = 'DISLIKE'")
  long countDislikesByComment(@Param("commentId") UUID commentId);

  @Query("SELECT v FROM Vote v WHERE v.createdAt >= :since")
  List<Vote> findVotesSince(@Param("since") Instant since);

  @Query("SELECT v FROM Vote v WHERE v.ipAddress = :ipAddress AND v.createdAt >= :since")
  List<Vote> findVotesByIpSince(
      @Param("ipAddress") String ipAddress, @Param("since") Instant since);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.id = :userId AND v.createdAt >= :since")
  long countUserVotesSince(@Param("userId") UUID userId, @Param("since") Instant since);

  @Query("SELECT v FROM Vote v WHERE v.post.id = :postId ORDER BY v.createdAt DESC")
  List<Vote> findVotesByPostOrdered(@Param("postId") UUID postId);

  @Query("SELECT v FROM Vote v WHERE v.comment.id = :commentId ORDER BY v.createdAt DESC")
  List<Vote> findVotesByCommentOrdered(@Param("commentId") UUID commentId);

  @Query("SELECT v FROM Vote v WHERE v.voteType = :voteType AND v.createdAt >= :since")
  List<Vote> findVotesByTypeSince(
      @Param("voteType") Vote.VoteType voteType, @Param("since") Instant since);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.createdAt >= :since")
  long countVotesSince(@Param("since") Instant since);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteType = 'LIKE' AND v.createdAt >= :since")
  long countLikesSince(@Param("since") Instant since);

  @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteType = 'DISLIKE' AND v.createdAt >= :since")
  long countDislikesSince(@Param("since") Instant since);
}
