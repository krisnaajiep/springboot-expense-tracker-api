package com.krisnaajiep.expensetrackerapi.security.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 19.08
@Last Modified 27/06/25 19.08
Version 1.0
*/

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "authentication")
@Setter
@Getter
@Validated
public class AuthProperties {
    private Jwt jwt;
    private RefreshToken refreshToken;

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank
        @Size(min = 32, max = 512)
        private String secret;

        @Positive
        private long expiration;
    }

    @Getter
    @Setter
    public static class RefreshToken {
        @Positive
        private long expiration;
    }
}
