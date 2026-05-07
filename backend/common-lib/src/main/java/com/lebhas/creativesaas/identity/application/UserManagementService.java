package com.lebhas.creativesaas.identity.application;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.common.security.authorization.RolePermissionRegistry;
import com.lebhas.creativesaas.common.security.context.CurrentUser;
import com.lebhas.creativesaas.common.security.context.CurrentUserContext;
import com.lebhas.creativesaas.common.tenant.TenantContext;
import com.lebhas.creativesaas.identity.application.dto.UpdateUserCommand;
import com.lebhas.creativesaas.identity.application.dto.UpdateUserStatusCommand;
import com.lebhas.creativesaas.identity.application.dto.UserView;
import com.lebhas.creativesaas.identity.domain.UserEntity;
import com.lebhas.creativesaas.identity.domain.UserStatus;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import com.lebhas.creativesaas.identity.infrastructure.persistence.UserRepository;
import com.lebhas.creativesaas.identity.infrastructure.persistence.WorkspaceMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final CurrentUserContext currentUserContext;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final IdentityViewMapper identityViewMapper;
    private final RolePermissionRegistry rolePermissionRegistry;

    public UserManagementService(
            UserRepository userRepository,
            WorkspaceMembershipRepository workspaceMembershipRepository,
            CurrentUserContext currentUserContext,
            WorkspaceAuthorizationService workspaceAuthorizationService,
            IdentityViewMapper identityViewMapper,
            RolePermissionRegistry rolePermissionRegistry
    ) {
        this.userRepository = userRepository;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
        this.currentUserContext = currentUserContext;
        this.workspaceAuthorizationService = workspaceAuthorizationService;
        this.identityViewMapper = identityViewMapper;
        this.rolePermissionRegistry = rolePermissionRegistry;
    }

    @Transactional(readOnly = true)
    public List<UserView> listUsers(UserStatus status) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        if (currentUser.isMaster() && TenantContext.getWorkspaceId().isEmpty()) {
            return userRepository.findAll().stream()
                    .filter(user -> !user.isDeleted())
                    .filter(user -> status == null || user.getStatus() == status)
                    .map(user -> identityViewMapper.toUserView(user, null, user.getRole(), rolePermissionRegistry.resolve(Set.of(user.getRole()))))
                    .toList();
        }
        UUID workspaceId = workspaceAuthorizationService.requireWorkspaceAccess(TenantContext.getWorkspaceId().orElse(null));
        return userRepository.findWorkspaceUsers(workspaceId, status).stream()
                .map(user -> identityViewMapper.toUserView(user, workspaceId, resolveUserRole(user, workspaceId), rolePermissionRegistry.resolve(Set.of(resolveUserRole(user, workspaceId)))))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserView getUser(UUID userId) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        if (currentUser.isMaster() && TenantContext.getWorkspaceId().isEmpty()) {
            UserEntity user = userRepository.findByIdAndDeletedFalse(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
            return identityViewMapper.toUserView(user, null, user.getRole(), rolePermissionRegistry.resolve(Set.of(user.getRole())));
        }
        UUID workspaceId = workspaceAuthorizationService.requireWorkspaceAccess(TenantContext.getWorkspaceId().orElse(null));
        UserEntity user = userRepository.findWorkspaceUserById(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        Role role = resolveUserRole(user, workspaceId);
        return identityViewMapper.toUserView(user, workspaceId, role, rolePermissionRegistry.resolve(Set.of(role)));
    }

    @Transactional
    public UserView updateUser(UpdateUserCommand command) {
        UserEntity user = loadEditableUser(command.userId());
        ensureUniqueEmail(user, command.email());
        Role newRole = command.role() == null ? user.getRole() : command.role();
        guardRoleMutation(newRole, user);
        user.updateProfile(command.firstName(), command.lastName(), command.email(), command.phone());
        user.assignRole(newRole);
        UUID workspaceId = TenantContext.getWorkspaceId().orElse(null);
        if (workspaceId != null && !newRole.isMaster()) {
            WorkspaceMembershipEntity membership = workspaceAuthorizationService.requireActiveMembership(user.getId(), workspaceId);
            membership.assignRole(newRole);
            workspaceMembershipRepository.save(membership);
        }
        userRepository.save(user);
        return buildUserView(user, workspaceId);
    }

    @Transactional
    public UserView changeStatus(UpdateUserStatusCommand command) {
        UserEntity user = loadEditableUser(command.userId());
        guardMasterMutation(user);
        user.changeStatus(command.status());
        userRepository.save(user);
        UUID workspaceId = TenantContext.getWorkspaceId().orElse(null);
        if (workspaceId != null && !user.getRole().isMaster()) {
            workspaceMembershipRepository.findByUserIdAndWorkspaceIdAndDeletedFalse(user.getId(), workspaceId)
                    .ifPresent(membership -> {
                        membership.changeStatus(command.status() == UserStatus.ACTIVE
                                ? WorkspaceMembershipStatus.ACTIVE
                                : WorkspaceMembershipStatus.SUSPENDED);
                        workspaceMembershipRepository.save(membership);
                    });
        }
        return buildUserView(user, workspaceId);
    }

    private UserEntity loadEditableUser(UUID userId) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        if (currentUser.isMaster() && TenantContext.getWorkspaceId().isEmpty()) {
            return userRepository.findByIdAndDeletedFalse(userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        }
        UUID workspaceId = workspaceAuthorizationService.requireWorkspaceAccess(TenantContext.getWorkspaceId().orElse(null));
        UserEntity user = userRepository.findWorkspaceUserById(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        guardMasterMutation(user);
        return user;
    }

    private void ensureUniqueEmail(UserEntity existingUser, String email) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        userRepository.findByEmailIgnoreCaseAndDeletedFalse(normalized)
                .filter(user -> !user.getId().equals(existingUser.getId()))
                .ifPresent(user -> {
                    throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
                });
    }

    private void guardRoleMutation(Role targetRole, UserEntity targetUser) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        if (targetRole == Role.MASTER && !currentUser.isMaster()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Only master users can assign the master role");
        }
        if (targetUser.getRole().isMaster() && !currentUser.isMaster()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Master users can only be managed by another master user");
        }
    }

    private void guardMasterMutation(UserEntity targetUser) {
        if (targetUser.getRole().isMaster() && !currentUserContext.requireCurrentUser().isMaster()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Master users can only be managed by another master user");
        }
    }

    private UserView buildUserView(UserEntity user, UUID workspaceId) {
        Role role = workspaceId == null ? user.getRole() : resolveUserRole(user, workspaceId);
        return identityViewMapper.toUserView(user, workspaceId, role, rolePermissionRegistry.resolve(Set.of(role)));
    }

    private Role resolveUserRole(UserEntity user, UUID workspaceId) {
        if (user.getRole().isMaster() || workspaceId == null) {
            return user.getRole();
        }
        return workspaceAuthorizationService.requireActiveMembership(user.getId(), workspaceId).getRole();
    }
}
