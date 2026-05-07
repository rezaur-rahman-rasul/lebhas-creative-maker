package com.lebhas.creativesaas.identity.domain;

import com.lebhas.creativesaas.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "refresh_tokens",
        schema = "platform",
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_tokens_token_id", columnNames = "token_id")
)
public class RefreshTokenEntity extends BaseEntity {

    @Column(name = "token_id", nullable = false, updatable = false)
    private UUID tokenId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "client_ip", length = 60)
    private String clientIp;

    @Column(name = "user_agent", length = 300)
    private String userAgent;

    protected RefreshTokenEntity() {
    }

    public static RefreshTokenEntity issue(
            UUID tokenId,
            UUID userId,
            UUID workspaceId,
            String tokenHash,
            Instant expiresAt,
            String clientIp,
            String userAgent
    ) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.tokenId = tokenId;
        entity.userId = userId;
        entity.workspaceId = workspaceId;
        entity.tokenHash = tokenHash;
        entity.expiresAt = expiresAt;
        entity.clientIp = clientIp;
        entity.userAgent = userAgent;
        return entity;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public void markUsed(Instant usedAt) {
        this.lastUsedAt = usedAt;
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
