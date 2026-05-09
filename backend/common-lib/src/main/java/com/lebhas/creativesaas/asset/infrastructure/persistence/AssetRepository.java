package com.lebhas.creativesaas.asset.infrastructure.persistence;

import com.lebhas.creativesaas.asset.domain.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetEntity, UUID>, JpaSpecificationExecutor<AssetEntity> {

    Optional<AssetEntity> findByIdAndWorkspaceIdAndDeletedFalse(UUID id, UUID workspaceId);

    Optional<AssetEntity> findByIdAndDeletedFalse(UUID id);

    long countByWorkspaceIdAndFolderIdAndDeletedFalse(UUID workspaceId, UUID folderId);
}
