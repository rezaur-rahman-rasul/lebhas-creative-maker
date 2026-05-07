package com.lebhas.creativesaas.common.jpa;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@AutoConfigurationPackage(basePackages = "com.lebhas.creativesaas")
@EnableJpaRepositories(basePackages = "com.lebhas.creativesaas")
public class PlatformJpaConfiguration {
}
