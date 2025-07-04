package com.krisnaajiep.expensetrackerapi;

import com.krisnaajiep.expensetrackerapi.util.EnvUtility;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ExpenseTrackerApiApplication {

    public static void main(String[] args) {
        // Set env
        EnvUtility.setEnv();

        SpringApplication.run(ExpenseTrackerApiApplication.class, args);
    }

}
