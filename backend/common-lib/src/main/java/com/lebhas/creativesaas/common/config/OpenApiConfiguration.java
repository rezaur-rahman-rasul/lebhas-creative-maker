package com.lebhas.creativesaas.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI platformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Creative SaaS Platform API")
                        .version("0.1.0")
                        .description("Backend foundation for the Bangladesh creative SaaS platform"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    GroupedOpenApi foundationApiGroup() {
        return GroupedOpenApi.builder()
                .group("foundation")
                .pathsToMatch("/health", "/liveness", "/readiness", "/actuator/**")
                .build();
    }
}
