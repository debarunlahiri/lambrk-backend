package com.lambrk.repository;

import com.lambrk.domain.FreeTierUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FreeTierUsageRepository extends JpaRepository<FreeTierUsage, Long> {

    Optional<FreeTierUsage> findByUserIdAndPeriodYearAndPeriodMonth(Long userId, int year, int month);

    @Query("SELECT ftu FROM FreeTierUsage ftu WHERE ftu.userId = :userId AND ftu.periodYear = :year AND ftu.periodMonth = :month AND ftu.isFreeTier = true")
    Optional<FreeTierUsage> findFreeTierUsageByUserAndPeriod(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT SUM(ftu.storageBytesUsed) FROM FreeTierUsage ftu WHERE ftu.userId = :userId AND ftu.isFreeTier = true")
    Long getTotalStorageUsedByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(ftu.bandwidthBytes) FROM FreeTierUsage ftu WHERE ftu.userId = :userId AND ftu.periodYear = :year AND ftu.periodMonth = :month")
    Long getMonthlyBandwidthByUser(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}
