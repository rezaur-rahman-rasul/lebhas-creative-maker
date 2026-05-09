package com.lebhas.creativesaas.creative.interfaces;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class UploadAssetRequest {

    @NotNull
    private MultipartFile file;

    @NotNull
    private AssetCategory assetCategory;

    private UUID folderId;

    private String tags;

    private String metadata;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public AssetCategory getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(AssetCategory assetCategory) {
        this.assetCategory = assetCategory;
    }

    public UUID getFolderId() {
        return folderId;
    }

    public void setFolderId(UUID folderId) {
        this.folderId = folderId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
