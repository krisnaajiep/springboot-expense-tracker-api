package com.krisnaajiep.expensetrackerapi.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 31/07/25 01.17
@Last Modified 31/07/25 01.17
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class CacheConfig {
    public static final String EXPENSES = "expenses";

    private final RedisSerializer<Object> redisSerializer;

    @Bean
    public RedisCacheManagerBuilderCustomizer cacheManagerBuilderCustomizer() {
        return builder -> builder.withCacheConfiguration(EXPENSES, cacheConfiguration());
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer));
    }

    @Bean
    public SimpleKeyGenerator expensesKeyGenerator() {
        return new SimpleKeyGenerator() {
            @NonNull
            @Override
            public Object generate(@NonNull Object target, @NonNull Method method, @NonNull Object... params) {
                long userId = (long) params[0];
                ExpenseFilter filter = (ExpenseFilter) params[1];
                LocalDate from = (LocalDate) params[2];
                LocalDate to = (LocalDate) params[3];
                Pageable pageable = (Pageable) params[4];

                return String.format(
                        "userId=%d:filter=%s&from=%s&to=%s&page=%d&size=%d&sort=%s",
                        userId, filter, from, to, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()
                );
            }
        };
    }
}
