package com.krisnaajiep.expensetrackerapi.util;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 06/07/25 15.26
@Last Modified 06/07/25 15.26
Version 1.0
*/

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;

import java.lang.reflect.Field;

public class ValidationUtility {
    private static final Logger log = LoggerFactory.getLogger(ValidationUtility.class);

    public static String getJsonPropertyName(FieldError error, Class<?> dtoClass) {
        try {
            Field field = dtoClass.getDeclaredField(error.getField());
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            return (jsonProperty != null && !jsonProperty.value().isBlank())
                    ? jsonProperty.value()
                    : error.getField();
        } catch (Exception e) {
            log.error("Error while getting json property name: {}", error.getField(), e);
            return error.getField();
        }
    }
}
