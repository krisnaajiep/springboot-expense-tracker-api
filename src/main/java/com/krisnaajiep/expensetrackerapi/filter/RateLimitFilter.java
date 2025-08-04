package com.krisnaajiep.expensetrackerapi.filter;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 06/07/25 22.07
@Last Modified 06/07/25 22.07
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final long capacity;
    private final long refillAmount;
    private final Duration refillDuration;

    public RateLimitFilter(RateLimitProperties properties) {
        capacity = properties.getCapacity();
        refillAmount = properties.getRefillAmount();
        refillDuration = Duration.ofMillis(properties.getRefillDuration());
    }

    private Bucket createBucket(String clientIp) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillAmount, refillDuration)
                .build();

        Bucket bucket = Bucket.builder().addLimit(bandwidth).build();

        return buckets.computeIfAbsent(clientIp, ip -> bucket);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        Bucket bucket = createBucket(clientIp);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        long resetTimestamp = Instant.now().plusNanos(probe.getNanosToWaitForRefill()).getEpochSecond();

        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTimestamp));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Rate limit exceeded\"}");
        }
    }
}
