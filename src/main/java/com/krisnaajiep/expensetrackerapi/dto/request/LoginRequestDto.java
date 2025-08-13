package com.krisnaajiep.expensetrackerapi.dto.request;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 28/06/25 23.03
@Last Modified 28/06/25 23.03
Version 1.0
*/

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "Login request body")
public class LoginRequestDto {
    @NotBlank
    @Email
    @Schema(description = "User email", example = "john@doe.com")
    private String email;

    @NotBlank
    @Schema(description = "User password", example = "MyPass_1234")
    private String password;
}
