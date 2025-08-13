package com.krisnaajiep.expensetrackerapi.dto.response;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.50
@Last Modified 30/06/25 02.50
Version 1.0
*/

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Schema(name = "ExpenseResponse", description = "Expense response body")
public class ExpenseResponseDto {
    @Schema(description = "Expenses ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Expenses description", example = "Purchase of new computer")
    private String description;

    @Schema(description = "Expenses amount", example = "800.00")
    private BigDecimal amount;

    @Schema(description = "Expenses category", example = "Electronics")
    private String category;

    @Schema(description = "Expenses date", example = "2025-06-30")
    private LocalDate date;

    @Override
    public String toString() {
        return "ExpenseResponseDto{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + date +
                '}';
    }
}
