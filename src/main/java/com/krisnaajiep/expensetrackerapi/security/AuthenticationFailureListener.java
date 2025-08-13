package com.krisnaajiep.expensetrackerapi.security;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 05/08/25 22.35
@Last Modified 05/08/25 22.35
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.security.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFailureListener.class);
    private final LoginAttemptService loginAttemptService;

    @Override
    public void onApplicationEvent(@NonNull AuthenticationFailureBadCredentialsEvent event) {
        String ip = loginAttemptService.getClientIp();
        loginAttemptService.loginFailed(ip);
        log.info("Login failed for ip {} using email: {}", ip, event.getAuthentication().getName());
    }
}
