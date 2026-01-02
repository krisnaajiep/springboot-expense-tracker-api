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

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseCategoryResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.mapper.ExpenseCategoryMapper;
import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class for managing expense categories. Currently, it provides methods to retrieve expense category data.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseCategoryService {
    /**
     * Repository for managing persistence operations related to the ExpenseCategory entity.
     * Provides methods to perform CRUD operations and custom queries.
     * This repository is used by the ExpenseCategoryService to interact with the database.
     */
    private final ExpenseCategoryRepository expenseCategoryRepository;


    /**
     * Retrieves an expense category by its unique identifier.
     *
     * @param id The unique identifier of the expense category.
     * @return The ExpenseCategory entity corresponding to the provided id.
     * @throws NotFoundException if no expense category is found with the given id.
     */
    public ExpenseCategory getById(Long id) {
        return expenseCategoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Expense category not found with ID: " + id)
        );
    }

    /**
     * Retrieves all expense categories from the repository and maps them to a list of {@link ExpenseCategoryResponseDto}.
     *
     * @return A list of ExpenseCategoryResponseDto representing all expense categories.
     */
    public List<ExpenseCategoryResponseDto> findAll() {
        List<ExpenseCategory> categories = expenseCategoryRepository.findAll();

        return categories.stream()
                .map(ExpenseCategoryMapper::toExpenseCategoryResponseDto)
                .toList();
    }
}
