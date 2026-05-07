package com.lebhas.creativesaas.identity.application;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.common.security.context.CurrentUser;
import com.lebhas.creativesaas.common.security.context.CurrentUserContext;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import com.lebhas.creativesaas.identity.infrastructure.persistence.WorkspaceMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WorkspaceAuthorizationService {

    private final CurrentUserContext currentUserContext;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;

    public WorkspaceAuthorizationService(
            CurrentUserContext currentUserContext,
            WorkspaceMembershipRepository workspaceMembershipRepository
    ) {
        this.currentUserContext = currentUserContext;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
    }

    @Transactional(readOnly = true)
    public UUID requireWorkspaceAccess(UUID requestedWorkspaceId) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        if (currentUser.isMaster()) {
            return requestedWorkspaceId == null ? currentUserContext.requireWorkspaceId() : requestedWorkspaceId;
        }
        UUID effectiveWorkspaceId = requestedWorkspaceId == null ? currentUserContext.requireWorkspaceId() : requestedWorkspaceId;
        if (currentUser.workspaceId() != null && !effectiveWorkspaceId.equals(currentUser.workspaceId())) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
        if (!workspaceMembershipRepository.existsByUserIdAndWorkspaceIdAndStatusAndDeletedFalse(
                currentUser.userId(),
                effectiveWorkspaceId,
                WorkspaceMembershipStatus.ACTIVE)) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
        return effectiveWorkspaceId;
    }

    @Transactional(readOnly = true)
    public WorkspaceMembershipEntity requireActiveMembership(UUID userId, UUID workspaceId) {
        return workspaceMembershipRepository.findByUserIdAndWorkspaceIdAndDeletedFalse(userId, workspaceId)
                .filter(WorkspaceMembershipEntity::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }

    public Role resolveEffectiveRole(UUID userId, Role fallbackRole, UUID workspaceId) {
        if (fallbackRole.isMaster()) {
            return Role.MASTER;
        }
        return requireActiveMembership(userId, workspaceId).getRole();
    }
}
