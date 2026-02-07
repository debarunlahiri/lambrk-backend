package com.lambrk.repository;

import com.lambrk.domain.LogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long> {

    Page<LogEntry> findByOrderByTimestampDesc(Pageable pageable);

    Page<LogEntry> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Page<LogEntry> findByEndpointContainingIgnoreCaseOrderByTimestampDesc(String endpoint, Pageable pageable);

    Page<LogEntry> findByMethodOrderByTimestampDesc(String method, Pageable pageable);

    Page<LogEntry> findByStatusCodeOrderByTimestampDesc(Integer statusCode, Pageable pageable);

    Page<LogEntry> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);

    Page<LogEntry> findByLogLevelOrderByTimestampDesc(String logLevel, Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
    Page<LogEntry> findByTimestampBetween(@Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.userId = :userId AND l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
    Page<LogEntry> findByUserIdAndTimestampBetween(@Param("userId") Long userId, @Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.endpoint LIKE %:endpoint% AND l.timestamp BETWEEN :start AND :end ORDER BY l.timestamp DESC")
    Page<LogEntry> findByEndpointContainingAndTimestampBetween(@Param("endpoint") String endpoint, @Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.statusCode >= 400 ORDER BY l.timestamp DESC")
    Page<LogEntry> findErrors(Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.statusCode >= 500 ORDER BY l.timestamp DESC")
    Page<LogEntry> findServerErrors(Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.exceptionMessage IS NOT NULL ORDER BY l.timestamp DESC")
    Page<LogEntry> findExceptions(Pageable pageable);

    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.timestamp >= :since")
    Long countSince(@Param("since") Instant since);

    @Query("SELECT COUNT(l) FROM LogEntry l WHERE l.statusCode >= 400 AND l.timestamp >= :since")
    Long countErrorsSince(@Param("since") Instant since);

    @Query("SELECT l.method, COUNT(l) FROM LogEntry l WHERE l.timestamp >= :since GROUP BY l.method")
    List<Object[]> countByMethodSince(@Param("since") Instant since);

    @Query("SELECT l.endpoint, COUNT(l) FROM LogEntry l WHERE l.timestamp >= :since GROUP BY l.endpoint ORDER BY COUNT(l) DESC")
    List<Object[]> findMostFrequentEndpoints(@Param("since") Instant since, Pageable pageable);

    @Query("SELECT AVG(l.responseTimeMs) FROM LogEntry l WHERE l.timestamp >= :since")
    Optional<Double> findAverageResponseTime(@Param("since") Instant since);

    @Query("SELECT l FROM LogEntry l WHERE l.correlationId = :correlationId ORDER BY l.timestamp")
    List<LogEntry> findByCorrelationId(@Param("correlationId") String correlationId);

    @Query("SELECT l FROM LogEntry l WHERE l.isAuthenticated = false ORDER BY l.timestamp DESC")
    Page<LogEntry> findAnonymousRequests(Pageable pageable);

    @Query("SELECT l FROM LogEntry l WHERE l.isAuthenticated = true ORDER BY l.timestamp DESC")
    Page<LogEntry> findAuthenticatedRequests(Pageable pageable);
}
