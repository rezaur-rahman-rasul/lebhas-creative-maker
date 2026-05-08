package com.lebhas.creativesaas.workspace.application;

import com.lebhas.creativesaas.common.security.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class WorkspaceActivityLogger {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceActivityLogger.class);

    public void logWorkspaceCreated(UUID workspaceId, UUID ownerId, String slug) {
        log.info("workspace_event type=workspace_created workspaceId={} ownerId={} slug={}", workspaceId, ownerId, slug);
    }

    public void logWorkspaceUpdated(UUID workspaceId, UUID actorUserId, String slug, String status) {
        log.info("workspace_event type=workspace_updated workspaceId={} actorUserId={} slug={} status={}",
                workspaceId, actorUserId, slug, status);
    }

    public void logBrandProfileUpdated(UUID workspaceId, UUID actorUserId) {
        log.info("workspace_event type=brand_profile_updated workspaceId={} actorUserId={}", workspaceId, actorUserId);
    }

    public void logSettingsUpdated(UUID workspaceId, UUID actorUserId) {
        log.info("workspace_event type=workspace_settings_updated workspaceId={} actorUserId={}", workspaceId, actorUserId);
    }

    public void logCrewInvited(UUID workspaceId, UUID actorUserId, String email, Set<Permission> permissions) {
        log.info("workspace_event type=crew_invited workspaceId={} actorUserId={} email={} permissions={}",
                workspaceId, actorUserId, email, permissions);
    }

    public void logCrewUpdated(UUID workspaceId, UUID actorUserId, UUID crewUserId, Set<Permission> permissions, String status) {
        log.info("workspace_event type=crew_updated workspaceId={} actorUserId={} crewUserId={} permissions={} status={}",
                workspaceId, actorUserId, crewUserId, permissions, status);
    }

    public void logCrewRemoved(UUID workspaceId, UUID actorUserId, UUID crewUserId) {
        log.info("workspace_event type=crew_removed workspaceId={} actorUserId={} crewUserId={}",
                workspaceId, actorUserId, crewUserId);
    }

    public void logAuthorizationFailure(UUID workspaceId, UUID actorUserId, String reason) {
        log.warn("workspace_event type=authorization_failure workspaceId={} actorUserId={} reason={}",
                workspaceId, actorUserId, reason);
    }
}
