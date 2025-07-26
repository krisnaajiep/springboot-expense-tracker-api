package com.krisnaajiep.expensetrackerapi.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 25/07/25 14.54
@Last Modified 25/07/25 14.54
Version 1.0
*/

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private final Environment environment;
    public static final String SECURITY_SCHEME_NAME = "BearerToken";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(components())
                .info(info());
    }

    private Components components() {
        return new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }

    private Info info() {
        return new Info()
                .title(environment.getProperty("spring.application.name"))
                .description("Simple Expense Tracker RESTful API to allow users to manage their expenses.")
                .version(environment.getProperty("spring.application.version"))
                .contact(contact())
                .license(license());
    }

    private Contact contact() {
        return new Contact()
                .name("Krisna Ajie")
                .email("krisnaajiep@gmail.com")
                .url("https://krisnaajiep.github.io/");
    }

    private License license() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }
}
