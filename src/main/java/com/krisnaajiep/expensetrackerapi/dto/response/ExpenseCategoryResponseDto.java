package com.krisnaajiep.expensetrackerapi.dto.response;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.15
@Last Modified 30/06/25 02.15
Version 1.0
*/

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(name = "ExpenseCategoryResponse", description = "Expense category response body")
public class ExpenseCategoryResponseDto {
    @Schema(description = "Expense category ID", example = "1")
    private Long id;

    @Schema(description = "Expense category name", example = "Electronics")
    private String name;

    @Override
    public String toString() {
        return "ExpenseCategoryResponseDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
