package com.lebhas.creativesaas.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "platform.services.auth")
public class AuthGatewayProperties {

    private URI baseUrl = URI.create("http://localhost:8081");

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }
}
