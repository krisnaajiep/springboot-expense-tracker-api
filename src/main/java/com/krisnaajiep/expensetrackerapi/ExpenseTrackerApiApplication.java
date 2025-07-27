package com.krisnaajiep.expensetrackerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ExpenseTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApiApplication.class, args);
    }

}
