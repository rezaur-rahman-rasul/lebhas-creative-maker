package com.lebhas.creativesaas.asset.application.dto;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import com.lebhas.creativesaas.asset.domain.AssetFileType;
import com.lebhas.creativesaas.asset.domain.AssetStatus;
import com.lebhas.creativesaas.asset.domain.StorageProvider;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record AssetView(
        UUID id,
        UUID workspaceId,
        UUID uploadedBy,
        UUID folderId,
        String originalFileName,
        String storedFileName,
        AssetFileType fileType,
        String mimeType,
        String fileExtension,
        long fileSize,
        StorageProvider storageProvider,
        String storageBucket,
        String storageKey,
        String publicUrl,
        String previewUrl,
        String thumbnailUrl,
        AssetCategory assetCategory,
        AssetStatus status,
        Integer width,
        Integer height,
        Long duration,
        Set<String> tags,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant updatedAt
) {
}
