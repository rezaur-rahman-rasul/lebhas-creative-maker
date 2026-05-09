package com.lebhas.creativesaas.prompt.infrastructure.persistence;

import com.lebhas.creativesaas.prompt.domain.PromptHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PromptHistoryRepository extends JpaRepository<PromptHistoryEntity, UUID>, JpaSpecificationExecutor<PromptHistoryEntity> {

    Optional<PromptHistoryEntity> findByIdAndWorkspaceIdAndDeletedFalse(UUID id, UUID workspaceId);
}
