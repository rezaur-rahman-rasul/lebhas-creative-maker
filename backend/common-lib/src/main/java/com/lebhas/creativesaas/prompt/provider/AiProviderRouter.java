package com.lebhas.creativesaas.prompt.provider;

import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AiProviderRouter {

    private final Map<AiProviderType, TextAiProvider> providers = new EnumMap<>(AiProviderType.class);
    private final PromptAiProperties properties;

    public AiProviderRouter(List<TextAiProvider> providers, PromptAiProperties properties) {
        providers.forEach(provider -> this.providers.put(provider.type(), provider));
        this.properties = properties;
    }

    public AiResponse generate(AiRequest request) {
        TextAiProvider provider = providers.get(properties.getProvider());
        if (provider == null || properties.getProvider() == AiProviderType.DISABLED) {
            throw new BusinessException(ErrorCode.PROMPT_AI_PROVIDER_UNAVAILABLE, "Text AI provider is not configured");
        }
        return provider.generate(request);
    }

    public String activeProviderName() {
        return properties.getProvider().name();
    }

    public String activeModelName() {
        return switch (properties.getProvider()) {
            case OPENAI -> properties.getOpenAi().getModel();
            case DISABLED -> null;
        };
    }
}
