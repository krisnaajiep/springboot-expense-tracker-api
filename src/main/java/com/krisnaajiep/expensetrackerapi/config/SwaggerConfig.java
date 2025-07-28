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
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private final Environment environment;
    public static final String SECURITY_SCHEME_NAME = "BearerToken";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(components())
                .info(info())
                .servers(servers());
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
                .description("""
                Simple Expense Tracker RESTful API built with Spring Boot to allow users to create, read, update, and delete expenses.
                
                [GitHub Repository](https://github.com/krisnaajiep/springboot-expense-tracker-api)
                """)
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

    private List<Server> servers() {
        return List.of(
                new Server().url("http://localhost:" + environment.getProperty("server.port"))
                        .description("⚠️ Only for local testing")
        );
    }
}
