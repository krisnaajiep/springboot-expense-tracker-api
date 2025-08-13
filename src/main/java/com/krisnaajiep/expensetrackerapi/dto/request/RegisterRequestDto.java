package com.krisnaajiep.expensetrackerapi.dto.request;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 03.23
@Last Modified 27/06/25 03.23
Version 1.0
*/

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "RegisterRequest", description = "Register request body")
public class RegisterRequestDto {
    @NotBlank
    @Size(min = 1, max = 255)
    @Schema(description = "User name", example = "John Doe")
    @Pattern(regexp = "^[\\p{L}\\s,.'-]*$", message = "{name.Pattern}")
    private String name;

    @NotBlank
    @Email
    @Size(min = 1, max = 255)
    @Schema(description = "User email", example = "john@doe.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@_#\\-$]).*$", message = "{password.Pattern}")
    @Schema(description = "User password", example = "MyPass_1234")
    private String password;
}
