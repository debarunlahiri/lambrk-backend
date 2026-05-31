package com.lambrk.repository;

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
import java.util.Set;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID>, JpaSpecificationExecutor<Community> {

    Optional<Community> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true ORDER BY s.subscriberCount DESC")
    Page<Community> findPublicCommunities(Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true AND s.name LIKE %:query%")
    Page<Community> searchPublicCommunities(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true ORDER BY s.activeUserCount DESC")
    Page<Community> findTrendingCommunities(Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true AND s.createdAt >= :since ORDER BY s.subscriberCount DESC")
    List<Community> findNewCommunitiesSince(@Param("since") Instant since);

    @Query("SELECT COUNT(s) FROM Community s WHERE s.isPublic = true")
    long countPublicCommunities();

    @Query("SELECT COUNT(s) FROM Community s WHERE s.createdAt >= :since")
    long countCommunitiesCreatedSince(@Param("since") Instant since);

    @Query("UPDATE Community s SET s.memberCount = s.memberCount + :delta WHERE s.id = :communityId")
    @Modifying
    void updateMemberCount(@Param("communityId") UUID communityId, @Param("delta") int delta);

    @Query("UPDATE Community s SET s.subscriberCount = s.subscriberCount + :delta WHERE s.id = :communityId")
    @Modifying
    void updateSubscriberCount(@Param("communityId") UUID communityId, @Param("delta") int delta);

    @Query("UPDATE Community s SET s.activeUserCount = s.activeUserCount + :delta WHERE s.id = :communityId")
    @Modifying
    void updateActiveUserCount(@Param("communityId") UUID communityId, @Param("delta") int delta);

    @Query("SELECT s FROM Community s WHERE s.isRestricted = true")
    List<Community> findRestrictedCommunities();

    @Query("SELECT s FROM Community s WHERE s.isOver18 = true AND s.isPublic = true")
    List<Community> findNSFWCommunities();

    @Query("SELECT s FROM Community s WHERE s.subscriberCount >= :minSubscribers AND s.isPublic = true ORDER BY s.subscriberCount DESC")
    Page<Community> findCommunitiesByMinSubscribers(@Param("minSubscribers") int minSubscribers, Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.activeUserCount >= :minActiveUsers AND s.isPublic = true ORDER BY s.activeUserCount DESC")
    Page<Community> findCommunitiesByMinActiveUsers(@Param("minActiveUsers") int minActiveUsers, Pageable pageable);

    @Query("SELECT s FROM Community s JOIN s.memberships m WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    Set<Community> findSubscribedCommunitiesByUser(@Param("userId") UUID userId);

    @Query("SELECT s FROM Community s JOIN s.moderators m WHERE m.user.id = :userId AND m.isActive = true")
    Set<Community> findModeratedCommunitiesByUser(@Param("userId") UUID userId);

    @Query("SELECT s FROM Community s WHERE s.createdBy.id = :userId")
    List<Community> findCreatedCommunitiesByUser(@Param("userId") UUID userId);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true AND (s.name LIKE %:query% OR s.title LIKE %:query% OR s.description LIKE %:query%)")
    Page<Community> searchCommunities(@Param("query") String query, Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true ORDER BY s.createdAt DESC")
    Page<Community> findNewestCommunities(Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true ORDER BY s.memberCount DESC")
    Page<Community> findLargestCommunities(Pageable pageable);

    @Query("SELECT s FROM Community s WHERE s.isPublic = true AND s.activeUserCount > 0 ORDER BY s.activeUserCount DESC")
    Page<Community> findActiveCommunities(Pageable pageable);
}
