package com.krisnaajiep.expensetrackerapi.security.service;

import com.krisnaajiep.expensetrackerapi.security.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {
    private static final String KEY_PREFIX = "login:fail:ip:";
    private static final String FIRST_IP = "192.168.1.1";
    private static final String SECOND_IP = "192.168.1.2";
    private static final int MAX_ATTEMPTS = 5;
    private static final long JAIL_DURATION = 600000L;

    private final AuthProperties authProperties = new AuthProperties();

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HttpServletRequest request;

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        authProperties.setLogin(new AuthProperties.Login());
        authProperties.getLogin().setMaxAttempts(MAX_ATTEMPTS);
        authProperties.getLogin().setJailDuration(JAIL_DURATION);
        loginAttemptService = new LoginAttemptService(redisTemplate, authProperties, request);
    }

    @Test
    void loginSucceeded() {
        loginAttemptService.loginSucceeded(FIRST_IP);
        verify(redisTemplate, times(1)).delete(KEY_PREFIX + FIRST_IP);
    }

    @Test
    void testLoginFailed_1stAttempt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get(KEY_PREFIX + SECOND_IP)).thenReturn(null);

        loginAttemptService.loginFailed(SECOND_IP);

        verify(redisTemplate.opsForValue(), times(1)).get(KEY_PREFIX + SECOND_IP);
        verify(redisTemplate.opsForValue(), times(1))
                .set(KEY_PREFIX + SECOND_IP, 1, Duration.ofMillis(JAIL_DURATION));
    }

    @Test
    void testLoginFailed_6thAttempt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get(KEY_PREFIX + FIRST_IP)).thenReturn(5);

        loginAttemptService.loginFailed(FIRST_IP);

        verify(redisTemplate.opsForValue(), times(1)).get(KEY_PREFIX + FIRST_IP);
        verify(redisTemplate.opsForValue(), times(1))
                .set(KEY_PREFIX + FIRST_IP, 6, Duration.ofMillis(JAIL_DURATION));
    }

    @Test
    void testIsJailed_ReturnTrue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get(KEY_PREFIX + FIRST_IP)).thenReturn(5);
        assertTrue(loginAttemptService.isJailed(FIRST_IP));

    }

    @Test
    void testIsJailed_ReturnFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get(KEY_PREFIX + SECOND_IP)).thenReturn(4);
        assertFalse(loginAttemptService.isJailed(SECOND_IP));
    }

    @Test
    void testGetRemainingJailTime() {
        when(redisTemplate.getExpire(KEY_PREFIX + FIRST_IP, TimeUnit.MILLISECONDS)).thenReturn(600000L);

        assertEquals(600000L, loginAttemptService.getRemainingJailTime(FIRST_IP));

        verify(redisTemplate, times(1))
                .getExpire(KEY_PREFIX + FIRST_IP, TimeUnit.MILLISECONDS);
    }

    @Test
    void testGetClientIp_NullForwardedForHeader() {
        when(request.getRemoteAddr()).thenReturn(FIRST_IP);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        assertEquals(FIRST_IP, loginAttemptService.getClientIp());

        verify(request, times(1)).getRemoteAddr();
        verify(request, times(1)).getHeader("X-Forwarded-For");
    }

    @Test
    void testGetClientIp_EmptyForwardedForHeader() {
        when(request.getRemoteAddr()).thenReturn(FIRST_IP);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");

        assertEquals(FIRST_IP, loginAttemptService.getClientIp());

        verify(request, times(1)).getRemoteAddr();
        verify(request, times(1)).getHeader("X-Forwarded-For");
    }

    @Test
    void testGetClientIp_ValidForwardedForHeader() {
        when(request.getRemoteAddr()).thenReturn(FIRST_IP);
        when(request.getHeader("X-Forwarded-For")).thenReturn(SECOND_IP);

        assertEquals(SECOND_IP, loginAttemptService.getClientIp());

        verify(request, times(1)).getRemoteAddr();
        verify(request, times(1)).getHeader("X-Forwarded-For");
    }
}