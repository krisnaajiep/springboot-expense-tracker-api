package com.krisnaajiep.expensetrackerapi.security.service;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 05/08/25 22.02
@Last Modified 05/08/25 22.02
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.security.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private static final String KEY_PREFIX = "login:fail:ip:";
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final Integer maxAttempts;
    private final Duration jailDuration;
    private final HttpServletRequest request;

    public LoginAttemptService(
            RedisTemplate<String, Object> redisTemplate,
            AuthProperties authProperties,
            HttpServletRequest request
    ) {
        this.redisTemplate = redisTemplate;
        this.maxAttempts = authProperties.getLogin().getMaxAttempts();
        this.jailDuration = Duration.ofMillis(authProperties.getLogin().getJailDuration());
        this.request = request;
    }

    public void loginSucceeded(String ip) {
        redisTemplate.delete(KEY_PREFIX + ip);
    }

    public void loginFailed(String ip) {
        Integer attempts = getAttempts(ip);
        attempts = attempts == null ? 1 : attempts + 1;
        redisTemplate.opsForValue().set(KEY_PREFIX + ip, attempts, jailDuration);

        if (attempts >= maxAttempts) {
            log.info("Login failed for ip {} after {} attempts", ip, attempts);
        }
    }

    public boolean isJailed(String ip) {
        Integer attempts = getAttempts(ip);
        return attempts != null && attempts >= maxAttempts;
    }

    public long getRemainingJailTime(String ip) {
        return redisTemplate.getExpire(KEY_PREFIX + ip, TimeUnit.MILLISECONDS);
    }

    private Integer getAttempts(String ip) {
        return (Integer) redisTemplate.opsForValue().get(KEY_PREFIX + ip);
    }

    public String getClientIp() {
        String ip = request.getRemoteAddr();
        String forwardedForHeader = request.getHeader("X-Forwarded-For");

        if (forwardedForHeader != null && !forwardedForHeader.isBlank()) {
            ip = forwardedForHeader.split(",")[0].trim();
        }

        return ip;
    }
}
