package com.lebhas.creativesaas.asset.application;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AssetActivityLogger {

    private static final Logger log = LoggerFactory.getLogger(AssetActivityLogger.class);

    public void logAssetUploaded(UUID workspaceId, UUID assetId, UUID actorUserId, AssetCategory category, String storageKey) {
        log.info("asset_event type=asset_uploaded workspaceId={} assetId={} actorUserId={} category={} storageKey={}",
                workspaceId, assetId, actorUserId, category, storageKey);
    }

    public void logAssetUpdated(UUID workspaceId, UUID assetId, UUID actorUserId) {
        log.info("asset_event type=asset_updated workspaceId={} assetId={} actorUserId={}",
                workspaceId, assetId, actorUserId);
    }

    public void logAssetDeleted(UUID workspaceId, UUID assetId, UUID actorUserId) {
        log.info("asset_event type=asset_deleted workspaceId={} assetId={} actorUserId={}",
                workspaceId, assetId, actorUserId);
    }

    public void logFolderCreated(UUID workspaceId, UUID folderId, UUID actorUserId, String name) {
        log.info("asset_event type=asset_folder_created workspaceId={} folderId={} actorUserId={} name={}",
                workspaceId, folderId, actorUserId, name);
    }

    public void logFolderUpdated(UUID workspaceId, UUID folderId, UUID actorUserId, String name) {
        log.info("asset_event type=asset_folder_updated workspaceId={} folderId={} actorUserId={} name={}",
                workspaceId, folderId, actorUserId, name);
    }

    public void logFolderDeleted(UUID workspaceId, UUID folderId, UUID actorUserId) {
        log.info("asset_event type=asset_folder_deleted workspaceId={} folderId={} actorUserId={}",
                workspaceId, folderId, actorUserId);
    }

    public void logSignedUrlGenerated(UUID workspaceId, UUID assetId, UUID actorUserId, String mode) {
        log.info("asset_event type=signed_url_generated workspaceId={} assetId={} actorUserId={} mode={}",
                workspaceId, assetId, actorUserId, mode);
    }

    public void logValidationFailure(UUID workspaceId, UUID actorUserId, String reason) {
        log.warn("asset_event type=validation_failure workspaceId={} actorUserId={} reason={}",
                workspaceId, actorUserId, reason);
    }
}
