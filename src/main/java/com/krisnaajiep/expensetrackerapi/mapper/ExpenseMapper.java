package com.krisnaajiep.expensetrackerapi.mapper;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 07.53
@Last Modified 30/06/25 07.53
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;

public class ExpenseMapper {
    public static Expense toExpense(User user, ExpenseRequestDto expenseRequestDto) {
        return Expense.builder()
                .user(user)
                .description(expenseRequestDto.getDescription())
                .amount(expenseRequestDto.getAmount())
                .category(Expense.Category.fromDisplayName(expenseRequestDto.getCategory()))
                .date(expenseRequestDto.getDate())
                .build();
    }

    public static ExpenseResponseDto toExpenseResponseDto(Expense expense) {
        return new ExpenseResponseDto(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory().getDisplayName(),
                expense.getDate()
        );
    }
}
