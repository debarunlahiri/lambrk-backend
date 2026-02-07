package com.lambrk.repository;

import com.lambrk.domain.Vote;
import com.lambrk.domain.Post;
import com.lambrk.domain.Comment;
import com.lambrk.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long>, JpaSpecificationExecutor<Vote> {

    Optional<Vote> findByUserAndPost(User user, Post post);

    Optional<Vote> findByUserAndComment(User user, Comment comment);

    List<Vote> findByUser(User user);

    List<Vote> findByPost(Post post);

    List<Vote> findByComment(Comment comment);

    @Query("SELECT v FROM Vote v WHERE v.user.id = :userId AND v.post IS NOT NULL")
    List<Vote> findPostVotesByUser(@Param("userId") Long userId);

    @Query("SELECT v FROM Vote v WHERE v.user.id = :userId AND v.comment IS NOT NULL")
    List<Vote> findCommentVotesByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId AND v.voteType = 'UPVOTE'")
    long countUpvotesByPost(@Param("postId") Long postId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId AND v.voteType = 'DOWNVOTE'")
    long countDownvotesByPost(@Param("postId") Long postId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.voteType = 'UPVOTE'")
    long countUpvotesByComment(@Param("commentId") Long commentId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.comment.id = :commentId AND v.voteType = 'DOWNVOTE'")
    long countDownvotesByComment(@Param("commentId") Long commentId);

    @Query("SELECT v FROM Vote v WHERE v.createdAt >= :since")
    List<Vote> findVotesSince(@Param("since") Instant since);

    @Query("SELECT v FROM Vote v WHERE v.ipAddress = :ipAddress AND v.createdAt >= :since")
    List<Vote> findVotesByIpSince(@Param("ipAddress") String ipAddress, @Param("since") Instant since);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.id = :userId AND v.createdAt >= :since")
    long countUserVotesSince(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT v FROM Vote v WHERE v.post.id = :postId ORDER BY v.createdAt DESC")
    List<Vote> findVotesByPostOrdered(@Param("postId") Long postId);

    @Query("SELECT v FROM Vote v WHERE v.comment.id = :commentId ORDER BY v.createdAt DESC")
    List<Vote> findVotesByCommentOrdered(@Param("commentId") Long commentId);

    @Query("SELECT v FROM Vote v WHERE v.voteType = :voteType AND v.createdAt >= :since")
    List<Vote> findVotesByTypeSince(@Param("voteType") Vote.VoteType voteType, @Param("since") Instant since);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.createdAt >= :since")
    long countVotesSince(@Param("since") Instant since);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteType = 'UPVOTE' AND v.createdAt >= :since")
    long countUpvotesSince(@Param("since") Instant since);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.voteType = 'DOWNVOTE' AND v.createdAt >= :since")
    long countDownvotesSince(@Param("since") Instant since);
}
