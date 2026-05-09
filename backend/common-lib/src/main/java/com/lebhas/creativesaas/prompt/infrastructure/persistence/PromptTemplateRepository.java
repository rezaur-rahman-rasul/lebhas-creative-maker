package com.lebhas.creativesaas.prompt.infrastructure.persistence;

import com.lebhas.creativesaas.prompt.domain.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, UUID>, JpaSpecificationExecutor<PromptTemplateEntity> {

    Optional<PromptTemplateEntity> findByIdAndDeletedFalse(UUID id);
}
