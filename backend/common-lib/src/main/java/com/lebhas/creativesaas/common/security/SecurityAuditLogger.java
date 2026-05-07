package com.lebhas.creativesaas.common.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditLogger.class);

    public void logLoginAttempt(String email, UUID workspaceId) {
        log.info("auth_event type=login_attempt email={} workspaceId={}", email, workspaceId);
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

    public void logSecurityException(String type, String path, String reason) {
        log.warn("auth_event type={} path={} reason={}", type, path, reason);
    }
}
