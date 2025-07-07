package com.krisnaajiep.expensetrackerapi.filter;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 07/07/25 13.12
@Last Modified 07/07/25 13.12
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.config.ThrottlingConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BlockingBucket;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ThrottlingFilter extends OncePerRequestFilter {
    private final ConcurrentMap<String, BlockingBucket> buckets = new ConcurrentHashMap<>();

    private final long capacity;
    private final long refillAmount;
    private final Duration refillDuration;

    public ThrottlingFilter(ThrottlingConfig config) {
        capacity = config.getCapacity();
        refillAmount = config.getRefillAmount();
        refillDuration = Duration.ofMillis(config.getRefillDuration());
    }

    private BlockingBucket createBucket(String clientIp) {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillAmount, refillDuration)
                .build();

        BlockingBucket bucket = Bucket.builder().addLimit(bandwidth).build().asBlocking();

        return buckets.computeIfAbsent(clientIp, ip -> bucket);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        BlockingBucket bucket = createBucket(clientIp);

        try {
            bucket.consume(1);
            filterChain.doFilter(request, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            response.setStatus(503);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Service unavailable\"}");
        }
    }
}
