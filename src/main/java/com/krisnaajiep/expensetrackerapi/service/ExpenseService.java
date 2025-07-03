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

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseFilter;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.mapper.ExpenseMapper;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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

    /**
     * Updates an existing expense.
     *
     * @param expense the expense to be updated
     * @return the updated expense as a response DTO
     */
    @Transactional
    public ExpenseResponseDto update(Long expenseId, Expense expense) {
        // Check if the expense exists
        Expense existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));

        // Ensure the user of the expense matches the user in the provided expense
        if (!existingExpense.getUser().getId().equals(expense.getUser().getId())) {
            throw new AccessDeniedException("User does not match the expense owner.");
        }

        // Update the existing expense with new values
        existingExpense.setDescription(expense.getDescription());
        existingExpense.setAmount(expense.getAmount());
        existingExpense.setCategory(expense.getCategory());
        existingExpense.setDate(expense.getDate());

        // Convert the updated entity back to ExpenseResponseDto and return it
        return ExpenseMapper.toExpenseResponseDto(existingExpense);
    }

    /**
     * Deletes an expense by its ID.
     *
     * @param expenseId the ID of the expense to be deleted
     */
    @Transactional
    public void delete(Long userId, Long expenseId) {
        // Check if the expense exists
        Expense existingExpense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found with ID: " + expenseId));

        // Ensure the user of the expense matches the user in the provided expense
        if (!existingExpense.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("User does not match the expense owner.");
        }

        // Delete the expense
        expenseRepository.delete(existingExpense);
    }

    /**
     * Finds all expenses for a user with optional filtering and pagination.
     *
     * @param userId the ID of the user
     * @param filter the filter criteria (optional)
     * @param from   the start date for filtering (optional)
     * @param to     the end date for filtering (optional)
     * @param pageable pagination information
     * @return a page of expenses as response DTOs
     */
    @Transactional(readOnly = true)
    public Page<ExpenseResponseDto> findAll(
            Long userId, ExpenseFilter filter, LocalDate from, LocalDate to, Pageable pageable
    ) {
        // Filter expenses based on the provided filter
        if (filter != null) {
            ExpenseFilter.DateRange dateRange = filter.resolve();
            from = dateRange.startDate();
            to = dateRange.endDate();
        }

        // Fetch all expenses for the user with pagination
        Page<Expense> expenses = expenseRepository.findAll(userId, from, to, pageable);

        // Convert the Page of Expense entities to a Page of ExpenseResponseDto
        return expenses.map(ExpenseMapper::toExpenseResponseDto);
    }
}
