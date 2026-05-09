package com.lebhas.creativesaas.creative.interfaces;

import com.lebhas.creativesaas.asset.application.AssetManagementService;
import com.lebhas.creativesaas.asset.application.AssetMetadataSerializer;
import com.lebhas.creativesaas.asset.application.dto.AssetListCriteria;
import com.lebhas.creativesaas.asset.application.dto.AssetUrlView;
import com.lebhas.creativesaas.asset.application.dto.AssetView;
import com.lebhas.creativesaas.asset.application.dto.UpdateAssetCommand;
import com.lebhas.creativesaas.asset.application.dto.UploadAssetCommand;
import com.lebhas.creativesaas.common.api.ApiResponse;
import com.lebhas.creativesaas.common.api.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/assets")
@Tag(name = "Assets")
@SecurityRequirement(name = "bearerAuth")
public class AssetController {

    private final AssetManagementService assetManagementService;
    private final AssetMetadataSerializer assetMetadataSerializer;

    public AssetController(
            AssetManagementService assetManagementService,
            AssetMetadataSerializer assetMetadataSerializer
    ) {
        this.assetManagementService = assetManagementService;
        this.assetMetadataSerializer = assetMetadataSerializer;
    }

    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ASSET_UPLOAD')")
    @Operation(summary = "Upload an asset", description = "Supported image/logo formats: JPG, JPEG, PNG, WebP, SVG. Supported video formats: MP4, MOV. Limits: 10MB images, 5MB logos, 200MB videos.")
    public ApiResponse<AssetView> uploadAsset(
            @PathVariable UUID workspaceId,
            @Valid
            @ModelAttribute
            @RequestBody(content = @Content(mediaType = "multipart/form-data", schema = @Schema(implementation = UploadAssetRequest.class)))
            UploadAssetRequest request
    ) {
        return ApiResponse.success(assetManagementService.uploadAsset(new UploadAssetCommand(
                workspaceId,
                request.getFolderId(),
                request.getAssetCategory(),
                parseTags(request.getTags()),
                parseMetadata(request.getMetadata()),
                request.getFile())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "List workspace assets")
    public ApiResponse<PagedResult<AssetView>> listAssets(
            @PathVariable UUID workspaceId,
            @Valid @ModelAttribute AssetListRequest request
    ) {
        return ApiResponse.success(assetManagementService.listAssets(new AssetListCriteria(
                workspaceId,
                request.getAssetCategory(),
                request.getFileType(),
                request.getFolderId(),
                request.getTag(),
                request.getUploadedBy(),
                request.getStatus(),
                request.getSearch(),
                request.getCreatedFrom(),
                request.getCreatedTo(),
                request.getPage() == null ? 0 : request.getPage(),
                request.getSize() == null ? 20 : request.getSize(),
                request.getSortBy(),
                request.getDirection())));
    }

    @GetMapping("/{assetId}")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Get a workspace asset")
    public ApiResponse<AssetView> getAsset(@PathVariable UUID workspaceId, @PathVariable UUID assetId) {
        return ApiResponse.success(assetManagementService.getAsset(workspaceId, assetId));
    }

    @PutMapping("/{assetId}")
    @PreAuthorize("hasAuthority('ASSET_UPDATE')")
    @Operation(summary = "Update asset metadata, category, folder, or tags")
    public ApiResponse<AssetView> updateAsset(
            @PathVariable UUID workspaceId,
            @PathVariable UUID assetId,
            @org.springframework.web.bind.annotation.RequestBody UpdateAssetRequest request
    ) {
        return ApiResponse.success(assetManagementService.updateAsset(new UpdateAssetCommand(
                workspaceId,
                assetId,
                request.folderId(),
                request.assetCategory(),
                request.tags(),
                request.metadata())));
    }

    @DeleteMapping("/{assetId}")
    @PreAuthorize("hasAuthority('ASSET_DELETE')")
    @Operation(summary = "Soft delete an asset")
    public ApiResponse<Void> deleteAsset(@PathVariable UUID workspaceId, @PathVariable UUID assetId) {
        assetManagementService.deleteAsset(workspaceId, assetId);
        return ApiResponse.success("Asset deleted", null);
    }

    @GetMapping("/{assetId}/preview-url")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Generate a signed preview URL for an asset")
    public ApiResponse<AssetUrlView> previewUrl(@PathVariable UUID workspaceId, @PathVariable UUID assetId) {
        return ApiResponse.success(assetManagementService.generatePreviewUrl(workspaceId, assetId));
    }

    @GetMapping("/{assetId}/download-url")
    @PreAuthorize("hasAuthority('ASSET_VIEW')")
    @Operation(summary = "Generate a signed download URL for an asset")
    public ApiResponse<AssetUrlView> downloadUrl(@PathVariable UUID workspaceId, @PathVariable UUID assetId) {
        return ApiResponse.success(assetManagementService.generateDownloadUrl(workspaceId, assetId));
    }

    private Set<String> parseTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private Map<String, Object> parseMetadata(String metadata) {
        return assetMetadataSerializer.deserialize(metadata);
    }
}
