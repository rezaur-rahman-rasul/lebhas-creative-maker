package com.lebhas.creativesaas.generation.queue;

import com.lebhas.creativesaas.generation.application.GenerationJobProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${platform.generation.queue.worker-enabled:false}' == 'true' && '${platform.generation.queue.provider:IN_MEMORY}' == 'IN_MEMORY'")
public class InMemoryGenerationJobQueuePublisher implements GenerationJobQueuePublisher {

    private final TaskExecutor taskExecutor;
    private final GenerationJobProcessor generationJobProcessor;

    public InMemoryGenerationJobQueuePublisher(
            @Qualifier("generationTaskExecutor") TaskExecutor taskExecutor,
            GenerationJobProcessor generationJobProcessor
    ) {
        this.taskExecutor = taskExecutor;
        this.generationJobProcessor = generationJobProcessor;
    }

    @Override
    public void publish(GenerationJobQueuedEvent event) {
        taskExecutor.execute(() -> generationJobProcessor.process(event.jobId()));
    }
}
