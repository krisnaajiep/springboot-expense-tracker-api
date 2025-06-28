package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.ConflictException;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtility jwtUtility;

    @InjectMocks
    private AuthService authService;

    private final User user = new User();
    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    private static final String ACCESS_TOKEN = "mockedAccessToken";
    private static final String ENCODED_PASSWORD = "<PASSWORD>";

    @BeforeEach
    void setUp() {
        registerRequestDto.setName(user.getName());
        registerRequestDto.setEmail(user.getEmail());
        registerRequestDto.setPassword("password123");

        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@doe.com");
        user.setPassword(ENCODED_PASSWORD);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByEmail(registerRequestDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequestDto.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtility.generateToken(user.getId().toString(), user.getEmail())).thenReturn(ACCESS_TOKEN);

        TokenResponseDto tokenResponseDto = authService.register(registerRequestDto);

        assertNotNull(tokenResponseDto);
        assertEquals(ACCESS_TOKEN, tokenResponseDto.getAccessToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtility, times(1)).generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testRegisterFailure_EmailExists() {
        when(userRepository.existsByEmail(registerRequestDto.getEmail())).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> authService.register(registerRequestDto)
        );

        assertEquals("User with this email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtility, never()).generateToken(anyString(), anyString());
    }
}