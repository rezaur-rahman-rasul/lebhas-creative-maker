package com.lebhas.creativesaas.prompt.application;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.identity.application.WorkspaceAuthorizationService;
import com.lebhas.creativesaas.prompt.application.dto.PromptEnhancementCommand;
import com.lebhas.creativesaas.prompt.application.dto.PromptEnhancementView;
import com.lebhas.creativesaas.prompt.application.dto.PromptRewriteCommand;
import com.lebhas.creativesaas.prompt.application.dto.PromptRewriteView;
import com.lebhas.creativesaas.prompt.application.dto.PromptSuggestionCommand;
import com.lebhas.creativesaas.prompt.application.dto.PromptSuggestionListView;
import com.lebhas.creativesaas.prompt.application.dto.PromptSuggestionsView;
import com.lebhas.creativesaas.prompt.domain.SuggestionType;
import com.lebhas.creativesaas.prompt.provider.AiProviderRouter;
import com.lebhas.creativesaas.prompt.provider.AiResponse;
import com.lebhas.creativesaas.prompt.rate.PromptThrottleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class PromptIntelligenceService {

    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final PromptThrottleService promptThrottleService;
    private final PromptContextAssembler promptContextAssembler;
    private final PromptInstructionFactory promptInstructionFactory;
    private final AiProviderRouter aiProviderRouter;
    private final PromptResponseParser promptResponseParser;
    private final PromptHistoryRecorder promptHistoryRecorder;
    private final PromptActivityLogger promptActivityLogger;
    private final PromptJsonCodec promptJsonCodec;

    public PromptIntelligenceService(
            WorkspaceAuthorizationService workspaceAuthorizationService,
            PromptThrottleService promptThrottleService,
            PromptContextAssembler promptContextAssembler,
            PromptInstructionFactory promptInstructionFactory,
            AiProviderRouter aiProviderRouter,
            PromptResponseParser promptResponseParser,
            PromptHistoryRecorder promptHistoryRecorder,
            PromptActivityLogger promptActivityLogger,
            PromptJsonCodec promptJsonCodec
    ) {
        this.workspaceAuthorizationService = workspaceAuthorizationService;
        this.promptThrottleService = promptThrottleService;
        this.promptContextAssembler = promptContextAssembler;
        this.promptInstructionFactory = promptInstructionFactory;
        this.aiProviderRouter = aiProviderRouter;
        this.promptResponseParser = promptResponseParser;
        this.promptHistoryRecorder = promptHistoryRecorder;
        this.promptActivityLogger = promptActivityLogger;
        this.promptJsonCodec = promptJsonCodec;
    }

    @Transactional
    public PromptEnhancementView enhance(PromptEnhancementCommand command) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(command.workspaceId(), Permission.PROMPT_INTELLIGENCE_USE);
        validatePromptText(command.customPrompt(), "customPrompt");
        enforceTemplateUsagePermission(access, command.templateId(), "enhance");
        promptThrottleService.assertAllowed(command.workspaceId(), access.currentUser().userId(), command.clientIp(), "enhance");

        PromptContextAssembler.ResolvedPromptContext context = promptContextAssembler.assemble(
                command.workspaceId(),
                command.templateId(),
                command.useBrandProfile(),
                command.assetIds());
        promptActivityLogger.logPromptRequest(
                "enhance",
                command.workspaceId(),
                access.currentUser().userId(),
                command.platform(),
                command.campaignObjective(),
                command.language(),
                command.customPrompt().trim().length(),
                context.assets().size(),
                command.useBrandProfile(),
                Set.of(SuggestionType.ENHANCEMENT),
                command.customPrompt());

        String sourcePrompt = command.customPrompt().trim();
        try {
            AiResponse aiResponse = aiProviderRouter.generate(promptInstructionFactory.buildEnhancementRequest(command, context));
            PromptEnhancementView result = promptResponseParser.parseEnhancement(
                    aiResponse.content(),
                    aiResponse.provider(),
                    aiResponse.model(),
                    aiResponse.tokenUsage());
            promptHistoryRecorder.recordSuccess(
                    command.workspaceId(),
                    access.currentUser().userId(),
                    sourcePrompt,
                    promptJsonCodec.write(result, ErrorCode.PROMPT_CONTEXT_INVALID, "Prompt enhancement result could not be persisted"),
                    command.language(),
                    command.platform(),
                    command.campaignObjective(),
                    command.businessType(),
                    context.brandContextSnapshotJson(),
                    SuggestionType.ENHANCEMENT,
                    aiResponse.provider(),
                    aiResponse.model(),
                    aiResponse.tokenUsage());
            promptActivityLogger.logPromptCompleted("enhance", command.workspaceId(), access.currentUser().userId(), aiResponse.provider(), aiResponse.model(), aiResponse.tokenUsage());
            return result;
        } catch (RuntimeException exception) {
            recordFailureQuietly(
                    command.workspaceId(),
                    access.currentUser().userId(),
                    sourcePrompt,
                    command.language(),
                    command.platform(),
                    command.campaignObjective(),
                    command.businessType(),
                    context.brandContextSnapshotJson(),
                    SuggestionType.ENHANCEMENT);
            promptActivityLogger.logProviderFailure("enhance", command.workspaceId(), access.currentUser().userId(), aiProviderRouter.activeProviderName(), exception.getMessage());
            throw exception;
        }
    }

    @Transactional
    public PromptRewriteView rewrite(PromptRewriteCommand command) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(command.workspaceId(), Permission.PROMPT_INTELLIGENCE_USE);
        validatePromptText(command.existingPrompt(), "existingPrompt");
        enforceTemplateUsagePermission(access, command.templateId(), "rewrite");
        promptThrottleService.assertAllowed(command.workspaceId(), access.currentUser().userId(), command.clientIp(), "rewrite");

        PromptContextAssembler.ResolvedPromptContext context = promptContextAssembler.assemble(
                command.workspaceId(),
                command.templateId(),
                command.useBrandProfile(),
                command.assetIds());
        promptActivityLogger.logPromptRequest(
                "rewrite",
                command.workspaceId(),
                access.currentUser().userId(),
                command.platform(),
                command.campaignObjective(),
                command.language(),
                command.existingPrompt().trim().length(),
                context.assets().size(),
                command.useBrandProfile(),
                Set.of(SuggestionType.REWRITE),
                command.existingPrompt());

        String sourcePrompt = command.existingPrompt().trim();
        try {
            AiResponse aiResponse = aiProviderRouter.generate(promptInstructionFactory.buildRewriteRequest(command, context));
            PromptRewriteView result = promptResponseParser.parseRewrite(
                    aiResponse.content(),
                    aiResponse.provider(),
                    aiResponse.model(),
                    aiResponse.tokenUsage());
            promptHistoryRecorder.recordSuccess(
                    command.workspaceId(),
                    access.currentUser().userId(),
                    sourcePrompt,
                    promptJsonCodec.write(result, ErrorCode.PROMPT_CONTEXT_INVALID, "Prompt rewrite result could not be persisted"),
                    command.language(),
                    command.platform(),
                    command.campaignObjective(),
                    command.businessType(),
                    context.brandContextSnapshotJson(),
                    SuggestionType.REWRITE,
                    aiResponse.provider(),
                    aiResponse.model(),
                    aiResponse.tokenUsage());
            promptActivityLogger.logPromptCompleted("rewrite", command.workspaceId(), access.currentUser().userId(), aiResponse.provider(), aiResponse.model(), aiResponse.tokenUsage());
            return result;
        } catch (RuntimeException exception) {
            recordFailureQuietly(
                    command.workspaceId(),
                    access.currentUser().userId(),
                    sourcePrompt,
                    command.language(),
                    command.platform(),
                    command.campaignObjective(),
                    command.businessType(),
                    context.brandContextSnapshotJson(),
                    SuggestionType.REWRITE);
            promptActivityLogger.logProviderFailure("rewrite", command.workspaceId(), access.currentUser().userId(), aiProviderRouter.activeProviderName(), exception.getMessage());
            throw exception;
        }
    }

    @Transactional
    public PromptSuggestionsView generateSuggestions(PromptSuggestionCommand command) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService
                .requirePermission(command.workspaceId(), Permission.PROMPT_INTELLIGENCE_USE);
        validateSuggestionContext(command);
        enforceTemplateUsagePermission(access, command.templateId(), "suggestions");
        promptThrottleService.assertAllowed(command.workspaceId(), access.currentUser().userId(), command.clientIp(), "suggestions");

        PromptContextAssembler.ResolvedPromptContext context = promptContextAssembler.assemble(
                command.workspaceId(),
                command.templateId(),
                command.useBrandProfile(),
                command.assetIds());
        String sourcePrompt = resolveSuggestionSource(command);
        Set<SuggestionType> suggestionTypes = normalizeSuggestionTypes(command.suggestionTypes());
        promptActivityLogger.logPromptRequest(
                "suggestions",
                command.workspaceId(),
                access.currentUser().userId(),
                command.platform(),
                command.campaignObjective(),
                command.language(),
                sourcePrompt.length(),
                context.assets().size(),
                command.useBrandProfile(),
                suggestionTypes,
                sourcePrompt);

        try {
            AiResponse aiResponse = aiProviderRouter.generate(promptInstructionFactory.buildSuggestionRequest(
                    new PromptSuggestionCommand(
                            command.workspaceId(),
                            sourcePrompt,
                            command.assetIds(),
                            command.templateId(),
                            command.businessType(),
                            command.campaignObjective(),
                            command.platform(),
                            command.creativeStyle(),
                            command.language(),
                            command.tone(),
                            command.targetAudience(),
                            command.offerDetails(),
                            command.ctaPreference(),
                            command.useBrandProfile(),
                            suggestionTypes,
                            command.clientIp()),
                    context));
            PromptSuggestionsView result = promptResponseParser.parseSuggestions(
                    aiResponse.content(),
                    aiResponse.provider(),
                    aiResponse.model(),
                    aiResponse.tokenUsage());
            promptHistoryRecorder.recordSuccess(
                    command.workspaceId(),
                    access.currentUser().userId(),
                    sourcePrompt,
                    promptJsonCodec.write(result, ErrorCode.PROMPT_CONTEXT_INVALID, "Prompt suggestions result could not be persisted"),
                    command.language(),
                    command.platform(),
                    command.campaignObjective(),
                    command.businessType(),
                    context.brandContextSnapshotJson(),
                    suggestionTypes.size() == 1 ? suggestionTypes.iterator().next() : SuggestionType.GENERAL_SUGGESTIONS,
                    aiResponse.provider(),
                    aiResponse.model(),
                    aiResponse.tokenUsage());
            promptActivityLogger.logPromptCompleted("suggestions", command.workspaceId(), access.currentUser().userId(), aiResponse.provider(), aiResponse.model(), aiResponse.tokenUsage());
            return result;
        } catch (RuntimeException exception) {
            recordFailureQuietly(
                    command.workspaceId(),
                    access.currentUser().userId(),
                    sourcePrompt,
                    command.language(),
                    command.platform(),
                    command.campaignObjective(),
                    command.businessType(),
                    context.brandContextSnapshotJson(),
                    suggestionTypes.size() == 1 ? suggestionTypes.iterator().next() : SuggestionType.GENERAL_SUGGESTIONS);
            promptActivityLogger.logProviderFailure("suggestions", command.workspaceId(), access.currentUser().userId(), aiProviderRouter.activeProviderName(), exception.getMessage());
            throw exception;
        }
    }

    @Transactional
    public PromptSuggestionListView generateSuggestionList(PromptSuggestionCommand command, SuggestionType suggestionType) {
        PromptSuggestionsView suggestions = generateSuggestions(new PromptSuggestionCommand(
                command.workspaceId(),
                command.customPrompt(),
                command.assetIds(),
                command.templateId(),
                command.businessType(),
                command.campaignObjective(),
                command.platform(),
                command.creativeStyle(),
                command.language(),
                command.tone(),
                command.targetAudience(),
                command.offerDetails(),
                command.ctaPreference(),
                command.useBrandProfile(),
                Set.of(suggestionType),
                command.clientIp()));
        return new PromptSuggestionListView(
                suggestionType,
                switch (suggestionType) {
                    case CTA_SUGGESTIONS -> suggestions.ctaSuggestions();
                    case HEADLINE_SUGGESTIONS -> suggestions.headlineSuggestions();
                    case OFFER_SUGGESTIONS -> suggestions.offerSuggestions();
                    case CREATIVE_ANGLE_SUGGESTIONS -> suggestions.creativeAngleSuggestions();
                    case CAMPAIGN_TONE_SUGGESTIONS -> suggestions.campaignToneSuggestions();
                    case BUSINESS_CATEGORY_SUGGESTIONS -> suggestions.businessCategorySuggestions();
                    default -> List.of();
                },
                suggestions.reasoningSummary(),
                suggestions.aiProvider(),
                suggestions.aiModel(),
                suggestions.tokenUsage());
    }

    private void validatePromptText(String prompt, String fieldName) {
        if (!StringUtils.hasText(prompt)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, fieldName + " must be provided");
        }
        int length = prompt.trim().length();
        if (length < 5 || length > 5000) {
            throw new BusinessException(ErrorCode.PROMPT_LENGTH_INVALID, fieldName + " must be between 5 and 5000 characters");
        }
    }

    private void validateSuggestionContext(PromptSuggestionCommand command) {
        if (StringUtils.hasText(command.customPrompt())) {
            validatePromptText(command.customPrompt(), "customPrompt");
            return;
        }
        boolean hasContext = StringUtils.hasText(command.businessType())
                || StringUtils.hasText(command.targetAudience())
                || StringUtils.hasText(command.offerDetails())
                || StringUtils.hasText(command.ctaPreference())
                || command.useBrandProfile()
                || (command.assetIds() != null && !command.assetIds().isEmpty())
                || command.templateId() != null
                || command.platform() != null
                || command.campaignObjective() != null;
        if (!hasContext) {
            throw new BusinessException(ErrorCode.PROMPT_UNSUPPORTED_COMBINATION, "Suggestions require a prompt or at least one meaningful campaign context input");
        }
    }

    private void enforceTemplateUsagePermission(WorkspaceAuthorizationService.WorkspaceAccess access, UUID templateId, String operation) {
        if (templateId == null) {
            return;
        }
        if (access.effectiveRole().isMaster()
                || access.permissions().contains(Permission.PROMPT_TEMPLATE_VIEW)
                || access.permissions().contains(Permission.PROMPT_TEMPLATE_MANAGE)) {
            return;
        }
        promptActivityLogger.logAuthorizationFailure(operation, access.workspace().getId(), access.currentUser().userId(), "missing_prompt_template_view_permission");
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    private Set<SuggestionType> normalizeSuggestionTypes(Set<SuggestionType> suggestionTypes) {
        if (suggestionTypes == null || suggestionTypes.isEmpty()) {
            return EnumSet.of(
                    SuggestionType.CTA_SUGGESTIONS,
                    SuggestionType.HEADLINE_SUGGESTIONS,
                    SuggestionType.OFFER_SUGGESTIONS,
                    SuggestionType.CREATIVE_ANGLE_SUGGESTIONS,
                    SuggestionType.CAMPAIGN_TONE_SUGGESTIONS,
                    SuggestionType.BUSINESS_CATEGORY_SUGGESTIONS);
        }
        EnumSet<SuggestionType> normalized = EnumSet.copyOf(suggestionTypes);
        normalized.remove(SuggestionType.ENHANCEMENT);
        normalized.remove(SuggestionType.REWRITE);
        normalized.remove(SuggestionType.GENERAL_SUGGESTIONS);
        if (normalized.isEmpty()) {
            throw new BusinessException(ErrorCode.PROMPT_UNSUPPORTED_COMBINATION, "Suggestion request must include at least one supported suggestion category");
        }
        return Set.copyOf(normalized);
    }

    private String resolveSuggestionSource(PromptSuggestionCommand command) {
        if (StringUtils.hasText(command.customPrompt())) {
            return command.customPrompt().trim();
        }
        StringBuilder builder = new StringBuilder("Prompt suggestion context");
        append(builder, "businessType", command.businessType());
        append(builder, "campaignObjective", command.campaignObjective() == null ? null : command.campaignObjective().name());
        append(builder, "platform", command.platform() == null ? null : command.platform().name());
        append(builder, "creativeStyle", command.creativeStyle() == null ? null : command.creativeStyle().name());
        append(builder, "language", command.language() == null ? null : command.language().name());
        append(builder, "tone", command.tone() == null ? null : command.tone().name());
        append(builder, "targetAudience", command.targetAudience());
        append(builder, "offerDetails", command.offerDetails());
        append(builder, "ctaPreference", command.ctaPreference());
        return builder.toString();
    }

    private void append(StringBuilder builder, String label, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        builder.append(" | ").append(label).append('=').append(value.trim());
    }

    private void recordFailureQuietly(
            UUID workspaceId,
            UUID userId,
            String sourcePrompt,
            com.lebhas.creativesaas.prompt.domain.PromptLanguage language,
            com.lebhas.creativesaas.prompt.domain.PromptPlatform platform,
            com.lebhas.creativesaas.prompt.domain.CampaignObjective campaignObjective,
            String businessType,
            String brandContextSnapshot,
            SuggestionType suggestionType
    ) {
        try {
            promptHistoryRecorder.recordFailure(
                    workspaceId,
                    userId,
                    sourcePrompt,
                    language,
                    platform,
                    campaignObjective,
                    businessType,
                    brandContextSnapshot,
                    suggestionType,
                    aiProviderRouter.activeProviderName(),
                    aiProviderRouter.activeModelName());
        } catch (RuntimeException ignored) {
        }
    }
}
