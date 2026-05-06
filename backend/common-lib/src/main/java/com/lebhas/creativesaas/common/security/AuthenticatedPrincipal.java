package com.lebhas.creativesaas.common.security;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedPrincipal(
        UUID userId,
        UUID workspaceId,
        String email,
        Set<Role> roles
) {
}
