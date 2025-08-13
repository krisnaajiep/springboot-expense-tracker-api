package com.krisnaajiep.expensetrackerapi.util;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 29/06/25 09.21
@Last Modified 29/06/25 09.21
Version 1.0
*/

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.util.Base64;

public class StringUtility {
    public static String generateSecureToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }

    public static String generateRandomString(int length) {
        return RandomStringUtils.insecure().nextAlphanumeric(length);
    }

    public static String generatePasswordForTest() {
        return generateRandomString(4) + "Ab_1";
    }
}
