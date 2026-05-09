package com.lebhas.creativesaas.generation.queue;

import com.lebhas.creativesaas.generation.application.CreativeGenerationProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${platform.generation.queue.worker-enabled:false}' == 'true' && '${platform.generation.queue.provider:IN_MEMORY}' == 'RABBITMQ'")
public class RabbitGenerationJobQueuePublisher implements GenerationJobQueuePublisher {

    private final RabbitTemplate rabbitTemplate;
    private final CreativeGenerationProperties properties;

    public RabbitGenerationJobQueuePublisher(RabbitTemplate rabbitTemplate, CreativeGenerationProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void publish(GenerationJobQueuedEvent event) {
        rabbitTemplate.convertAndSend(
                properties.getQueue().getExchange(),
                properties.getQueue().getRoutingKey(),
                event.jobId().toString());
    }
}
