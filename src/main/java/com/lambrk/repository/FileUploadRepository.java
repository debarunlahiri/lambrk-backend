package com.lambrk.repository;

import com.lambrk.domain.FileUpload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileUploadRepository extends JpaRepository<FileUpload, Long> {

    Optional<FileUpload> findByFileName(String fileName);

    @Query("SELECT f FROM FileUpload f WHERE f.uploadedBy.id = :userId ORDER BY f.uploadedAt DESC")
    Page<FileUpload> findByUploadedByOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT f FROM FileUpload f WHERE f.type = :type AND f.isPublic = :isPublic ORDER BY f.uploadedAt DESC")
    Page<FileUpload> findByTypeAndIsPublicOrderByCreatedAtDesc(@Param("type") FileUpload.FileUploadType type, @Param("isPublic") boolean isPublic, Pageable pageable);

    @Query("SELECT f FROM FileUpload f WHERE f.uploadedBy.id = :userId AND f.type = :type ORDER BY f.uploadedAt DESC")
    Page<FileUpload> findByUploadedByAndTypeOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("type") FileUpload.FileUploadType type, Pageable pageable);

    @Query("SELECT f FROM FileUpload f WHERE f.isPublic = true ORDER BY f.uploadedAt DESC")
    Page<FileUpload> findPublicFilesOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT f FROM FileUpload f WHERE f.isNSFW = :isNSFW AND f.isPublic = true ORDER BY f.uploadedAt DESC")
    Page<FileUpload> findPublicNSFWFilesOrderByCreatedAtDesc(@Param("isNSFW") boolean isNSFW, Pageable pageable);

    @Query("SELECT f FROM FileUpload f WHERE f.uploadedBy.id = :userId AND f.isPublic = :isPublic ORDER BY f.uploadedAt DESC")
    Page<FileUpload> findByUploadedByAndIsPublicOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("isPublic") boolean isPublic, Pageable pageable);

    @Query("SELECT COUNT(f) FROM FileUpload f WHERE f.uploadedBy.id = :userId")
    long countFilesByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(f) FROM FileUpload f WHERE f.uploadedBy.id = :userId AND f.type = :type")
    long countFilesByUserAndType(@Param("userId") Long userId, @Param("type") FileUpload.FileUploadType type);

    @Query("SELECT SUM(f.fileSize) FROM FileUpload f WHERE f.uploadedBy.id = :userId")
    Long getTotalFileSizeByUser(@Param("userId") Long userId);

    @Query("SELECT f.type, COUNT(f) FROM FileUpload f WHERE f.uploadedBy.id = :userId GROUP BY f.type")
    List<Object[]> getFileTypeStatsByUser(@Param("userId") Long userId);

    @Query("SELECT f.uploadedBy.id, COUNT(f) FROM FileUpload f GROUP BY f.uploadedBy.id ORDER BY COUNT(f) DESC")
    List<Object[]> getTopUploaders();

    @Query("SELECT f FROM FileUpload f WHERE f.uploadedAt >= :since ORDER BY f.uploadedAt DESC")
    List<FileUpload> findFilesSince(@Param("since") java.time.Instant since);

    @Query("SELECT f FROM FileUpload f WHERE f.fileSize > :minSize ORDER BY f.fileSize DESC")
    List<FileUpload> findLargeFiles(@Param("minSize") long minSize);

    @Query("SELECT f FROM FileUpload f WHERE f.checksum = :checksum")
    Optional<FileUpload> findByChecksum(@Param("checksum") String checksum);

    @Query("SELECT f FROM FileUpload f WHERE f.originalFileName LIKE %:searchTerm%")
    List<FileUpload> searchByFileName(@Param("searchTerm") String searchTerm);
}
