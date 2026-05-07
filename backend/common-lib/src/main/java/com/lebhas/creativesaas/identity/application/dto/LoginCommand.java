package com.lebhas.creativesaas.identity.application.dto;

import java.util.UUID;

public record LoginCommand(
        String email,
        String password,
        UUID workspaceId,
        String clientIp,
        String userAgent
) {
}
