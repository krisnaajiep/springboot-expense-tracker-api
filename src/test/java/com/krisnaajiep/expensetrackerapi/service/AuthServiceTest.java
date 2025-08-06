package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.security.config.AuthProperties;
import com.krisnaajiep.expensetrackerapi.dto.response.TokenResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.ConflictException;
import com.krisnaajiep.expensetrackerapi.handler.exception.UnauthorizedException;
import com.krisnaajiep.expensetrackerapi.model.RefreshToken;
import com.krisnaajiep.expensetrackerapi.repository.RefreshTokenRepository;
import com.krisnaajiep.expensetrackerapi.security.CustomUserDetails;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.security.service.LoginAttemptService;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.apache.commons.codec.digest.DigestUtils;
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

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

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

    @Mock
    private AuthProperties authProperties;

    @Mock
    private AuthProperties.RefreshToken refreshTokenProperties;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    private final User user = new User();
    private final RefreshToken refreshToken = new RefreshToken();

    private static final String ACCESS_TOKEN = SecureRandomUtility.generateRandomString(32);
    private static final String REFRESH_TOKEN = SecureRandomUtility.generateRandomString(32);
    private static final String ENCODED_REFRESH_TOKEN = DigestUtils.sha256Hex(REFRESH_TOKEN);
    private static final String PASSWORD = SecureRandomUtility.generateRandomString(8);
    private static final String ENCODED_PASSWORD = SecureRandomUtility.generateRandomString(10);
    private static final long REFRESH_TOKEN_EXP = 86400000;
    private static final String CLIENT_IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@doe.com");
        user.setPassword(ENCODED_PASSWORD);

        refreshToken.setToken(ENCODED_REFRESH_TOKEN);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(86400000));
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(user.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtility.generateToken(user.getId().toString(), user.getEmail())).thenReturn(ACCESS_TOKEN);
        when(authProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
        when(refreshTokenProperties.getExpiration()).thenReturn(REFRESH_TOKEN_EXP);

        TokenResponseDto tokenResponseDto = authService.register(user);

        assertNotNull(tokenResponseDto);
        assertNotNull(tokenResponseDto.getAccessToken());
        assertNotNull(tokenResponseDto.getRefreshToken());
        assertEquals(ACCESS_TOKEN, tokenResponseDto.getAccessToken());

        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtility, times(1)).generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> authService.register(user)
        );

        assertEquals("User with this email already exists", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(jwtUtility, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testLogin_Success() {
        when(loginAttemptService.getClientIp()).thenReturn(CLIENT_IP);
        when(loginAttemptService.isJailed(CLIENT_IP)).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(user.getId());
        when(userDetails.getUsername()).thenReturn(user.getEmail());
        when(jwtUtility.generateToken(user.getId().toString(), user.getEmail())).thenReturn(ACCESS_TOKEN);
        when(authProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
        when(refreshTokenProperties.getExpiration()).thenReturn(REFRESH_TOKEN_EXP);

        TokenResponseDto tokenResponseDto = authService.login(user.getEmail(), PASSWORD);

        assertNotNull(tokenResponseDto);
        assertNotNull(tokenResponseDto.getAccessToken());
        assertNotNull(tokenResponseDto.getRefreshToken());
        assertEquals(ACCESS_TOKEN, tokenResponseDto.getAccessToken());

        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(authentication, times(1)).getPrincipal();
        verify(userDetails, times(2)).getId();
        verify(userDetails, times(1)).getUsername();
        verify(jwtUtility, times(1)).generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testLogin_InvalidCredentials() {
        when(loginAttemptService.getClientIp()).thenReturn(CLIENT_IP);
        when(loginAttemptService.isJailed(CLIENT_IP)).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authService.login(user.getEmail(), PASSWORD));

        verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
        verify(authentication, never()).getPrincipal();
        verify(userDetails, never()).getId();
        verify(userDetails, never()).getUsername();
        verify(jwtUtility, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testRefresh_Success() {
        when(refreshTokenRepository.findByToken(ENCODED_REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));
        when(jwtUtility.generateToken(user.getId().toString(), user.getEmail())).thenReturn(ACCESS_TOKEN);
        when(authProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
        when(refreshTokenProperties.getExpiration()).thenReturn(REFRESH_TOKEN_EXP);

        TokenResponseDto tokenResponseDto = authService.refreshToken(REFRESH_TOKEN);

        assertNotNull(tokenResponseDto);
        assertEquals(ACCESS_TOKEN, tokenResponseDto.getAccessToken());
        assertNotEquals(REFRESH_TOKEN, tokenResponseDto.getRefreshToken());

        verify(refreshTokenRepository, times(1)).findByToken(ENCODED_REFRESH_TOKEN);
        verify(jwtUtility, times(1)).generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testRefresh_NotFound() {
        when(refreshTokenRepository.findByToken(ENCODED_REFRESH_TOKEN)).thenThrow(UnauthorizedException.class);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(REFRESH_TOKEN));

        verify(refreshTokenRepository, times(1)).findByToken(ENCODED_REFRESH_TOKEN);
        verify(jwtUtility, never()).generateToken(anyString(), anyString());
    }

    @Test
    void testRefresh_Expired() {
        refreshToken.setExpiryDate(Instant.now().minusMillis(86400000));
        when(refreshTokenRepository.findByToken(ENCODED_REFRESH_TOKEN)).thenReturn(Optional.of(refreshToken));

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(REFRESH_TOKEN));

        verify(refreshTokenRepository, times(1)).findByToken(ENCODED_REFRESH_TOKEN);
        verify(jwtUtility, never()).generateToken(anyString(), anyString());
    }
}