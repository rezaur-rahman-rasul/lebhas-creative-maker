package com.lebhas.creativesaas.prompt.domain;

import com.lebhas.creativesaas.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "prompt_templates", schema = "platform")
public class PromptTemplateEntity extends BaseEntity {

    @Column(name = "workspace_id")
    private UUID workspaceId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 40)
    private PromptPlatform platform;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_objective", length = 40)
    private CampaignObjective campaignObjective;

    @Column(name = "business_type", length = 80)
    private String businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 30)
    private PromptLanguage language;

    @Column(name = "template_text", nullable = false, columnDefinition = "TEXT")
    private String templateText;

    @Column(name = "is_system_default", nullable = false)
    private boolean systemDefault;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PromptTemplateStatus status;

    protected PromptTemplateEntity() {
    }

    public static PromptTemplateEntity create(
            UUID workspaceId,
            String name,
            String description,
            PromptPlatform platform,
            CampaignObjective campaignObjective,
            String businessType,
            PromptLanguage language,
            String templateText,
            boolean systemDefault,
            PromptTemplateStatus status
    ) {
        PromptTemplateEntity entity = new PromptTemplateEntity();
        entity.workspaceId = systemDefault ? null : workspaceId;
        entity.name = normalizeRequired(name);
        entity.description = normalizeNullable(description);
        entity.platform = platform;
        entity.campaignObjective = campaignObjective;
        entity.businessType = normalizeNullable(businessType);
        entity.language = language;
        entity.templateText = normalizeRequired(templateText);
        entity.systemDefault = systemDefault;
        entity.status = status == null ? PromptTemplateStatus.ACTIVE : status;
        return entity;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public PromptPlatform getPlatform() {
        return platform;
    }

    public CampaignObjective getCampaignObjective() {
        return campaignObjective;
    }

    public String getBusinessType() {
        return businessType;
    }

    public PromptLanguage getLanguage() {
        return language;
    }

    public String getTemplateText() {
        return templateText;
    }

    public boolean isSystemDefault() {
        return systemDefault;
    }

    public PromptTemplateStatus getStatus() {
        return status;
    }

    public boolean isActiveTemplate() {
        return status == PromptTemplateStatus.ACTIVE && !isDeleted();
    }

    public boolean isAccessibleInWorkspace(UUID requestedWorkspaceId) {
        return systemDefault || (workspaceId != null && workspaceId.equals(requestedWorkspaceId));
    }

    public void update(
            UUID workspaceId,
            String name,
            String description,
            PromptPlatform platform,
            CampaignObjective campaignObjective,
            String businessType,
            PromptLanguage language,
            String templateText,
            boolean systemDefault,
            PromptTemplateStatus status
    ) {
        this.workspaceId = systemDefault ? null : workspaceId;
        this.name = normalizeRequired(name);
        this.description = normalizeNullable(description);
        this.platform = platform;
        this.campaignObjective = campaignObjective;
        this.businessType = normalizeNullable(businessType);
        this.language = language;
        this.templateText = normalizeRequired(templateText);
        this.systemDefault = systemDefault;
        this.status = status == null ? PromptTemplateStatus.ACTIVE : status;
    }

    private static String normalizeRequired(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
