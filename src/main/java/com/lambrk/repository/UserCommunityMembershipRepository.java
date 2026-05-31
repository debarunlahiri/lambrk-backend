package com.lambrk.repository;

import com.lambrk.domain.UserCommunityMembership;
import com.lambrk.domain.UserCommunityMembership.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCommunityMembershipRepository extends JpaRepository<UserCommunityMembership, UUID> {

    Optional<UserCommunityMembership> findByUserIdAndCommunityId(UUID userId, UUID communityId);

    List<UserCommunityMembership> findByUserIdAndStatus(UUID userId, MembershipStatus status);

    List<UserCommunityMembership> findByCommunityIdAndStatus(UUID communityId, MembershipStatus status);

    boolean existsByUserIdAndCommunityIdAndStatus(UUID userId, UUID communityId, MembershipStatus status);

    long countByCommunityIdAndStatus(UUID communityId, MembershipStatus status);
}
