package com.lebhas.creativesaas.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogger.class);

    public void logLoginAttempt(String email, UUID workspaceId, String clientIp) {
        log.info("auth_event type=login_attempt email={} workspaceId={} clientIp={}", email, workspaceId, clientIp);
    }

    public void logLoginSuccess(UUID userId, UUID workspaceId) {
        log.info("auth_event type=login_success userId={} workspaceId={}", userId, workspaceId);
    }

    public void logLoginFailure(String email, UUID workspaceId, String reason) {
        log.warn("auth_event type=login_failure email={} workspaceId={} reason={}", email, workspaceId, reason);
    }

    public void logTokenRefresh(UUID userId, UUID workspaceId) {
        log.info("auth_event type=token_refresh userId={} workspaceId={}", userId, workspaceId);
    }

    public void logLogout(UUID userId, UUID workspaceId) {
        log.info("auth_event type=logout userId={} workspaceId={}", userId, workspaceId);
    }

    public void logRateLimitTriggered(String flow, String subject, String clientIp, String scope, long attempts) {
        log.warn(
                "auth_event type=rate_limit_triggered flow={} subject={} clientIp={} scope={} attempts={}",
                flow,
                subject,
                clientIp,
                scope,
                attempts);
    }

    public void logAccountLocked(UUID userId, String email, Instant lockedUntil) {
        log.warn("auth_event type=account_locked userId={} email={} lockedUntil={}", userId, email, lockedUntil);
    }

    public void logSecurityException(String type, String path, String reason) {
        log.warn("auth_event type={} path={} reason={}", type, path, reason);
    }
}
