package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.request.LoginRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.ConflictException;
import com.krisnaajiep.expensetrackerapi.model.CustomUserDetails;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
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

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private final User user = new User();
    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();
    private final LoginRequestDto loginRequestDto = new LoginRequestDto();

    private static final String ACCESS_TOKEN = SecureRandomUtility.generateRandomString(32);
    private static final String PASSWORD = SecureRandomUtility.generateRandomString(8);
    private static final String ENCODED_PASSWORD = SecureRandomUtility.generateRandomString(10);

    @BeforeEach
    void setUp() {
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@doe.com");
        user.setPassword(ENCODED_PASSWORD);

        registerRequestDto.setName(user.getName());
        registerRequestDto.setEmail(user.getEmail());
        registerRequestDto.setPassword(PASSWORD);

        loginRequestDto.setEmail(user.getEmail());
        loginRequestDto.setPassword(PASSWORD);
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

    @Test
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(user.getId());
        when(userDetails.getUsername()).thenReturn(user.getEmail());
        when(jwtUtility.generateToken(user.getId().toString(), user.getEmail())).thenReturn(ACCESS_TOKEN);

        TokenResponseDto tokenResponseDto = authService.login(loginRequestDto);

        assertNotNull(tokenResponseDto);
        assertEquals(ACCESS_TOKEN, tokenResponseDto.getAccessToken());

        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(authentication, times(1)).getPrincipal();
        verify(userDetails, times(1)).getId();
        verify(userDetails, times(1)).getUsername();
        verify(jwtUtility, times(1)).generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testLoginFailure_InvalidCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequestDto));

        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(authentication, never()).getPrincipal();
        verify(userDetails, never()).getId();
        verify(userDetails, never()).getUsername();
        verify(jwtUtility, never()).generateToken(anyString(), anyString());
    }
}