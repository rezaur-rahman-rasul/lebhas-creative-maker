package com.lebhas.creativesaas.asset.storage;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import com.lebhas.creativesaas.asset.domain.AssetEntity;
import com.lebhas.creativesaas.asset.domain.StorageProvider;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

public interface StorageService {

    StorageProvider provider();

    StoredObject store(StorageUploadRequest request);

    SignedAssetUrl generatePreviewUrl(AssetEntity asset);

    SignedAssetUrl generateDownloadUrl(AssetEntity asset);

    void delete(AssetEntity asset);

    StoredObjectMetadata getMetadata(AssetEntity asset);

    record StorageUploadRequest(
            UUID workspaceId,
            UUID assetId,
            AssetCategory assetCategory,
            String storedFileName,
            String mimeType,
            MultipartFile file
    ) {
    }

    record StoredObject(
            String storedFileName,
            String bucket,
            String storageKey,
            String publicUrl,
            String previewUrl,
            String thumbnailUrl
    ) {
    }

    record SignedAssetUrl(String url, Instant expiresAt) {
    }

    record StoredObjectMetadata(long contentLength, Instant lastModified) {
    }
}
