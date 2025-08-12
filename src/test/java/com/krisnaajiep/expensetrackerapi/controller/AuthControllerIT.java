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
import com.krisnaajiep.expensetrackerapi.security.config.AuthProperties;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import com.krisnaajiep.expensetrackerapi.util.ValidationMessages;
import org.apache.commons.codec.digest.DigestUtils;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @Autowired
    private AuthProperties authProperties;

    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();
    private final LoginRequestDto loginRequestDto = new LoginRequestDto();
    private final RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto();

    private static final String USER_NAME = "John Doe";
    private static final String USER_EMAIL = "john@doe.com";
    private static final String USER_PASSWORD = SecureRandomUtility.generateRandomString(8) + "1_2";

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        expenseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void testRegister_ValidationErrors() throws Exception {
        registerRequestDto.setName("<h1>John Doe</h1>");
        registerRequestDto.setEmail("john_doe.com");
        registerRequestDto.setPassword("invalid123");

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
            assertEquals(
                    ValidationMessages.getValidationMessage("name.Pattern"),
                    ((Map<?, ?>) response.get("errors")).get("name")
            );
            assertEquals(ValidationMessages.EMAIL_MESSAGE, ((Map<?, ?>) response.get("errors")).get("email"));
            assertEquals(
                    ValidationMessages.getValidationMessage("password.Pattern"),
                    ((Map<?, ?>) response.get("errors")).get("password")
            );
        });
    }

    @Test
    void testRegister_Conflict() throws Exception {
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
    void testRegister_Success() throws Exception {
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
    void testLogin_ValidationErrors() throws Exception {
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
            assertEquals(ValidationMessages.EMAIL_MESSAGE, ((Map<?, ?>) response.get("errors")).get("email"));
            assertEquals(ValidationMessages.NOT_BLANK_MESSAGE, ((Map<?, ?>) response.get("errors")).get("password"));
        });
    }

    @Test
    void testLogin_Unauthenticated() throws Exception {
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
    void testLogin_TooManyRequests() throws Exception {
        loginRequestDto.setEmail(USER_EMAIL);
        loginRequestDto.setPassword(USER_PASSWORD);
        final String clientIp = "127.0.0.2";
        final long maxAttempts = authProperties.getLogin().getMaxAttempts();

        for (int i = 0; i < maxAttempts; i++) {
            mockMvc.perform(post("/login")
                    .accept(MediaType.ALL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequestDto))
                    .header("X-Forwarded-For", clientIp)
            ).andExpectAll(
                    status().isUnauthorized()
            );
        }

        mockMvc.perform(post("/login")
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDto))
                .header("X-Forwarded-For", clientIp)
        ).andExpectAll(
                status().isTooManyRequests()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertNotNull(response);
            assertNotNull(result.getResponse().getHeader("Retry-After"));
            assertNotNull(response.get("message"));
            assertEquals("Too many failed login attempts. Please try again later.", response.get("message"));
        });
    }

    @Test
    void testLogin_Success() throws Exception {
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
    void testRefresh_BadRequest() throws Exception {
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
    void testRefresh_NotFound() throws Exception {
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
    void testRefresh_Expired() throws Exception {
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
    void testRefresh_Success() throws Exception {
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

    @Test
    void testRevoke_Unauthorized() throws Exception {
        String accessToken = jwtUtility.generateToken(Long.toString(1L), null);

        mockMvc.perform(post("/revoke")
                .accept(MediaType.ALL)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    void testRevoke_Success() throws Exception {
        String rawRefreshToken = SecureRandomUtility.generateRandomString(32);
        setRefreshToken(rawRefreshToken, Instant.now().plusMillis(86400000));
        refreshTokenRequestDto.setRefreshToken(rawRefreshToken);

        String accessToken = jwtUtility.generateToken(Long.toString(1L), USER_EMAIL);

        mockMvc.perform(post("/revoke")
                .accept(MediaType.ALL)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("All tokens revoked successfully", response.get("message"));
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