package com.lambrk.service;

import com.lambrk.domain.FreeTierUsage;
import com.lambrk.exception.FreeTierLimitExceededException;
import com.lambrk.repository.FreeTierUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class FreeTierLimitService {

    private static final Logger logger = LoggerFactory.getLogger(FreeTierLimitService.class);

    private final FreeTierUsageRepository freeTierUsageRepository;

    @Value("${app.free-tier.storage-limit-mb:5120}") // 5GB default
    private long storageLimitMB;

    @Value("${app.free-tier.monthly-upload-limit:100}") // 100 uploads/month
    private int monthlyUploadLimit;

    @Value("${app.free-tier.monthly-bandwidth-gb:100}") // 100GB bandwidth/month
    private long monthlyBandwidthGB;

    @Value("${app.free-tier.enabled:true}")
    private boolean freeTierEnabled;

    public FreeTierLimitService(FreeTierUsageRepository freeTierUsageRepository) {
        this.freeTierUsageRepository = freeTierUsageRepository;
    }

    public void checkUploadAllowed(UUID userId, long fileSizeBytes) {
        if (!freeTierEnabled) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();
        Optional<FreeTierUsage> usageOpt = freeTierUsageRepository
            .findFreeTierUsageByUserAndPeriod(userId, currentMonth.getYear(), currentMonth.getMonthValue());

        FreeTierUsage usage = usageOpt.orElseGet(() ->
            FreeTierUsage.createForUser(userId, true)
        );

        // Check storage limit
        long storageLimitBytes = storageLimitMB * 1024 * 1024;
        long projectedStorage = usage.getStorageBytesUsed() + fileSizeBytes;

        if (projectedStorage > storageLimitBytes) {
            throw new FreeTierLimitExceededException(
                "Storage limit exceeded. Free tier limit: " + storageLimitMB + "MB. " +
                "Current usage: " + (usage.getStorageBytesUsed() / 1024 / 1024) + "MB, " +
                "File size: " + (fileSizeBytes / 1024 / 1024) + "MB"
            );
        }

        // Check monthly upload limit
        if (usage.getUploadsCount() >= monthlyUploadLimit) {
            throw new FreeTierLimitExceededException(
                "Monthly upload limit exceeded. Free tier limit: " + monthlyUploadLimit + " uploads/month. " +
                "Current uploads: " + usage.getUploadsCount()
            );
        }
    }

    public FreeTierUsage recordUpload(UUID userId, long fileSizeBytes) {
        if (!freeTierEnabled) {
            return null;
        }

        YearMonth currentMonth = YearMonth.now();
        Optional<FreeTierUsage> usageOpt = freeTierUsageRepository
            .findByUserIdAndPeriodYearAndPeriodMonth(userId, currentMonth.getYear(), currentMonth.getMonthValue());

        FreeTierUsage usage = usageOpt.orElseGet(() ->
            FreeTierUsage.createForUser(userId, true)
        );

        FreeTierUsage updated = usage
            .withAddedStorage(fileSizeBytes)
            .withIncrementedUploads();

        FreeTierUsage saved = freeTierUsageRepository.save(updated);
        logger.info("Recorded upload for user {}: {} bytes. Total storage: {} bytes, Uploads: {}",
            userId, fileSizeBytes, saved.getStorageBytesUsed(), saved.getUploadsCount());

        return saved;
    }

    public void recordFileDeletion(UUID userId, long fileSizeBytes) {
        if (!freeTierEnabled) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();
        Optional<FreeTierUsage> usageOpt = freeTierUsageRepository
            .findByUserIdAndPeriodYearAndPeriodMonth(userId, currentMonth.getYear(), currentMonth.getMonthValue());

        if (usageOpt.isPresent()) {
            FreeTierUsage usage = usageOpt.get();
            FreeTierUsage updated = usage.withRemovedStorage(fileSizeBytes);
            freeTierUsageRepository.save(updated);
            logger.info("Recorded file deletion for user {}: {} bytes removed", userId, fileSizeBytes);
        }
    }

    public void recordBandwidthUsage(UUID userId, long bytesTransferred) {
        if (!freeTierEnabled) {
            return;
        }

        YearMonth currentMonth = YearMonth.now();
        Optional<FreeTierUsage> usageOpt = freeTierUsageRepository
            .findByUserIdAndPeriodYearAndPeriodMonth(userId, currentMonth.getYear(), currentMonth.getMonthValue());

        FreeTierUsage usage = usageOpt.orElseGet(() ->
            FreeTierUsage.createForUser(userId, true)
        );

        // Check bandwidth limit
        long bandwidthLimitBytes = monthlyBandwidthGB * 1024L * 1024L * 1024L;
        long projectedBandwidth = usage.getBandwidthBytes() + bytesTransferred;

        if (projectedBandwidth > bandwidthLimitBytes) {
            throw new FreeTierLimitExceededException(
                "Monthly bandwidth limit exceeded. Free tier limit: " + monthlyBandwidthGB + "GB/month. " +
                "Current bandwidth: " + (usage.getBandwidthBytes() / 1024 / 1024 / 1024) + "GB"
            );
        }

        FreeTierUsage updated = usage.withAddedBandwidth(bytesTransferred);
        freeTierUsageRepository.save(updated);
    }

    public FreeTierUsage getCurrentUsage(UUID userId) {
        YearMonth currentMonth = YearMonth.now();
        return freeTierUsageRepository
            .findByUserIdAndPeriodYearAndPeriodMonth(userId, currentMonth.getYear(), currentMonth.getMonthValue())
            .orElseGet(() -> FreeTierUsage.createForUser(userId, true));
    }

    public long getStorageLimitBytes() {
        return storageLimitMB * 1024 * 1024;
    }

    public int getMonthlyUploadLimit() {
        return monthlyUploadLimit;
    }

    public long getMonthlyBandwidthLimitBytes() {
        return monthlyBandwidthGB * 1024L * 1024L * 1024L;
    }

    public boolean isFreeTierEnabled() {
        return freeTierEnabled;
    }

    public record FreeTierStatus(
        long storageUsedBytes,
        long storageLimitBytes,
        int uploadsThisMonth,
        int uploadLimit,
        long bandwidthUsedBytes,
        long bandwidthLimitBytes,
        boolean isFreeTier,
        boolean withinLimits
    ) {}

    public FreeTierStatus getUserStatus(UUID userId) {
        FreeTierUsage usage = getCurrentUsage(userId);

        long storageLimitBytes = getStorageLimitBytes();
        long bandwidthLimitBytes = getMonthlyBandwidthLimitBytes();

        boolean withinLimits = usage.getStorageBytesUsed() < storageLimitBytes &&
                              usage.getUploadsCount() < monthlyUploadLimit &&
                              usage.getBandwidthBytes() < bandwidthLimitBytes;

        return new FreeTierStatus(
            usage.getStorageBytesUsed(),
            storageLimitBytes,
            usage.getUploadsCount(),
            monthlyUploadLimit,
            usage.getBandwidthBytes(),
            bandwidthLimitBytes,
            usage.isFreeTier(),
            withinLimits
        );
    }
}
