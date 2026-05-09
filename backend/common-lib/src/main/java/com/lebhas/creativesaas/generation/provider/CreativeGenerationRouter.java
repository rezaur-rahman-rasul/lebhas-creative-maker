package com.lebhas.creativesaas.generation.provider;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.generation.application.CreativeGenerationProperties;
import com.lebhas.creativesaas.generation.domain.CreativeType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CreativeGenerationRouter {

    private final Map<CreativeAiProviderType, CreativeAiProvider> providers = new EnumMap<>(CreativeAiProviderType.class);
    private final CreativeGenerationProperties properties;

    public CreativeGenerationRouter(List<CreativeAiProvider> providers, CreativeGenerationProperties properties) {
        providers.forEach(provider -> this.providers.put(provider.type(), provider));
        this.properties = properties;
    }

    public AiGenerationResponse generate(AiGenerationRequest request) {
        CreativeAiProviderType providerType = providerFor(request.creativeType());
        CreativeAiProvider provider = providers.get(providerType);
        if (provider == null || providerType == CreativeAiProviderType.DISABLED) {
            throw new BusinessException(ErrorCode.GENERATION_PROVIDER_UNAVAILABLE, "Creative AI provider is not configured");
        }
        if (!provider.supports(request.creativeType())) {
            throw new BusinessException(ErrorCode.GENERATION_PROVIDER_REQUEST_FAILED, "Creative AI provider does not support " + request.creativeType());
        }
        return provider.generate(request);
    }

    public String plannedProviderName(CreativeType creativeType) {
        return providerFor(creativeType).name();
    }

    public String plannedModelName(CreativeType creativeType) {
        CreativeAiProviderType providerType = providerFor(creativeType);
        return switch (providerType) {
            case OPENAI -> properties.getOpenAi().getModel();
            case STABILITY -> properties.getStability().getModel();
            case GENERIC_VIDEO -> "generic-video-placeholder";
            case DISABLED -> null;
        };
    }

    private CreativeAiProviderType providerFor(CreativeType creativeType) {
        return creativeType.isVideo() ? properties.getVideoProvider() : properties.getImageProvider();
    }
}
