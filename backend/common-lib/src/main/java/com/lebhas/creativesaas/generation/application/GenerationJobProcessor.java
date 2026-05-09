package com.lebhas.creativesaas.generation.application;

import com.lebhas.creativesaas.generation.provider.AiGenerationRequest;
import com.lebhas.creativesaas.generation.provider.AiGenerationResponse;
import com.lebhas.creativesaas.generation.provider.CreativeGenerationRouter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GenerationJobProcessor {

    private final GenerationJobStateService jobStateService;
    private final CreativeGenerationPromptRenderer promptRenderer;
    private final CreativeGenerationRouter generationRouter;

    public GenerationJobProcessor(
            GenerationJobStateService jobStateService,
            CreativeGenerationPromptRenderer promptRenderer,
            CreativeGenerationRouter generationRouter
    ) {
        this.jobStateService = jobStateService;
        this.promptRenderer = promptRenderer;
        this.generationRouter = generationRouter;
    }

    public void process(UUID jobId) {
        GenerationWorkItem workItem = jobStateService.start(jobId);
        if (workItem == null) {
            return;
        }
        String providerName = generationRouter.plannedProviderName(workItem.creativeType());
        try {
            AiGenerationRequest providerRequest = promptRenderer.toProviderRequest(workItem);
            AiGenerationResponse response = generationRouter.generate(providerRequest);
            jobStateService.complete(jobId, response);
        } catch (RuntimeException exception) {
            jobStateService.fail(jobId, providerName, exception);
        }
    }
}
