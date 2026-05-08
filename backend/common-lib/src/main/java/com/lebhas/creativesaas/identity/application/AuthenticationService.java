package com.lebhas.creativesaas.identity.application;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.common.security.SecurityAuditLogger;
import com.lebhas.creativesaas.common.security.authorization.RolePermissionRegistry;
import com.lebhas.creativesaas.common.security.context.CurrentUser;
import com.lebhas.creativesaas.common.security.context.CurrentUserContext;
import com.lebhas.creativesaas.common.security.jwt.IssuedAccessToken;
import com.lebhas.creativesaas.common.security.jwt.JwtAccessTokenService;
import com.lebhas.creativesaas.common.security.session.AccessTokenRevocationStore;
import com.lebhas.creativesaas.identity.application.dto.AuthSessionView;
import com.lebhas.creativesaas.identity.application.dto.LoginCommand;
import com.lebhas.creativesaas.identity.application.dto.LogoutCommand;
import com.lebhas.creativesaas.identity.application.dto.RefreshSessionCommand;
import com.lebhas.creativesaas.identity.application.dto.RegisterUserCommand;
import com.lebhas.creativesaas.identity.application.dto.UserView;
import com.lebhas.creativesaas.identity.domain.UserEntity;
import com.lebhas.creativesaas.identity.domain.UserStatus;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import com.lebhas.creativesaas.identity.infrastructure.persistence.UserRepository;
import com.lebhas.creativesaas.identity.infrastructure.persistence.WorkspaceMembershipRepository;
import com.lebhas.creativesaas.workspace.application.WorkspacePermissionPolicy;
import com.lebhas.creativesaas.workspace.application.WorkspaceProvisioningService;
import com.lebhas.creativesaas.workspace.domain.WorkspaceLanguage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final InvitationService invitationService;
    private final CurrentUserContext currentUserContext;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final IdentityViewMapper identityViewMapper;
    private final RolePermissionRegistry rolePermissionRegistry;
    private final WorkspacePermissionPolicy workspacePermissionPolicy;
    private final WorkspaceProvisioningService workspaceProvisioningService;
    private final AccessTokenRevocationStore accessTokenRevocationStore;
    private final SecurityAuditLogger securityAuditLogger;
    private final Clock clock;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            WorkspaceMembershipRepository workspaceMembershipRepository,
            PasswordEncoder passwordEncoder,
            JwtAccessTokenService jwtAccessTokenService,
            RefreshTokenService refreshTokenService,
            InvitationService invitationService,
            CurrentUserContext currentUserContext,
            WorkspaceAuthorizationService workspaceAuthorizationService,
            IdentityViewMapper identityViewMapper,
            RolePermissionRegistry rolePermissionRegistry,
            WorkspacePermissionPolicy workspacePermissionPolicy,
            WorkspaceProvisioningService workspaceProvisioningService,
            AccessTokenRevocationStore accessTokenRevocationStore,
            SecurityAuditLogger securityAuditLogger,
            Clock clock
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.workspaceMembershipRepository = workspaceMembershipRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAccessTokenService = jwtAccessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.invitationService = invitationService;
        this.currentUserContext = currentUserContext;
        this.workspaceAuthorizationService = workspaceAuthorizationService;
        this.identityViewMapper = identityViewMapper;
        this.rolePermissionRegistry = rolePermissionRegistry;
        this.workspacePermissionPolicy = workspacePermissionPolicy;
        this.workspaceProvisioningService = workspaceProvisioningService;
        this.accessTokenRevocationStore = accessTokenRevocationStore;
        this.securityAuditLogger = securityAuditLogger;
        this.clock = clock;
    }

    @Transactional
    public AuthSessionView register(RegisterUserCommand command, String clientIp, String userAgent) {
        String normalizedEmail = normalizeEmail(command.email());
        if (userRepository.existsByEmailIgnoreCaseAndDeletedFalse(normalizedEmail)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Role role = Role.ADMIN;
        UUID workspaceId = command.workspaceId();
        if (command.invitationToken() != null && !command.invitationToken().isBlank()) {
            InvitationService.PendingInvitation pendingInvitation = invitationService.resolvePendingInvitation(command.invitationToken(), normalizedEmail);
            workspaceId = pendingInvitation.invitation().getWorkspaceId();
            role = pendingInvitation.invitation().getRole();
            UserEntity invitedUser = createUser(command, normalizedEmail, role);
            userRepository.save(invitedUser);
            workspaceMembershipRepository.save(WorkspaceMembershipEntity.create(
                    workspaceId,
                    invitedUser.getId(),
                    role,
                    WorkspaceMembershipStatus.ACTIVE,
                    pendingInvitation.invitation().getPermissions(),
                    clock.instant(),
                    pendingInvitation.invitation().getInvitedByUserId()));
            invitedUser.markLastLogin(clock.instant());
            userRepository.save(invitedUser);
            invitationService.markAccepted(pendingInvitation.invitation());
            return issueSession(invitedUser, workspaceId, role, clientIp, userAgent);
        }

        UserEntity user = createUser(command, normalizedEmail, role);
        userRepository.save(user);
        WorkspaceProvisioningService.ProvisionedWorkspace provisionedWorkspace = workspaceProvisioningService.provisionOwnedWorkspace(
                user.getId(),
                new WorkspaceProvisioningService.WorkspaceSeed(
                        defaultWorkspaceName(command.firstName(), command.lastName()),
                        null,
                        null,
                        null,
                        null,
                        "Asia/Dhaka",
                        WorkspaceLanguage.ENGLISH,
                        "BDT",
                        "BD"));
        workspaceId = provisionedWorkspace.workspace().getId();
        user.markLastLogin(clock.instant());
        userRepository.save(user);
        return issueSession(user, workspaceId, role, clientIp, userAgent);
    }

    @Transactional
    public AuthSessionView login(LoginCommand command) {
        securityAuditLogger.logLoginAttempt(command.email(), command.workspaceId());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(command.email(), command.password()));
        } catch (DisabledException exception) {
            securityAuditLogger.logLoginFailure(command.email(), command.workspaceId(), "account_disabled");
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        } catch (BadCredentialsException exception) {
            securityAuditLogger.logLoginFailure(command.email(), command.workspaceId(), "bad_credentials");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (AuthenticationException exception) {
            securityAuditLogger.logLoginFailure(command.email(), command.workspaceId(), "authentication_failed");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        UserEntity user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(command.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!user.isActive()) {
            securityAuditLogger.logLoginFailure(command.email(), command.workspaceId(), "user_inactive");
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }
        Role effectiveRole;
        UUID workspaceId = command.workspaceId();
        if (user.getRole().isMaster()) {
            effectiveRole = Role.MASTER;
        } else {
            WorkspaceMembershipEntity membership = resolveLoginMembership(user.getId(), workspaceId);
            workspaceId = membership.getWorkspaceId();
            effectiveRole = membership.getRole();
        }
        user.markLastLogin(clock.instant());
        userRepository.save(user);
        securityAuditLogger.logLoginSuccess(user.getId(), workspaceId);
        return issueSession(user, workspaceId, effectiveRole, command.clientIp(), command.userAgent());
    }

    @Transactional
    public AuthSessionView refresh(RefreshSessionCommand command) {
        RefreshTokenService.ValidatedRefreshToken validatedRefreshToken = refreshTokenService.validate(command.refreshToken());
        UserEntity user = userRepository.findByIdAndDeletedFalse(validatedRefreshToken.refreshToken().getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.USER_INACTIVE);
        }
        UUID workspaceId = validatedRefreshToken.refreshToken().getWorkspaceId();
        Role effectiveRole = workspaceId == null
                ? user.getRole()
                : workspaceAuthorizationService.resolveEffectiveRole(user.getId(), user.getRole(), workspaceId);
        RefreshTokenService.IssuedRefreshToken rotatedToken = refreshTokenService.rotate(
                validatedRefreshToken,
                user,
                command.clientIp(),
                command.userAgent());
        IssuedAccessToken accessToken = jwtAccessTokenService.generate(user, workspaceId, effectiveRole);
        securityAuditLogger.logTokenRefresh(user.getId(), workspaceId);
        return new AuthSessionView(
                accessToken.token(),
                accessToken.expiresAt(),
                rotatedToken.token(),
                rotatedToken.expiresAt(),
                toUserView(user, workspaceId, effectiveRole));
    }

    @Transactional
    public void logout(LogoutCommand command) {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        if (command.refreshToken() != null && !command.refreshToken().isBlank()) {
            refreshTokenService.revokeSilently(command.refreshToken(), currentUser.userId());
        }
        if (currentUser.tokenId() != null && currentUser.accessTokenExpiresAt() != null) {
            accessTokenRevocationStore.revoke(currentUser.tokenId(), currentUser.accessTokenExpiresAt());
        }
        securityAuditLogger.logLogout(currentUser.userId(), currentUser.workspaceId());
    }

    @Transactional(readOnly = true)
    public UserView currentUser() {
        CurrentUser currentUser = currentUserContext.requireCurrentUser();
        UserEntity user = userRepository.findByIdAndDeletedFalse(currentUser.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        Role role = currentUser.roles().stream().findFirst().orElse(user.getRole());
        return identityViewMapper.toUserView(user, currentUser.workspaceId(), role, currentUser.permissions());
    }

    private UserEntity createUser(RegisterUserCommand command, String normalizedEmail, Role role) {
        return UserEntity.register(
                command.firstName(),
                command.lastName(),
                normalizedEmail,
                command.phone(),
                passwordEncoder.encode(command.password()),
                role,
                UserStatus.ACTIVE,
                false);
    }

    private WorkspaceMembershipEntity resolveLoginMembership(UUID userId, UUID requestedWorkspaceId) {
        List<WorkspaceMembershipEntity> activeMemberships = workspaceMembershipRepository
                .findAllByUserIdAndStatusAndDeletedFalse(userId, WorkspaceMembershipStatus.ACTIVE);
        if (requestedWorkspaceId != null) {
            return activeMemberships.stream()
                    .filter(membership -> membership.getWorkspaceId().equals(requestedWorkspaceId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED));
        }
        if (activeMemberships.isEmpty()) {
            throw new BusinessException(ErrorCode.WORKSPACE_ACCESS_DENIED);
        }
        if (activeMemberships.size() > 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "workspaceId is required when the user belongs to multiple workspaces");
        }
        return activeMemberships.getFirst();
    }

    private AuthSessionView issueSession(UserEntity user, UUID workspaceId, Role role, String clientIp, String userAgent) {
        IssuedAccessToken accessToken = jwtAccessTokenService.generate(user, workspaceId, role);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user, workspaceId, clientIp, userAgent);
        return new AuthSessionView(
                accessToken.token(),
                accessToken.expiresAt(),
                refreshToken.token(),
                refreshToken.expiresAt(),
                toUserView(user, workspaceId, role));
    }

    private UserView toUserView(UserEntity user, UUID workspaceId, Role role) {
        java.util.Set<Permission> permissions = workspaceId == null
                ? rolePermissionRegistry.resolve(Set.of(role))
                : workspaceAuthorizationService.resolveEffectivePermissions(user.getId(), role, workspaceId);
        return identityViewMapper.toUserView(
                user,
                workspaceId,
                role,
                permissions);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String defaultWorkspaceName(String firstName, String lastName) {
        String normalizedFirstName = firstName == null ? "" : firstName.trim();
        String normalizedLastName = lastName == null ? "" : lastName.trim();
        String fullName = (normalizedFirstName + " " + normalizedLastName).trim();
        return fullName.isBlank() ? "Workspace" : fullName + " Workspace";
    }
}
