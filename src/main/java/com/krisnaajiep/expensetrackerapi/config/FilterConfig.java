package com.krisnaajiep.expensetrackerapi.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 07/07/25 08.50
@Last Modified 07/07/25 08.50
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.filter.RateLimitFilter;
import com.krisnaajiep.expensetrackerapi.filter.ThrottlingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<ThrottlingFilter> registerThrottlingFilter(ThrottlingFilter filter) {
        FilterRegistrationBean<ThrottlingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> registerRateLimitFilter(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(2);
        return registration;
    }
}
