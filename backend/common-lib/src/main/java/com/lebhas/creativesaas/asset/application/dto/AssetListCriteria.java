package com.lebhas.creativesaas.asset.application.dto;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import com.lebhas.creativesaas.asset.domain.AssetFileType;
import com.lebhas.creativesaas.asset.domain.AssetStatus;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.UUID;

public record AssetListCriteria(
        UUID workspaceId,
        AssetCategory assetCategory,
        AssetFileType fileType,
        UUID folderId,
        String tag,
        UUID uploadedBy,
        AssetStatus status,
        String keyword,
        Instant createdFrom,
        Instant createdTo,
        int page,
        int size,
        String sortBy,
        Sort.Direction sortDirection
) {
}
