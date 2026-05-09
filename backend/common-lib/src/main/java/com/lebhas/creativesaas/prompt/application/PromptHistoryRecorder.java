package com.lebhas.creativesaas.prompt.application;

import com.lebhas.creativesaas.prompt.domain.CampaignObjective;
import com.lebhas.creativesaas.prompt.domain.PromptHistoryEntity;
import com.lebhas.creativesaas.prompt.domain.PromptLanguage;
import com.lebhas.creativesaas.prompt.domain.PromptPlatform;
import com.lebhas.creativesaas.prompt.domain.SuggestionType;
import com.lebhas.creativesaas.prompt.infrastructure.persistence.PromptHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PromptHistoryRecorder {

    private final PromptHistoryRepository promptHistoryRepository;

    public PromptHistoryRecorder(PromptHistoryRepository promptHistoryRepository) {
        this.promptHistoryRepository = promptHistoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(
            UUID workspaceId,
            UUID userId,
            String sourcePrompt,
            String outputPayload,
            PromptLanguage language,
            PromptPlatform platform,
            CampaignObjective campaignObjective,
            String businessType,
            String brandContextSnapshot,
            SuggestionType suggestionType,
            String aiProvider,
            String aiModel,
            Integer tokenUsage
    ) {
        promptHistoryRepository.save(PromptHistoryEntity.success(
                workspaceId,
                userId,
                sourcePrompt,
                outputPayload,
                language,
                platform,
                campaignObjective,
                businessType,
                brandContextSnapshot,
                suggestionType,
                aiProvider,
                aiModel,
                tokenUsage));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(
            UUID workspaceId,
            UUID userId,
            String sourcePrompt,
            PromptLanguage language,
            PromptPlatform platform,
            CampaignObjective campaignObjective,
            String businessType,
            String brandContextSnapshot,
            SuggestionType suggestionType,
            String aiProvider,
            String aiModel
    ) {
        promptHistoryRepository.save(PromptHistoryEntity.failure(
                workspaceId,
                userId,
                sourcePrompt,
                language,
                platform,
                campaignObjective,
                businessType,
                brandContextSnapshot,
                suggestionType,
                aiProvider,
                aiModel));
    }
}
