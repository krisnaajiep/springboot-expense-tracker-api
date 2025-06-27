package com.krisnaajiep.expensetrackerapi.dto.response;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 04.36
@Last Modified 27/06/25 04.36
Version 1.0
*/

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponseDto {
    @JsonProperty("access-token")
    private String accessToken;
}
