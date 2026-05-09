package com.lebhas.creativesaas.asset.domain;

import com.lebhas.creativesaas.common.audit.TenantAwareEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "assets", schema = "platform")
public class AssetEntity extends TenantAwareEntity {

    @Column(name = "uploaded_by", nullable = false, updatable = false)
    private UUID uploadedBy;

    @Column(name = "folder_id")
    private UUID folderId;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", length = 255)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 30)
    private AssetFileType fileType;

    @Column(name = "mime_type", length = 120)
    private String mimeType;

    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    @Column(name = "file_size")
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_provider", nullable = false, length = 30)
    private StorageProvider storageProvider;

    @Column(name = "storage_bucket", length = 120)
    private String storageBucket;

    @Column(name = "storage_key", length = 600)
    private String storageKey;

    @Column(name = "public_url", length = 1000)
    private String publicUrl;

    @Column(name = "preview_url", length = 1000)
    private String previewUrl;

    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_category", nullable = false, length = 40)
    private AssetCategory assetCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AssetStatus status;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "duration")
    private Long duration;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "asset_tags",
            schema = "platform",
            joinColumns = @JoinColumn(name = "asset_id", nullable = false)
    )
    @Column(name = "tag", nullable = false, length = 80)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "metadata")
    private String metadataJson;

    protected AssetEntity() {
    }

    public static AssetEntity createPending(
            UUID workspaceId,
            UUID uploadedBy,
            UUID folderId,
            String originalFileName,
            AssetCategory assetCategory,
            Set<String> tags,
            String metadataJson
    ) {
        AssetEntity asset = new AssetEntity();
        asset.assignWorkspace(workspaceId);
        asset.uploadedBy = uploadedBy;
        asset.folderId = folderId;
        asset.originalFileName = originalFileName;
        asset.assetCategory = assetCategory;
        asset.tags = new LinkedHashSet<>(tags == null ? Set.of() : tags);
        asset.metadataJson = metadataJson;
        asset.status = AssetStatus.PROCESSING;
        asset.storageProvider = StorageProvider.LOCAL;
        return asset;
    }

    public UUID getUploadedBy() {
        return uploadedBy;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public AssetFileType getFileType() {
        return fileType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public long getFileSize() {
        return fileSize;
    }

    public StorageProvider getStorageProvider() {
        return storageProvider;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public AssetCategory getAssetCategory() {
        return assetCategory;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Long getDuration() {
        return duration;
    }

    public Set<String> getTags() {
        return Set.copyOf(tags);
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void completeUpload(
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
            Integer width,
            Integer height,
            Long duration
    ) {
        this.storedFileName = storedFileName;
        this.fileType = fileType;
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.storageProvider = storageProvider;
        this.storageBucket = storageBucket;
        this.storageKey = storageKey;
        this.publicUrl = publicUrl;
        this.previewUrl = previewUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.width = width;
        this.height = height;
        this.duration = duration;
        this.status = AssetStatus.ACTIVE;
    }

    public void updateDetails(UUID folderId, AssetCategory assetCategory, Set<String> tags, String metadataJson) {
        this.folderId = folderId;
        this.assetCategory = assetCategory;
        this.tags.clear();
        this.tags.addAll(tags == null ? Set.of() : tags);
        this.metadataJson = metadataJson;
    }

    public void markUploadFailed() {
        this.status = AssetStatus.FAILED;
    }

    public void markDeletedAsset() {
        this.status = AssetStatus.DELETED;
        markDeleted();
    }
}
