package com.lebhas.creativesaas.identity.application;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.jwt.JwtProperties;
import com.lebhas.creativesaas.common.security.session.OpaqueTokenService;
import com.lebhas.creativesaas.identity.domain.RefreshTokenEntity;
import com.lebhas.creativesaas.identity.domain.UserEntity;
import com.lebhas.creativesaas.identity.infrastructure.persistence.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final OpaqueTokenService opaqueTokenService;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            OpaqueTokenService opaqueTokenService,
            JwtProperties jwtProperties,
            Clock clock
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.opaqueTokenService = opaqueTokenService;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional
    public IssuedRefreshToken issue(UserEntity user, UUID workspaceId, String clientIp, String userAgent) {
        OpaqueTokenService.IssuedOpaqueToken token = opaqueTokenService.issue();
        Instant expiresAt = clock.instant().plus(jwtProperties.getRefreshTokenTtl());
        refreshTokenRepository.save(RefreshTokenEntity.issue(
                token.tokenId(),
                user.getId(),
                workspaceId,
                token.hashedSecret(),
                expiresAt,
                clientIp,
                userAgent));
        return new IssuedRefreshToken(token.value(), expiresAt, workspaceId);
    }

    @Transactional(readOnly = true)
    public ValidatedRefreshToken validate(String rawToken, String clientIp, String userAgent) {
        OpaqueTokenService.ParsedOpaqueToken parsedToken = opaqueTokenService.parse(rawToken, ErrorCode.REFRESH_TOKEN_INVALID);
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenIdAndDeletedFalse(parsedToken.tokenId())
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
        if (!opaqueTokenService.matches(refreshToken.getTokenHash(), parsedToken.hashedSecret())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }
        Instant now = clock.instant();
        if (refreshToken.isRevoked()) {
            throw new BusinessException(ErrorCode.TOKEN_REVOKED);
        }
        if (refreshToken.isExpired(now)) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        if (refreshToken.getClientIp() != null
                && clientIp != null
                && !refreshToken.getClientIp().isBlank()
                && !refreshToken.getClientIp().equals(clientIp.trim())) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh token context does not match the original client");
        }
        if (refreshToken.getUserAgent() != null
                && userAgent != null
                && !refreshToken.getUserAgent().isBlank()
                && !refreshToken.getUserAgent().equals(userAgent)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID, "Refresh token context does not match the original client");
        }
        return new ValidatedRefreshToken(refreshToken);
    }

    @Transactional
    public IssuedRefreshToken rotate(ValidatedRefreshToken validatedRefreshToken, UserEntity user, String clientIp, String userAgent) {
        Instant now = clock.instant();
        validatedRefreshToken.refreshToken().markUsed(now);
        validatedRefreshToken.refreshToken().revoke(now);
        refreshTokenRepository.save(validatedRefreshToken.refreshToken());
        return issue(user, validatedRefreshToken.refreshToken().getWorkspaceId(), clientIp, userAgent);
    }

    @Transactional
    public void revokeSilently(String rawToken, UUID expectedUserId) {
        try {
            ValidatedRefreshToken validatedRefreshToken = validate(rawToken, null, null);
            if (!validatedRefreshToken.refreshToken().getUserId().equals(expectedUserId)) {
                return;
            }
            validatedRefreshToken.refreshToken().revoke(clock.instant());
            refreshTokenRepository.save(validatedRefreshToken.refreshToken());
        } catch (BusinessException ignored) {
        }
    }

    public record IssuedRefreshToken(String token, Instant expiresAt, UUID workspaceId) {
    }

    public record ValidatedRefreshToken(RefreshTokenEntity refreshToken) {
    }
}
