package com.lambrk.repository;

import com.lambrk.domain.UserCommunityModerator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCommunityModeratorRepository
    extends JpaRepository<UserCommunityModerator, UUID> {

  Optional<UserCommunityModerator> findByUserIdAndCommunityIdAndIsActiveTrue(
      UUID userId, UUID communityId);

  List<UserCommunityModerator> findByUserIdAndIsActiveTrue(UUID userId);

  List<UserCommunityModerator> findByCommunityIdAndIsActiveTrue(UUID communityId);

  boolean existsByUserIdAndCommunityIdAndIsActiveTrue(UUID userId, UUID communityId);
}
