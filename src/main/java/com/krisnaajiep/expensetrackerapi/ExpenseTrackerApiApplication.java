package com.krisnaajiep.expensetrackerapi;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ExpenseTrackerApiApplication {

    public static void main(String[] args) {
        // Load environment variables into System properties
        Dotenv.configure().ignoreIfMissing().systemProperties().load();

        SpringApplication.run(ExpenseTrackerApiApplication.class, args);
    }

}
