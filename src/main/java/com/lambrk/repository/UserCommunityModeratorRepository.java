package com.lambrk.repository;

import com.lambrk.domain.UserCommunityModerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCommunityModeratorRepository extends JpaRepository<UserCommunityModerator, UUID> {

    Optional<UserCommunityModerator> findByUserIdAndCommunityIdAndIsActiveTrue(UUID userId, UUID communityId);

    List<UserCommunityModerator> findByUserIdAndIsActiveTrue(UUID userId);

    List<UserCommunityModerator> findByCommunityIdAndIsActiveTrue(UUID communityId);

    boolean existsByUserIdAndCommunityIdAndIsActiveTrue(UUID userId, UUID communityId);
}
