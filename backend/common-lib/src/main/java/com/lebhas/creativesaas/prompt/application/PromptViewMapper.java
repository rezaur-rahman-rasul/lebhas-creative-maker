package com.lebhas.creativesaas.prompt.application;

import com.lebhas.creativesaas.prompt.application.dto.PromptHistoryView;
import com.lebhas.creativesaas.prompt.application.dto.PromptTemplateView;
import com.lebhas.creativesaas.prompt.domain.PromptHistoryEntity;
import com.lebhas.creativesaas.prompt.domain.PromptTemplateEntity;
import org.springframework.stereotype.Component;

@Component
public class PromptViewMapper {

    private final PromptJsonCodec promptJsonCodec;

    public PromptViewMapper(PromptJsonCodec promptJsonCodec) {
        this.promptJsonCodec = promptJsonCodec;
    }

    public PromptTemplateView toTemplateView(PromptTemplateEntity entity) {
        return new PromptTemplateView(
                entity.getId(),
                entity.getWorkspaceId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPlatform(),
                entity.getCampaignObjective(),
                entity.getBusinessType(),
                entity.getLanguage(),
                entity.getTemplateText(),
                entity.isSystemDefault(),
                entity.getStatus(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public PromptHistoryView toHistoryView(PromptHistoryEntity entity) {
        return new PromptHistoryView(
                entity.getId(),
                entity.getWorkspaceId(),
                entity.getUserId(),
                entity.getSourcePrompt(),
                entity.getEnhancedPrompt(),
                entity.getLanguage(),
                entity.getPlatform(),
                entity.getCampaignObjective(),
                entity.getBusinessType(),
                promptJsonCodec.readMapQuietly(entity.getBrandContextSnapshot()),
                entity.getSuggestionType(),
                entity.getAiProvider(),
                entity.getAiModel(),
                entity.getTokenUsage(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
