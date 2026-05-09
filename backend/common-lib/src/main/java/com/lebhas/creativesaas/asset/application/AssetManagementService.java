package com.lebhas.creativesaas.asset.application;

import com.lebhas.creativesaas.asset.application.dto.AssetListCriteria;
import com.lebhas.creativesaas.asset.application.dto.AssetUrlView;
import com.lebhas.creativesaas.asset.application.dto.AssetView;
import com.lebhas.creativesaas.asset.application.dto.UpdateAssetCommand;
import com.lebhas.creativesaas.asset.application.dto.UploadAssetCommand;
import com.lebhas.creativesaas.asset.domain.AssetEntity;
import com.lebhas.creativesaas.asset.domain.AssetStatus;
import com.lebhas.creativesaas.asset.infrastructure.persistence.AssetRepository;
import com.lebhas.creativesaas.asset.infrastructure.persistence.AssetSpecifications;
import com.lebhas.creativesaas.asset.storage.StorageService;
import com.lebhas.creativesaas.common.api.PagedResult;
import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.identity.application.WorkspaceAuthorizationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AssetManagementService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final AssetRepository assetRepository;
    private final AssetFolderService assetFolderService;
    private final AssetFileValidationService assetFileValidationService;
    private final AssetMetadataSerializer assetMetadataSerializer;
    private final AssetViewMapper assetViewMapper;
    private final AssetActivityLogger assetActivityLogger;
    private final StorageService storageService;

    public AssetManagementService(
            WorkspaceAuthorizationService workspaceAuthorizationService,
            AssetRepository assetRepository,
            AssetFolderService assetFolderService,
            AssetFileValidationService assetFileValidationService,
            AssetMetadataSerializer assetMetadataSerializer,
            AssetViewMapper assetViewMapper,
            AssetActivityLogger assetActivityLogger,
            StorageService storageService
    ) {
        this.workspaceAuthorizationService = workspaceAuthorizationService;
        this.assetRepository = assetRepository;
        this.assetFolderService = assetFolderService;
        this.assetFileValidationService = assetFileValidationService;
        this.assetMetadataSerializer = assetMetadataSerializer;
        this.assetViewMapper = assetViewMapper;
        this.assetActivityLogger = assetActivityLogger;
        this.storageService = storageService;
    }

    @Transactional
    public AssetView uploadAsset(UploadAssetCommand command) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(command.workspaceId(), Permission.ASSET_UPLOAD);
        UUID folderId = requireFolderId(command.workspaceId(), command.folderId());
        Set<String> tags = normalizeTags(command.tags());
        AssetFileValidationService.ValidatedAssetFile validatedFile = validateUpload(command, access.currentUser().userId());
        AssetEntity asset = AssetEntity.createPending(
                command.workspaceId(),
                access.currentUser().userId(),
                folderId,
                validatedFile.originalFileName(),
                command.assetCategory(),
                tags,
                assetMetadataSerializer.serialize(command.metadata()));
        assetRepository.saveAndFlush(asset);

        StorageService.StoredObject storedObject = storageService.store(new StorageService.StorageUploadRequest(
                asset.getWorkspaceId(),
                asset.getId(),
                asset.getAssetCategory(),
                validatedFile.sanitizedFileName(),
                validatedFile.mimeType(),
                command.file()));

        asset.completeUpload(
                storedObject.storedFileName(),
                validatedFile.fileType(),
                validatedFile.mimeType(),
                validatedFile.extension(),
                validatedFile.size(),
                storageService.provider(),
                storedObject.bucket(),
                storedObject.storageKey(),
                storedObject.publicUrl(),
                storedObject.previewUrl(),
                storedObject.thumbnailUrl(),
                validatedFile.width(),
                validatedFile.height(),
                validatedFile.duration());
        assetRepository.save(asset);
        assetActivityLogger.logAssetUploaded(
                asset.getWorkspaceId(),
                asset.getId(),
                access.currentUser().userId(),
                asset.getAssetCategory(),
                asset.getStorageKey());
        return assetViewMapper.toAssetView(asset);
    }

    @Transactional(readOnly = true)
    public PagedResult<AssetView> listAssets(AssetListCriteria criteria) {
        workspaceAuthorizationService.requirePermission(criteria.workspaceId(), Permission.ASSET_VIEW);
        Pageable pageable = PageRequest.of(
                Math.max(criteria.page(), 0),
                Math.min(criteria.size() <= 0 ? DEFAULT_PAGE_SIZE : criteria.size(), MAX_PAGE_SIZE),
                Sort.by(criteria.sortDirection() == null ? Sort.Direction.DESC : criteria.sortDirection(), resolveSortBy(criteria.sortBy())));
        return PagedResult.from(assetRepository.findAll(AssetSpecifications.forList(criteria), pageable).map(assetViewMapper::toAssetView));
    }

    @Transactional(readOnly = true)
    public AssetView getAsset(UUID workspaceId, UUID assetId) {
        workspaceAuthorizationService.requirePermission(workspaceId, Permission.ASSET_VIEW);
        return assetViewMapper.toAssetView(requireAsset(workspaceId, assetId));
    }

    @Transactional
    public AssetView updateAsset(UpdateAssetCommand command) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(command.workspaceId(), Permission.ASSET_UPDATE);
        AssetEntity asset = requireAsset(command.workspaceId(), command.assetId());
        UUID folderId = requireFolderId(command.workspaceId(), command.folderId());
        asset.updateDetails(
                folderId,
                command.assetCategory() == null ? asset.getAssetCategory() : command.assetCategory(),
                normalizeTags(command.tags()),
                assetMetadataSerializer.serialize(command.metadata() == null ? Map.of() : command.metadata()));
        assetRepository.save(asset);
        assetActivityLogger.logAssetUpdated(command.workspaceId(), asset.getId(), access.currentUser().userId());
        return assetViewMapper.toAssetView(asset);
    }

    @Transactional
    public void deleteAsset(UUID workspaceId, UUID assetId) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(workspaceId, Permission.ASSET_DELETE);
        AssetEntity asset = requireAsset(workspaceId, assetId);
        storageService.delete(asset);
        asset.markDeletedAsset();
        assetRepository.save(asset);
        assetActivityLogger.logAssetDeleted(workspaceId, assetId, access.currentUser().userId());
    }

    @Transactional(readOnly = true)
    public AssetUrlView generatePreviewUrl(UUID workspaceId, UUID assetId) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(workspaceId, Permission.ASSET_VIEW);
        AssetEntity asset = requireAsset(workspaceId, assetId);
        StorageService.SignedAssetUrl signedUrl = storageService.generatePreviewUrl(asset);
        assetActivityLogger.logSignedUrlGenerated(workspaceId, assetId, access.currentUser().userId(), "preview");
        return new AssetUrlView(signedUrl.url(), signedUrl.expiresAt());
    }

    @Transactional(readOnly = true)
    public AssetUrlView generateDownloadUrl(UUID workspaceId, UUID assetId) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(workspaceId, Permission.ASSET_VIEW);
        AssetEntity asset = requireAsset(workspaceId, assetId);
        StorageService.SignedAssetUrl signedUrl = storageService.generateDownloadUrl(asset);
        assetActivityLogger.logSignedUrlGenerated(workspaceId, assetId, access.currentUser().userId(), "download");
        return new AssetUrlView(signedUrl.url(), signedUrl.expiresAt());
    }

    @Transactional(readOnly = true)
    public AssetEntity requireAssetForSignedAccess(UUID assetId) {
        AssetEntity asset = assetRepository.findByIdAndDeletedFalse(assetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSET_NOT_FOUND));
        if (asset.getStatus() != AssetStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ASSET_NOT_FOUND);
        }
        return asset;
    }

    @Transactional(readOnly = true)
    public AssetEntity requireAsset(UUID workspaceId, UUID assetId) {
        return assetRepository.findByIdAndWorkspaceIdAndDeletedFalse(assetId, workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ASSET_NOT_FOUND));
    }

    private AssetFileValidationService.ValidatedAssetFile validateUpload(
            UploadAssetCommand command,
            UUID actorUserId
    ) {
        try {
            return assetFileValidationService.validate(command.file(), command.assetCategory());
        } catch (BusinessException exception) {
            assetActivityLogger.logValidationFailure(command.workspaceId(), actorUserId, exception.getMessage());
            throw exception;
        }
    }

    private UUID requireFolderId(UUID workspaceId, UUID folderId) {
        if (folderId == null) {
            return null;
        }
        return assetFolderService.requireFolder(workspaceId, folderId).getId();
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return Set.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String tag : tags) {
            if (!StringUtils.hasText(tag)) {
                continue;
            }
            String value = tag.trim().toLowerCase();
            if (value.length() > 80) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Asset tags must be 80 characters or fewer");
            }
            normalized.add(value);
        }
        return Set.copyOf(normalized);
    }

    private String resolveSortBy(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "createdAt";
        }
        return switch (sortBy.trim()) {
            case "fileSize" -> "fileSize";
            case "originalFileName" -> "originalFileName";
            case "updatedAt" -> "updatedAt";
            default -> "createdAt";
        };
    }
}
