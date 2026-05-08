package com.lebhas.creativesaas.identity.application;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.common.security.authorization.RolePermissionRegistry;
import com.lebhas.creativesaas.common.security.context.CurrentUser;
import com.lebhas.creativesaas.common.security.context.CurrentUserContext;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import com.lebhas.creativesaas.identity.infrastructure.persistence.WorkspaceMembershipRepository;
import com.lebhas.creativesaas.workspace.application.WorkspaceActivityLogger;
import com.lebhas.creativesaas.workspace.application.WorkspacePermissionPolicy;
import com.lebhas.creativesaas.workspace.domain.WorkspaceEntity;
import com.lebhas.creativesaas.workspace.infrastructure.persistence.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class WorkspaceAuthorizationService {

    private final CurrentUserContext currentUserContext;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspacePermissionPolicy workspacePermissionPolicy;
    private final WorkspaceActivityLogger workspaceActivityLogger;
    private final RolePermissionRegistry rolePermissionRegistry;

    public WorkspaceAuthorizationService(
            CurrentUserContext currentUserContext,
            WorkspaceMembershipRepository workspaceMembershipRepository,
            WorkspaceRepository workspaceRepository,
            WorkspacePermissionPolicy workspacePermissionPolicy,
            WorkspaceActivityLogger workspaceActivityLogger,
            RolePermissionRegistry rolePermissionRegistry
    ) {
        this.currentUserContext = currentUserContext;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspacePermissionPolicy = workspacePermissionPolicy;
        this.workspaceActivityLogger = workspaceActivityLogger;
        this.rolePermissionRegistry = rolePermissionRegistry;
    }

    @Transactional(readOnly = true)
    public UUID requireWorkspaceAccess(UUID requestedWorkspaceId) {
        return requireWorkspaceContext(requestedWorkspaceId).workspace().getId();
    }

    @Transactional(readOnly = true)
    public WorkspaceMembershipEntity requireActiveMembership(UUID userId, UUID workspaceId) {
        return workspaceMembershipRepository.findByUserIdAndWorkspaceIdAndDeletedFalse(userId, workspaceId)
                .filter(WorkspaceMembershipEntity::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }

    @Transactional(readOnly = true)
    public WorkspaceAccess requireWorkspaceContext(UUID requestedWorkspaceId) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        UUID effectiveWorkspaceId = requestedWorkspaceId == null ? currentUserContext.requireWorkspaceId() : requestedWorkspaceId;
        WorkspaceEntity workspace = workspaceRepository.findByIdAndDeletedFalse(effectiveWorkspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));
        if (currentUser.isMaster()) {
            return new WorkspaceAccess(workspace, currentUser, null, Role.MASTER, currentUser.permissions());
        }
        if (currentUser.workspaceId() != null && !effectiveWorkspaceId.equals(currentUser.workspaceId())) {
            deny(effectiveWorkspaceId, currentUser.userId(), "workspace_mismatch");
        }
        WorkspaceMembershipEntity membership = requireActiveMembership(currentUser.userId(), effectiveWorkspaceId);
        Set<Permission> permissions = workspacePermissionPolicy.resolveEffectivePermissions(membership.getRole(), membership.getPermissions());
        return new WorkspaceAccess(workspace, currentUser, membership, membership.getRole(), permissions);
    }

    @Transactional(readOnly = true)
    public WorkspaceAccess requirePermission(UUID requestedWorkspaceId, Permission permission) {
        WorkspaceAccess access = requireWorkspaceContext(requestedWorkspaceId);
        if (access.effectiveRole().isMaster() || access.permissions().contains(permission)) {
            return access;
        }
        deny(access.workspace().getId(), access.currentUser().userId(), "missing_permission_" + permission.name());
        return access;
    }

    @Transactional(readOnly = true)
    public WorkspaceAccess requireWorkspaceOwnerOrMaster(UUID requestedWorkspaceId) {
        WorkspaceAccess access = requireWorkspaceContext(requestedWorkspaceId);
        if (access.currentUser().isMaster() || access.workspace().getOwnerId().equals(access.currentUser().userId())) {
            return access;
        }
        workspaceActivityLogger.logAuthorizationFailure(access.workspace().getId(), access.currentUser().userId(), "workspace_owner_required");
        throw new BusinessException(ErrorCode.WORKSPACE_OWNER_REQUIRED);
    }

    public Role resolveEffectiveRole(UUID userId, Role fallbackRole, UUID workspaceId) {
        if (fallbackRole.isMaster()) {
            return Role.MASTER;
        }
        return requireActiveMembership(userId, workspaceId).getRole();
    }

    public Set<Permission> resolveEffectivePermissions(UUID userId, Role fallbackRole, UUID workspaceId) {
        if (fallbackRole.isMaster()) {
            return rolePermissionRegistry.resolve(Role.MASTER);
        }
        WorkspaceMembershipEntity membership = requireActiveMembership(userId, workspaceId);
        return workspacePermissionPolicy.resolveEffectivePermissions(membership.getRole(), membership.getPermissions());
    }

    private void deny(UUID workspaceId, UUID userId, String reason) {
        workspaceActivityLogger.logAuthorizationFailure(workspaceId, userId, reason);
        throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
    }

    public record WorkspaceAccess(
            WorkspaceEntity workspace,
            CurrentUser currentUser,
            WorkspaceMembershipEntity membership,
            Role effectiveRole,
            Set<Permission> permissions
    ) {
    }
}
