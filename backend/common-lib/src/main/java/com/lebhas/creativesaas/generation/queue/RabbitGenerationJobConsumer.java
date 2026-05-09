package com.lebhas.creativesaas.generation.queue;

import com.lebhas.creativesaas.generation.application.GenerationJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnExpression("'${platform.generation.queue.worker-enabled:false}' == 'true' && '${platform.generation.queue.provider:IN_MEMORY}' == 'RABBITMQ'")
public class RabbitGenerationJobConsumer {

    private static final Logger log = LoggerFactory.getLogger(RabbitGenerationJobConsumer.class);

    private final GenerationJobProcessor generationJobProcessor;

    public RabbitGenerationJobConsumer(GenerationJobProcessor generationJobProcessor) {
        this.generationJobProcessor = generationJobProcessor;
    }

    @RabbitListener(queues = "${platform.generation.queue.name:creative.generation.jobs}")
    public void consume(String jobId) {
        try {
            generationJobProcessor.process(UUID.fromString(jobId));
        } catch (IllegalArgumentException exception) {
            log.warn("generation_event type=invalid_queue_message jobId={}", jobId);
        }
    }
}
