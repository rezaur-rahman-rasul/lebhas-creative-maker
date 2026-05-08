package com.lebhas.creativesaas.common.security.authorization;

import com.lebhas.creativesaas.common.security.AuthenticatedPrincipal;
import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import com.lebhas.creativesaas.identity.infrastructure.persistence.WorkspaceMembershipRepository;
import com.lebhas.creativesaas.workspace.application.WorkspacePermissionPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class WorkspaceGrantedAuthorityResolver {

    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WorkspacePermissionPolicy workspacePermissionPolicy;
    private final RolePermissionRegistry rolePermissionRegistry;

    public WorkspaceGrantedAuthorityResolver(
            WorkspaceMembershipRepository workspaceMembershipRepository,
            WorkspacePermissionPolicy workspacePermissionPolicy,
            RolePermissionRegistry rolePermissionRegistry
    ) {
        this.workspaceMembershipRepository = workspaceMembershipRepository;
        this.workspacePermissionPolicy = workspacePermissionPolicy;
        this.rolePermissionRegistry = rolePermissionRegistry;
    }

    public Set<Permission> resolve(AuthenticatedPrincipal principal) {
        Role effectiveRole = principal.roles().stream().findFirst().orElse(null);
        if (effectiveRole == null) {
            return Set.of();
        }
        if (effectiveRole.isMaster() || principal.workspaceId() == null) {
            return rolePermissionRegistry.resolve(effectiveRole);
        }
        return workspaceMembershipRepository.findByUserIdAndWorkspaceIdAndDeletedFalse(principal.userId(), principal.workspaceId())
                .filter(WorkspaceMembershipEntity::isActive)
                .map(membership -> workspacePermissionPolicy.resolveEffectivePermissions(membership.getRole(), membership.getPermissions()))
                .orElse(Set.of());
    }

    public Set<Permission> resolve(Role role, UUID workspaceId, Set<Permission> storedPermissions) {
        if (role.isMaster() || workspaceId == null) {
            return rolePermissionRegistry.resolve(role);
        }
        return workspacePermissionPolicy.resolveEffectivePermissions(role, storedPermissions);
    }

    public boolean hasActiveMembership(UUID userId, UUID workspaceId) {
        return workspaceMembershipRepository.existsByUserIdAndWorkspaceIdAndStatusAndDeletedFalse(
                userId,
                workspaceId,
                WorkspaceMembershipStatus.ACTIVE);
    }
}
