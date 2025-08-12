package com.krisnaajiep.expensetrackerapi.util;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 11/08/25 18.59
@Last Modified 11/08/25 18.59
Version 1.0
*/

import java.util.ResourceBundle;

public class ValidationMessages {
    public static final String NOT_BLANK_MESSAGE = "must not be blank";
    public static final String NOT_NULL_MESSAGE = "must not be null";
    public static final String EMAIL_MESSAGE = "must be a well-formed email address";
    public static final String DECIMAL_MIN_MESSAGE = "must be greater than or equal to 0.01";
    public static final String DIGITS_MESSAGE = "numeric value out of bounds (<38 digits>.<2 digits> expected)";
    public static final String PAST_OR_PRESENT_MESSAGE = "must be a date in the past or in the present";

    public static String sizeMessage(int min) {
        return String.format("size must be between %d and %d", min, 255);
    }

    public static String getValidationMessage(String key) {
        return ResourceBundle.getBundle("ValidationMessages").getString(key);
    }
}
