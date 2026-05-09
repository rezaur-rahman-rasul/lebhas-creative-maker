package com.lebhas.creativesaas.creative.interfaces;

import com.lebhas.creativesaas.common.api.ApiResponse;
import com.lebhas.creativesaas.prompt.application.PromptIntelligenceService;
import com.lebhas.creativesaas.prompt.application.dto.PromptEnhancementCommand;
import com.lebhas.creativesaas.prompt.application.dto.PromptEnhancementView;
import com.lebhas.creativesaas.prompt.application.dto.PromptRewriteCommand;
import com.lebhas.creativesaas.prompt.application.dto.PromptRewriteView;
import com.lebhas.creativesaas.prompt.application.dto.PromptSuggestionCommand;
import com.lebhas.creativesaas.prompt.application.dto.PromptSuggestionListView;
import com.lebhas.creativesaas.prompt.application.dto.PromptSuggestionsView;
import com.lebhas.creativesaas.prompt.domain.SuggestionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/prompts")
@Tag(name = "Prompt Intelligence")
@SecurityRequirement(name = "bearerAuth")
public class PromptIntelligenceController {

    private final PromptIntelligenceService promptIntelligenceService;

    public PromptIntelligenceController(PromptIntelligenceService promptIntelligenceService) {
        this.promptIntelligenceService = promptIntelligenceService;
    }

    @PostMapping("/enhance")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Enhance a rough creative prompt")
    public ApiResponse<PromptEnhancementView> enhance(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptEnhanceRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.enhance(new PromptEnhancementCommand(
                workspaceId,
                request.customPrompt(),
                request.assetIds(),
                request.templateId(),
                request.businessType(),
                request.campaignObjective(),
                request.platform(),
                request.creativeStyle(),
                request.language(),
                request.tone(),
                request.targetAudience(),
                request.offerDetails(),
                request.ctaPreference(),
                request.useBrandProfile(),
                resolveClientIp(httpServletRequest))));
    }

    @PostMapping("/rewrite")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Rewrite an existing creative prompt into stronger variations")
    public ApiResponse<PromptRewriteView> rewrite(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptRewriteRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.rewrite(new PromptRewriteCommand(
                workspaceId,
                request.existingPrompt(),
                request.assetIds(),
                request.templateId(),
                request.businessType(),
                request.campaignObjective(),
                request.platform(),
                request.creativeStyle(),
                request.language(),
                request.tone(),
                request.targetAudience(),
                request.offerDetails(),
                request.ctaPreference(),
                request.useBrandProfile(),
                resolveClientIp(httpServletRequest))));
    }

    @PostMapping("/suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate grouped prompt intelligence suggestions")
    public ApiResponse<PromptSuggestionsView> suggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestions(toSuggestionCommand(
                workspaceId,
                request,
                request.suggestionTypes(),
                httpServletRequest)));
    }

    @PostMapping("/cta-suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate CTA suggestions")
    public ApiResponse<PromptSuggestionListView> ctaSuggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestionList(
                toSuggestionCommand(workspaceId, request, Set.of(SuggestionType.CTA_SUGGESTIONS), httpServletRequest),
                SuggestionType.CTA_SUGGESTIONS));
    }

    @PostMapping("/headline-suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate headline suggestions")
    public ApiResponse<PromptSuggestionListView> headlineSuggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestionList(
                toSuggestionCommand(workspaceId, request, Set.of(SuggestionType.HEADLINE_SUGGESTIONS), httpServletRequest),
                SuggestionType.HEADLINE_SUGGESTIONS));
    }

    @PostMapping("/creative-angle-suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate creative angle suggestions")
    public ApiResponse<PromptSuggestionListView> creativeAngleSuggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestionList(
                toSuggestionCommand(workspaceId, request, Set.of(SuggestionType.CREATIVE_ANGLE_SUGGESTIONS), httpServletRequest),
                SuggestionType.CREATIVE_ANGLE_SUGGESTIONS));
    }

    @PostMapping("/offer-suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate offer suggestions")
    public ApiResponse<PromptSuggestionListView> offerSuggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestionList(
                toSuggestionCommand(workspaceId, request, Set.of(SuggestionType.OFFER_SUGGESTIONS), httpServletRequest),
                SuggestionType.OFFER_SUGGESTIONS));
    }

    @PostMapping("/campaign-tone-suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate campaign tone suggestions")
    public ApiResponse<PromptSuggestionListView> campaignToneSuggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestionList(
                toSuggestionCommand(workspaceId, request, Set.of(SuggestionType.CAMPAIGN_TONE_SUGGESTIONS), httpServletRequest),
                SuggestionType.CAMPAIGN_TONE_SUGGESTIONS));
    }

    @PostMapping("/business-category-suggestions")
    @PreAuthorize("hasAuthority('PROMPT_INTELLIGENCE_USE')")
    @Operation(summary = "Generate business category suggestions")
    public ApiResponse<PromptSuggestionListView> businessCategorySuggestions(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody PromptSuggestionsRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success(promptIntelligenceService.generateSuggestionList(
                toSuggestionCommand(workspaceId, request, Set.of(SuggestionType.BUSINESS_CATEGORY_SUGGESTIONS), httpServletRequest),
                SuggestionType.BUSINESS_CATEGORY_SUGGESTIONS));
    }

    private PromptSuggestionCommand toSuggestionCommand(
            UUID workspaceId,
            PromptSuggestionsRequest request,
            Set<SuggestionType> suggestionTypes,
            HttpServletRequest httpServletRequest
    ) {
        return new PromptSuggestionCommand(
                workspaceId,
                request.customPrompt(),
                request.assetIds(),
                request.templateId(),
                request.businessType(),
                request.campaignObjective(),
                request.platform(),
                request.creativeStyle(),
                request.language(),
                request.tone(),
                request.targetAudience(),
                request.offerDetails(),
                request.ctaPreference(),
                request.useBrandProfile(),
                suggestionTypes,
                resolveClientIp(httpServletRequest));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            int separator = forwardedFor.indexOf(',');
            return (separator >= 0 ? forwardedFor.substring(0, separator) : forwardedFor).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp.trim() : request.getRemoteAddr();
    }
}
