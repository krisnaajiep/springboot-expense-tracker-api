package com.krisnaajiep.expensetrackerapi.service;

/*
IntelliJ IDEA 2025.1 (Ultimate Edition)
Build #IU-251.23774.435, built on April 14, 2025
@Author krisna a.k.a. Krisna Ajie
Java Developer
Created on 30/06/25 02.15
@Last Modified 30/06/25 02.15
Version 1.0
*/

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.mapper.ExpenseMapper;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    /**
     * Saves an expense for the given user.
     *
     * @param expense the expense to be saved
     * @return the saved expense as a response DTO
     */
    @Transactional
    public ExpenseResponseDto save(Expense expense) {
        // Save the entity using expenseRepository
        Expense savedExpense = expenseRepository.save(expense);

        // Convert the saved entity back to ExpenseResponseDto and return it
        return ExpenseMapper.toExpenseResponseDto(savedExpense);
    }
}
