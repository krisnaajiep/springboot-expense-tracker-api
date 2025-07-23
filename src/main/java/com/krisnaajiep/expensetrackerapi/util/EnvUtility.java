package com.krisnaajiep.expensetrackerapi.util;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 04/07/25 08.13
@Last Modified 04/07/25 08.13
Version 1.0
*/

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.stream.Stream;

public class EnvUtility {
    public static void setEnv() {
        Dotenv dotenv = Dotenv.load();
        Stream.of(
                "SPRING_DATASOURCE_URL",
                        "SPRING_DATASOURCE_USERNAME",
                        "SPRING_DATASOURCE_PASSWORD",
                        "JWT_SECRET"
                ).forEach(envVar -> System.setProperty(envVar, Objects.requireNonNull(dotenv.get(envVar))));
    }
}
