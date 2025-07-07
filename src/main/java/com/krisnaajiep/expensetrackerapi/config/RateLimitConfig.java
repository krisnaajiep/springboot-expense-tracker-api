package com.krisnaajiep.expensetrackerapi.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 07/07/25 12.47
@Last Modified 07/07/25 12.47
Version 1.0
*/

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "rate-limit")
@Setter
@Getter
@Validated
public class RateLimitConfig {
    @Positive
    private long capacity;

    @Positive
    private long refillAmount;

    @Positive
    private long refillDuration;
}
