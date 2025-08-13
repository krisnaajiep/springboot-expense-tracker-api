package com.krisnaajiep.expensetrackerapi.dto.request;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 11/08/25 15.10
@Last Modified 11/08/25 15.10
Version 1.0
*/

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestDtoTest<T> {
    protected static Validator validator;

    @BeforeAll
    static void init() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    protected void assertHasViolation(
            Set<ConstraintViolation<T>> violations,
            String key,
            String message
    ) {
        assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals(key) &&
                        violation.getMessage().equals(message)
        ));
    }
}
