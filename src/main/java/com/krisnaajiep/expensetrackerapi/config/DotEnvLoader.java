package com.krisnaajiep.expensetrackerapi.config;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 27/07/25 12.47
@Last Modified 27/07/25 12.47
Version 1.0
*/

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DotEnvLoader implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger log = LoggerFactory.getLogger(DotEnvLoader.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        log.info("Load DotEnv...");

        String filename = event
                .getEnvironment()
                .matchesProfiles("build", "test")
                ? ".env.test"
                : ".env";

        log.info("DotEnv filename: {}", filename);

        // Load environment variables into System properties if the file exists
        Dotenv.configure()
                .filename(filename)
                .ignoreIfMissing()
                .systemProperties()
                .load();
    }
}
