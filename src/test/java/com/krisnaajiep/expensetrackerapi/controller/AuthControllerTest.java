package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    private static final String USER_NAME = "John Doe";
    private static final String USER_EMAIL = "john@doe.com";
    private static final String USER_PASSWORD = SecureRandomUtility.generateRandomString(8);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        // Clear the user repository before each test
        userRepository.deleteAll();
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
                    new TypeReference<>() {}
            );

            System.out.printf("response: %s%n", response);

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
                .password(USER_PASSWORD)
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
                    new TypeReference<>() {}
            );

            System.out.printf("response: %s%n", response);

            assertNotNull(response);
            assertNotNull(response.get("message"));
            assertEquals("User with this email already exists", response.get("message"));
        });
    }

    @Test
    void testRegisterSuccess() throws Exception {
        registerRequestDto.setName(USER_NAME);
        registerRequestDto.setEmail(USER_EMAIL);
        registerRequestDto.setPassword(USER_PASSWORD);

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

            System.out.printf("Access Token: %s%n", response.getAccessToken());

            assertNotNull(response.getAccessToken());

            String accessToken = response.getAccessToken();

            assertNotNull(jwtUtility.getEmail(accessToken));

            String email = jwtUtility.getEmail(accessToken);

            System.out.printf("Email from token: %s%n", email);

            assertEquals(registerRequestDto.getEmail(), email);
        });
    }
}