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

import com.krisnaajiep.expensetrackerapi.dto.request.LoginRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.mapper.UserMapper;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

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
}
