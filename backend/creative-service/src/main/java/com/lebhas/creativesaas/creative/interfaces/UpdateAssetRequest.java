package com.lebhas.creativesaas.creative.interfaces;

import com.lebhas.creativesaas.asset.domain.AssetCategory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UpdateAssetRequest(
        UUID folderId,
        AssetCategory assetCategory,
        Set<String> tags,
        Map<String, Object> metadata
) {
}
