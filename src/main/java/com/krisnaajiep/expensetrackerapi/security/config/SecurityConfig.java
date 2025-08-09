package com.krisnaajiep.expensetrackerapi.security.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/06/25 03.00
@Last Modified 27/06/25 03.00
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.security.JwtAuthenticationFilter;
import com.krisnaajiep.expensetrackerapi.security.JwtAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider,
            JwtAuthenticationProvider jwtAuthenticationProvider,
            AuthenticationEventPublisher eventPublisher
    ) {
        ProviderManager manager = new ProviderManager(jwtAuthenticationProvider, daoAuthenticationProvider);
        manager.setAuthenticationEventPublisher(eventPublisher);
        return manager;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager);
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(false);
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("ETag", "Retry-After"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilter(
            HttpSecurity http,
            JwtAuthenticationFilter authenticationFilter,
            AuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        http
                .cors(Customizer.withDefaults()) // Enable CORS
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions
                .httpBasic(AbstractHttpConfigurer::disable) // Disable basic authentication
                .formLogin(AbstractHttpConfigurer::disable) // Disable form-based authentication
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers( // Allow access to these endpoints
                                "/register",
                                "/login",
                                "/refresh",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()
                        .anyRequest().authenticated()) // Require authentication for all other requests
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class) // Add a JWT authentication filter
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)) // Custom entry point for unauthorized access
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'none'"))); // Add a CSP header to the response

        return http.build();
    }
}
