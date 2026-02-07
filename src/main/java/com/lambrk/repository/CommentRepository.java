package com.lambrk.repository;

import com.lambrk.domain.Comment;
import com.lambrk.domain.Post;
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

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    Page<Comment> findByPost(Post post, Pageable pageable);

    Page<Comment> findByPostAndParentIsNull(Post post, Pageable pageable);

    Page<Comment> findByAuthor(User author, Pageable pageable);

    List<Comment> findByParent(Comment parent);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.score DESC")
    List<Comment> findTopLevelCommentsByPost(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parent.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParent(@Param("postId") Long postId, @Param("parentId") Long parentId);

    @Query("SELECT c FROM Comment c WHERE c.isDeleted = false AND c.isRemoved = false AND c.post.id = :postId ORDER BY c.score DESC")
    Page<Comment> findActiveCommentsByPost(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false AND c.isRemoved = false")
    long countActiveCommentsByPost(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :authorId AND c.isDeleted = false AND c.isRemoved = false")
    long countActiveCommentsByAuthor(@Param("authorId") Long authorId);

    @Query("UPDATE Comment c SET c.score = c.score + :delta, c.upvoteCount = c.upvoteCount + :upvoteDelta, c.downvoteCount = c.downvoteCount + :downvoteDelta WHERE c.id = :commentId")
    @Modifying
    void updateCommentScore(@Param("commentId") Long commentId, @Param("delta") int delta,
                           @Param("upvoteDelta") int upvoteDelta, @Param("downvoteDelta") int downvoteDelta);

    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + :delta WHERE c.id = :commentId")
    @Modifying
    void updateCommentReplyCount(@Param("commentId") Long commentId, @Param("delta") int delta);

    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:query% AND c.isDeleted = false AND c.isRemoved = false")
    Page<Comment> searchComments(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.content LIKE %:query% AND c.isDeleted = false AND c.isRemoved = false")
    Page<Comment> searchCommentsByPost(@Param("postId") Long postId, @Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.isStickied = true AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.createdAt DESC")
    List<Comment> findStickiedComments();

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.isStickied = true AND c.isDeleted = false AND c.isRemoved = false")
    List<Comment> findStickiedCommentsByPost(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.isEdited = true AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.editedAt DESC")
    Page<Comment> findEditedComments(Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.createdAt >= :since AND c.isDeleted = false AND c.isRemoved = false")
    List<Comment> findRecentComments(@Param("since") Instant since);

    @Query("SELECT c FROM Comment c WHERE c.depthLevel <= :maxDepth AND c.post.id = :postId AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.createdAt ASC")
    List<Comment> findCommentsByMaxDepth(@Param("postId") Long postId, @Param("maxDepth") int maxDepth);

    @Query("SELECT c FROM Comment c WHERE c.author.id = :authorId AND c.createdAt >= :since AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.createdAt DESC")
    Page<Comment> findUserCommentsSince(@Param("authorId") Long authorId, @Param("since") Instant since, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.score >= :minScore AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.score DESC")
    Page<Comment> findCommentsByMinScore(@Param("minScore") int minScore, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.isOver18 = true AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.score DESC")
    Page<Comment> findNSFWComments(Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.isCollapsed = true AND c.isDeleted = false AND c.isRemoved = false")
    List<Comment> findCollapsedComments();

    @Query("UPDATE Comment c SET c.isDeleted = true, c.deletedAt = :deletedAt WHERE c.id = :commentId")
    @Modifying
    void softDeleteComment(@Param("commentId") Long commentId, @Param("deletedAt") Instant deletedAt);

    @Query("UPDATE Comment c SET c.isRemoved = true, c.removedAt = :removedAt WHERE c.id = :commentId")
    @Modifying
    void removeComment(@Param("commentId") Long commentId, @Param("removedAt") Instant removedAt);

    @Query("SELECT c FROM Comment c WHERE c.awardCount > 0 AND c.isDeleted = false AND c.isRemoved = false ORDER BY c.awardCount DESC")
    Page<Comment> findAwardedComments(Pageable pageable);
}
