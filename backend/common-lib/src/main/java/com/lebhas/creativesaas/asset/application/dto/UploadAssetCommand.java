package com.lebhas.creativesaas.asset.application.dto;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record UploadAssetCommand(
        UUID workspaceId,
        UUID folderId,
        AssetCategory assetCategory,
        Set<String> tags,
        Map<String, Object> metadata,
        MultipartFile file
) {
}
