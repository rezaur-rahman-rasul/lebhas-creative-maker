package com.lebhas.creativesaas.identity.domain;

import com.lebhas.creativesaas.common.audit.TenantAwareEntity;
import com.lebhas.creativesaas.common.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "workspace_members",
        schema = "platform",
        uniqueConstraints = @UniqueConstraint(name = "uk_workspace_members_workspace_user", columnNames = {"workspace_id", "user_id"})
)
public class WorkspaceMembershipEntity extends TenantAwareEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkspaceMembershipStatus status;

    protected WorkspaceMembershipEntity() {
    }

    public static WorkspaceMembershipEntity create(UUID workspaceId, UUID userId, Role role, WorkspaceMembershipStatus status) {
        WorkspaceMembershipEntity membership = new WorkspaceMembershipEntity();
        membership.assignWorkspace(workspaceId);
        membership.userId = userId;
        membership.role = role;
        membership.status = status;
        return membership;
    }

    public UUID getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public WorkspaceMembershipStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == WorkspaceMembershipStatus.ACTIVE;
    }

    public void assignRole(Role role) {
        this.role = role;
    }

    public void changeStatus(WorkspaceMembershipStatus status) {
        this.status = status;
    }
}
