package com.lebhas.creativesaas.asset.application.dto;

import java.time.Instant;

public record AssetUrlView(
        String url,
        Instant expiresAt
) {
}
