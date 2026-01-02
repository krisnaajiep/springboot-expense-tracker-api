package com.krisnaajiep.expensetrackerapi.mapper;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.15
@Last Modified 30/06/25 02.15
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseCategoryResponseDto;
import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;

public class ExpenseCategoryMapper {
    public static ExpenseCategoryResponseDto toExpenseCategoryResponseDto(ExpenseCategory expenseCategory) {
        return new ExpenseCategoryResponseDto(
                expenseCategory.getId(),
                expenseCategory.getName()
        );
    }
}
