package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.request.LoginRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.request.RefreshTokenRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.model.RefreshToken;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import com.krisnaajiep.expensetrackerapi.repository.RefreshTokenRepository;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();
    private final LoginRequestDto loginRequestDto = new LoginRequestDto();
    private final RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto();

    private static final String USER_NAME = "John Doe";
    private static final String USER_EMAIL = "john@doe.com";
    private static final String USER_PASSWORD = SecureRandomUtility.generateRandomString(8);

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        expenseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        passwordEncoder = new BCryptPasswordEncoder();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testRegisterBadRequest() throws Exception {
        registerRequestDto.setName("");
        registerRequestDto.setEmail("john.com");
        registerRequestDto.setPassword(null);

        mockMvc.perform(post("/register")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("errors"));
            assertEquals(3, ((Map<?, ?>) response.get("errors")).size());
            assertEquals("must not be blank", ((Map<?, ?>) response.get("errors")).get("name"));
            assertEquals("must be a well-formed email address", ((Map<?, ?>) response.get("errors")).get("email"));
            assertEquals("must not be blank", ((Map<?, ?>) response.get("errors")).get("password"));
        });
    }

    @Test
    void testRegisterConflict() throws Exception {
        User user = User.builder()
                .name(USER_NAME)
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .build();

        userRepository.save(user);

        registerRequestDto.setName(USER_NAME);
        registerRequestDto.setEmail(USER_EMAIL);
        registerRequestDto.setPassword(USER_PASSWORD);

        mockMvc.perform(post("/register")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
        ).andExpectAll(
                status().isConflict()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("User with this email already exists", response.get("message"));
        });
    }

    @Test
    void testRegisterSuccess() throws Exception {
        registerRequestDto.setName(USER_NAME);
        registerRequestDto.setEmail(USER_EMAIL);
        registerRequestDto.setPassword(passwordEncoder.encode(USER_PASSWORD));

        mockMvc.perform(post("/register")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequestDto))
        ).andExpectAll(
                status().isCreated()
        ).andDo(result -> {
            TokenResponseDto response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response.getAccessToken());

            String accessToken = response.getAccessToken();

            assertNotNull(jwtUtility.getEmail(accessToken));

            String email = jwtUtility.getEmail(accessToken);

            assertEquals(registerRequestDto.getEmail(), email);

            assertNotNull(response.getRefreshToken());
        });
    }

    @Test
    void testLoginBadRequest() throws Exception {
        loginRequestDto.setEmail("john.com");
        loginRequestDto.setPassword(null);

        mockMvc.perform(post("/login")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("errors"));
            assertEquals(2, ((Map<?, ?>) response.get("errors")).size());
            assertEquals("must be a well-formed email address", ((Map<?, ?>) response.get("errors")).get("email"));
            assertEquals("must not be blank", ((Map<?, ?>) response.get("errors")).get("password"));
        });
    }

    @Test
    void testLoginUnauthenticated() throws Exception {
        loginRequestDto.setEmail(USER_EMAIL);
        loginRequestDto.setPassword(USER_PASSWORD);

        mockMvc.perform(post("/login")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("Invalid credentials", response.get("message"));
        });
    }

    @Test
    void testLoginSuccess() throws Exception {
        User user = User.builder()
                .name(USER_NAME)
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .build();

        userRepository.save(user);

        loginRequestDto.setEmail(USER_EMAIL);
        loginRequestDto.setPassword(USER_PASSWORD);

        mockMvc.perform(post("/login")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            TokenResponseDto response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response.getAccessToken());

            String accessToken = response.getAccessToken();

            assertNotNull(jwtUtility.getEmail(accessToken));

            String email = jwtUtility.getEmail(accessToken);

            assertEquals(loginRequestDto.getEmail(), email);

            assertNotNull(response.getRefreshToken());
        });
    }

    @Test
    public void testRefreshBadRequest() throws Exception {
        refreshTokenRequestDto.setRefreshToken(null);

        mockMvc.perform(post("/refresh")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDto))
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("errors"));
            assertEquals(1, ((Map<?, ?>) response.get("errors")).size());
            assertEquals("must not be blank", ((Map<?, ?>) response.get("errors")).get("refresh-token"));
        });
    }

    @Test
    public void testRefreshNotFound() throws Exception {
        refreshTokenRequestDto.setRefreshToken("invalid refresh token");

        mockMvc.perform(post("/refresh")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDto))
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("Invalid refresh token", response.get("message"));
        });
    }

    @Test
    public void testRefreshExpired() throws Exception {
        String rawRefreshToken = SecureRandomUtility.generateRandomString(32);
        setRefreshToken(rawRefreshToken, Instant.now().minusSeconds(3600));
        refreshTokenRequestDto.setRefreshToken(rawRefreshToken);

        mockMvc.perform(post("/refresh")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDto))
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("Refresh token expired", response.get("message"));
        });
    }

    @Test
    public void testRefreshSuccess() throws Exception {
        String rawRefreshToken = SecureRandomUtility.generateRandomString(32);
        setRefreshToken(rawRefreshToken, Instant.now().plusMillis(86400000));
        refreshTokenRequestDto.setRefreshToken(rawRefreshToken);

        mockMvc.perform(post("/refresh")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshTokenRequestDto))
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            TokenResponseDto response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertNotNull(response);

            assertNotNull(response.getAccessToken());

            String accessToken = response.getAccessToken();

            assertNotNull(jwtUtility.getEmail(accessToken));

            assertNotNull(response.getRefreshToken());
        });
    }

    private void setRefreshToken(String token, Instant expiryDate) {
        User user = User.builder()
                .name(USER_NAME)
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(DigestUtils.sha256Hex(token))
                .expiryDate(expiryDate)
                .user(user)
                .build();

        userRepository.save(user);
        refreshTokenRepository.save(refreshToken);
    }
}