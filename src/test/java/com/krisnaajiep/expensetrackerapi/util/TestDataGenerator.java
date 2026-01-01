package com.krisnaajiep.expensetrackerapi.util;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.15
@Last Modified 30/06/25 02.15
Version 1.0
*/

import com.github.javafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class TestDataGenerator {
    private static final Faker faker = new Faker(Locale.forLanguageTag("id-ID"));
    private static final String nameRegex = "^[\\p{L}\\s,.'-]{1,255}$";
    private static final String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@_#\\-$]).{8,255}$";

    public static Long generateRandomNumber(long min, long max) {
        return faker.number().numberBetween(min, max);
    }

    public static String generateFullName() {
        String name = faker.name().fullName();
        return validateFullName(name);
    }

    public static String generateEmail() {
        return faker.internet().emailAddress();
    }

    public static String generatePassword() {
        String password = faker.internet().password(8, 16, true, true, true);
        return validatePassword(password);
    }

    public static String generateDescription(int wordCount) {
        String description = faker.lorem().sentence(wordCount);
        return validateDescription(description, wordCount);
    }

    public static BigDecimal generateAmount(long min, long max) {
        double amount = faker.number().randomDouble(2, min, max);
        return BigDecimal.valueOf(amount);
    }

    public static LocalDate generateDate(long min, long max) {
        return faker.date()
                .between(
                        Date.from(LocalDate.now().plusDays(min).atStartOfDay(ZoneId.systemDefault()).toInstant()),
                        Date.from(LocalDate.now().plusDays(max).atStartOfDay(ZoneId.systemDefault()).toInstant())
                )
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private static String validateFullName(String name) {
        int loop = 0;

        while(!name.matches(nameRegex)) {
            if (loop > 100) {
                throw new RuntimeException("Failed to generate valid name after 100 attempts");
            }

            name = faker.name().fullName();
            loop++;
        }

        return name;
    }

    private static String validatePassword(String password) {
        int loop = 0;

        while(!password.matches(passwordRegex)) {
            if (loop > 100) {
                throw new RuntimeException("Failed to generate valid password after 100 attempts");
            }

            password = faker.internet().password(8, 16, true, true, true);
            loop++;
        }

        return password;
    }

    private static String validateDescription(String description, int wordCount) {
        int loop = 0;

        while(description.length() > 255) {
            if (loop > 100) {
                throw new RuntimeException("Failed to generate valid description after 100 attempts");
            }

            description = faker.lorem().sentence(wordCount);
            loop++;
        }

        return description;
    }
}
