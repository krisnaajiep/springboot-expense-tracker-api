package com.krisnaajiep.expensetrackerapi.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 02/07/25 18.53
@Last Modified 02/07/25 18.53
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseFilter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ExpenseFilterConverter implements Converter<String, ExpenseFilter> {
    @Override
    public ExpenseFilter convert(String source) {
        return ExpenseFilter.valueOf(source.toUpperCase());
    }
}
