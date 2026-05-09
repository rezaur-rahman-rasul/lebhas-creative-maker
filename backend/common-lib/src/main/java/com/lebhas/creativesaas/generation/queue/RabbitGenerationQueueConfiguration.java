package com.lebhas.creativesaas.generation.queue;

import com.lebhas.creativesaas.generation.application.CreativeGenerationProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("'${platform.generation.queue.worker-enabled:false}' == 'true' && '${platform.generation.queue.provider:IN_MEMORY}' == 'RABBITMQ'")
public class RabbitGenerationQueueConfiguration {

    @Bean
    DirectExchange generationExchange(CreativeGenerationProperties properties) {
        return new DirectExchange(properties.getQueue().getExchange(), true, false);
    }

    @Bean
    DirectExchange generationDeadLetterExchange(CreativeGenerationProperties properties) {
        return new DirectExchange(properties.getQueue().getDeadLetterExchange(), true, false);
    }

    @Bean
    Queue generationQueue(CreativeGenerationProperties properties) {
        return QueueBuilder.durable(properties.getQueue().getName())
                .deadLetterExchange(properties.getQueue().getDeadLetterExchange())
                .deadLetterRoutingKey(properties.getQueue().getDeadLetterRoutingKey())
                .build();
    }

    @Bean
    Queue generationDeadLetterQueue(CreativeGenerationProperties properties) {
        return QueueBuilder.durable(properties.getQueue().getDeadLetterQueue()).build();
    }

    @Bean
    Binding generationQueueBinding(Queue generationQueue, DirectExchange generationExchange, CreativeGenerationProperties properties) {
        return BindingBuilder.bind(generationQueue)
                .to(generationExchange)
                .with(properties.getQueue().getRoutingKey());
    }

    @Bean
    Binding generationDeadLetterQueueBinding(
            Queue generationDeadLetterQueue,
            DirectExchange generationDeadLetterExchange,
            CreativeGenerationProperties properties
    ) {
        return BindingBuilder.bind(generationDeadLetterQueue)
                .to(generationDeadLetterExchange)
                .with(properties.getQueue().getDeadLetterRoutingKey());
    }
}
