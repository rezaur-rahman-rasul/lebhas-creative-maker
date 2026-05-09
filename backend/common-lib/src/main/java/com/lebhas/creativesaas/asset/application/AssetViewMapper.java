package com.lebhas.creativesaas.asset.application;

import com.lebhas.creativesaas.asset.application.dto.AssetFolderView;
import com.lebhas.creativesaas.asset.application.dto.AssetView;
import com.lebhas.creativesaas.asset.domain.AssetEntity;
import com.lebhas.creativesaas.asset.domain.AssetFolderEntity;
import org.springframework.stereotype.Component;

@Component
public class AssetViewMapper {

    private final AssetMetadataSerializer assetMetadataSerializer;

    public AssetViewMapper(AssetMetadataSerializer assetMetadataSerializer) {
        this.assetMetadataSerializer = assetMetadataSerializer;
    }

    public AssetView toAssetView(AssetEntity asset) {
        return new AssetView(
                asset.getId(),
                asset.getWorkspaceId(),
                asset.getUploadedBy(),
                asset.getFolderId(),
                asset.getOriginalFileName(),
                asset.getStoredFileName(),
                asset.getFileType(),
                asset.getMimeType(),
                asset.getFileExtension(),
                asset.getFileSize(),
                asset.getStorageProvider(),
                asset.getStorageBucket(),
                asset.getStorageKey(),
                asset.getPublicUrl(),
                asset.getPreviewUrl(),
                asset.getThumbnailUrl(),
                asset.getAssetCategory(),
                asset.getStatus(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getDuration(),
                asset.getTags(),
                assetMetadataSerializer.deserialize(asset.getMetadataJson()),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }

    public AssetFolderView toFolderView(AssetFolderEntity folder) {
        return new AssetFolderView(
                folder.getId(),
                folder.getWorkspaceId(),
                folder.getName(),
                folder.getParentFolderId(),
                folder.getDescription(),
                folder.getCreatedBy(),
                folder.getCreatedAt(),
                folder.getUpdatedAt());
    }
}
