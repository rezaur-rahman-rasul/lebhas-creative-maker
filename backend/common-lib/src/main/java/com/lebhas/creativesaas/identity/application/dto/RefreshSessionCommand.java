package com.lebhas.creativesaas.identity.application.dto;

public record RefreshSessionCommand(
        String refreshToken,
        String clientIp,
        String userAgent
) {
}
