package com.lambrk.repository;

import com.lambrk.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c ORDER BY c.sortOrder ASC, c.name ASC")
    Page<Category> findAllOrdered(Pageable pageable);

    @Query("SELECT c FROM Category c JOIN c.communities comm WHERE comm.id = :communityId")
    Page<Category> findByCommunityId(@Param("communityId") UUID communityId, Pageable pageable);
}
