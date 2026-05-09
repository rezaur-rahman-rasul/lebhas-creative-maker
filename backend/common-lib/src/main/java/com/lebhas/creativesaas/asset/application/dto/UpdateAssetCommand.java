package com.lebhas.creativesaas.asset.application.dto;

import com.lebhas.creativesaas.asset.domain.AssetCategory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UpdateAssetCommand(
        UUID workspaceId,
        UUID assetId,
        UUID folderId,
        AssetCategory assetCategory,
        Set<String> tags,
        Map<String, Object> metadata
) {
}
