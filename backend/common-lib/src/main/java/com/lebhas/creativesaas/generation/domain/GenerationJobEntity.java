package com.lebhas.creativesaas.generation.domain;

import com.lebhas.creativesaas.common.audit.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "generation_jobs", schema = "platform")
public class GenerationJobEntity extends TenantAwareEntity {

    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 40)
    private GenerationJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CreativeGenerationStatus status;

    @Column(name = "provider_job_id", length = 160)
    private String providerJobId;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "queue_name", nullable = false, length = 160)
    private String queueName;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    protected GenerationJobEntity() {
    }

    public static GenerationJobEntity queue(
            UUID workspaceId,
            UUID requestId,
            GenerationJobType jobType,
            String queueName,
            int maxAttempts
    ) {
        GenerationJobEntity job = new GenerationJobEntity();
        job.assignWorkspace(workspaceId);
        job.requestId = requestId;
        job.jobType = jobType;
        job.status = CreativeGenerationStatus.QUEUED;
        job.queueName = queueName;
        job.maxAttempts = Math.max(1, maxAttempts);
        return job;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public GenerationJobType getJobType() {
        return jobType;
    }

    public CreativeGenerationStatus getStatus() {
        return status;
    }

    public String getProviderJobId() {
        return providerJobId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public String getQueueName() {
        return queueName;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getFailedAt() {
        return failedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void markStarted() {
        this.status = CreativeGenerationStatus.PROCESSING;
        this.attemptCount += 1;
        this.startedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markQueuedForRetry(String errorMessage) {
        this.status = CreativeGenerationStatus.QUEUED;
        this.errorMessage = truncate(errorMessage);
    }

    public void markCompleted(String providerJobId) {
        this.status = CreativeGenerationStatus.COMPLETED;
        this.providerJobId = normalizeNullable(providerJobId);
        this.completedAt = Instant.now();
        this.failedAt = null;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = CreativeGenerationStatus.FAILED;
        this.failedAt = Instant.now();
        this.errorMessage = truncate(errorMessage);
    }

    public void markCancelled(String reason) {
        this.status = CreativeGenerationStatus.CANCELLED;
        this.failedAt = Instant.now();
        this.errorMessage = truncate(reason);
    }

    public boolean canRetry() {
        return attemptCount < maxAttempts;
    }

    public boolean isTerminal() {
        return status == CreativeGenerationStatus.COMPLETED
                || status == CreativeGenerationStatus.FAILED
                || status == CreativeGenerationStatus.CANCELLED;
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 1000 ? normalized : normalized.substring(0, 1000);
    }
}
