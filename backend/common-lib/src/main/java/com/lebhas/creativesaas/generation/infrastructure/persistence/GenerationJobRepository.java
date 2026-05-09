package com.lebhas.creativesaas.generation.infrastructure.persistence;

import com.lebhas.creativesaas.generation.domain.GenerationJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GenerationJobRepository extends JpaRepository<GenerationJobEntity, UUID> {

    Optional<GenerationJobEntity> findByIdAndDeletedFalse(UUID id);

    Optional<GenerationJobEntity> findFirstByRequestIdAndWorkspaceIdAndDeletedFalseOrderByCreatedAtDesc(UUID requestId, UUID workspaceId);
}
