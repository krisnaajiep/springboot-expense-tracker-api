package com.krisnaajiep.expensetrackerapi.dto.request;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.33
@Last Modified 30/06/25 02.33
Version 1.0
*/

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequestDto {
    @NotBlank
    @Size(max = 255)
    private String description;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 38, fraction = 2)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^(Groceries|Leisure|Electronics|Utilities|Clothing|Health|Others)$",
             message = "Category must be one of: Groceries, Leisure, Electronics, Utilities, Clothing, Health, Others")
    private String category;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date; // Format: YYYY-MM-DD
}
