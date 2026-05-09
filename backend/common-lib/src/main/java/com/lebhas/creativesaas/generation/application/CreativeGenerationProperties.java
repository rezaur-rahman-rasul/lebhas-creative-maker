package com.lebhas.creativesaas.generation.application;

import com.lebhas.creativesaas.generation.provider.CreativeAiProviderType;
import com.lebhas.creativesaas.generation.queue.GenerationQueueProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "platform.generation")
public class CreativeGenerationProperties {

    private CreativeAiProviderType imageProvider = CreativeAiProviderType.DISABLED;
    private CreativeAiProviderType videoProvider = CreativeAiProviderType.GENERIC_VIDEO;
    private int maxAttempts = 3;
    private Duration requestTimeout = Duration.ofSeconds(60);
    private final Queue queue = new Queue();
    private final OpenAi openAi = new OpenAi();
    private final Stability stability = new Stability();

    public CreativeAiProviderType getImageProvider() {
        return imageProvider;
    }

    public void setImageProvider(CreativeAiProviderType imageProvider) {
        this.imageProvider = imageProvider;
    }

    public CreativeAiProviderType getVideoProvider() {
        return videoProvider;
    }

    public void setVideoProvider(CreativeAiProviderType videoProvider) {
        this.videoProvider = videoProvider;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public Queue getQueue() {
        return queue;
    }

    public OpenAi getOpenAi() {
        return openAi;
    }

    public Stability getStability() {
        return stability;
    }

    public static class Queue {
        private GenerationQueueProvider provider = GenerationQueueProvider.IN_MEMORY;
        private boolean workerEnabled;
        private String name = "creative.generation.jobs";
        private String exchange = "creative.generation";
        private String routingKey = "generation.job.created";
        private String deadLetterExchange = "creative.generation.dlx";
        private String deadLetterQueue = "creative.generation.jobs.dlq";
        private String deadLetterRoutingKey = "generation.job.dead";

        public GenerationQueueProvider getProvider() {
            return provider;
        }

        public void setProvider(GenerationQueueProvider provider) {
            this.provider = provider;
        }

        public boolean isWorkerEnabled() {
            return workerEnabled;
        }

        public void setWorkerEnabled(boolean workerEnabled) {
            this.workerEnabled = workerEnabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public String getDeadLetterExchange() {
            return deadLetterExchange;
        }

        public void setDeadLetterExchange(String deadLetterExchange) {
            this.deadLetterExchange = deadLetterExchange;
        }

        public String getDeadLetterQueue() {
            return deadLetterQueue;
        }

        public void setDeadLetterQueue(String deadLetterQueue) {
            this.deadLetterQueue = deadLetterQueue;
        }

        public String getDeadLetterRoutingKey() {
            return deadLetterRoutingKey;
        }

        public void setDeadLetterRoutingKey(String deadLetterRoutingKey) {
            this.deadLetterRoutingKey = deadLetterRoutingKey;
        }
    }

    public static class OpenAi {
        private String baseUrl = "https://api.openai.com";
        private String imagePath = "/v1/images/generations";
        private String apiKey = "";
        private String model = "gpt-image-1.5";
        private String organization = "";
        private String project = "";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getOrganization() {
            return organization;
        }

        public void setOrganization(String organization) {
            this.organization = organization;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }
    }

    public static class Stability {
        private String baseUrl = "https://api.stability.ai";
        private String generationPath = "/v2beta/stable-image/generate/core";
        private String apiKey = "";
        private String model = "stable-image-core";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getGenerationPath() {
            return generationPath;
        }

        public void setGenerationPath(String generationPath) {
            this.generationPath = generationPath;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
