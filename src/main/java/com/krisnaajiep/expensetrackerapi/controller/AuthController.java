package com.krisnaajiep.expensetrackerapi.controller;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 03.29
@Last Modified 27/06/25 03.29
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.config.SwaggerConfig;
import com.krisnaajiep.expensetrackerapi.dto.request.LoginRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.request.RefreshTokenRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.mapper.UserMapper;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.security.CustomUserDetails;
import com.krisnaajiep.expensetrackerapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth", description = "User authentication endpoints")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content),
    })
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TokenResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequestDto) {
        User user = UserMapper.toUser(registerRequestDto);
        TokenResponseDto tokenResponseDto = authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponseDto);
    }

    @Operation(summary = "Login user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
    })
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto tokenResponseDto = authService.login(
                loginRequestDto.getEmail(),
                loginRequestDto.getPassword()
        );
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponseDto);
    }

    @Operation(summary = "Refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token", content = @Content)
    })
    @PostMapping(
            value = "/refresh",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TokenResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequestDto) {
        TokenResponseDto tokenResponseDto = authService.refreshToken(
                refreshTokenRequestDto.getRefreshToken()
        );
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponseDto);
    }

    @Operation(summary = "Revoke all refresh tokens")
    @SecurityRequirement(name = SwaggerConfig.SECURITY_SCHEME_NAME)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens revoked successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(
            value = "/revoke",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> revoke(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.revoke(userDetails.getId());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "All tokens revoked successfully"));
    }
}
