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

import com.krisnaajiep.expensetrackerapi.config.CacheConfig;
import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseFilter;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.mapper.ExpenseMapper;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 * Service class for managing expenses. This class provides methods
 * for creating, updating, deleting, and fetching user expenses.
 * <p>
 * Each method includes business logic to ensure that expenses are
 * processed securely and correctly, such as enforcing ownership checks
 * and applying filters.
 */
@Service
@RequiredArgsConstructor
@EnableCaching
public class ExpenseService {
    private static final Logger log = LoggerFactory.getLogger(ExpenseService.class);

    /**
     * Repository for managing persistence operations related to the Expense entity.
     * Provides methods to perform CRUD operations and custom queries.
     * <p>
     * This repository is used by the ExpenseService to interact with the database,
     * such as saving, updating, deleting, and retrieving Expense entities with filtering
     * and pagination support.
     */
    private final ExpenseRepository expenseRepository;

    /**
     * A RedisTemplate bean used for performing Redis operations.
     * It provides a powerful and flexible way to interact with a Redis data store,
     * enabling storage and retrieval of key-value pairs.
     * <p>
     * This instance of RedisTemplate is configured for operations with a String key type
     * and an Object value type, making it suitable for storing and retrieving diverse
     * data structures, including serialized objects.
     * <p>
     * Common operations performed using this RedisTemplate include saving, retrieving,
     * updating, and deleting caching data, as well as performing pattern-based key operations.
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Saves the provided expense entity into the database and converts it into a DTO representation.
     *
     * @param expense the expense entity to be persisted
     * @return a DTO representation of the saved expense
     */
    @Transactional
    public ExpenseResponseDto save(Expense expense) {
        // Save the entity using expenseRepository
        Expense savedExpense = expenseRepository.save(expense);

        log.info("Saved expense with ID: {}", savedExpense.getId());

        // Delete all expenses cache by user ID if exists
        evictFromCache(expense.getUser().getId());

        // Convert the saved entity back to ExpenseResponseDto and return it
        return ExpenseMapper.toExpenseResponseDto(savedExpense);
    }

    /**
     * Updates an existing expense with new values provided in the given expense object.
     * Validates that the expense exists and the user associated with it matches the user in the given expense.
     *
     * @param expenseId the ID of the expense to be updated
     * @param expense the new expense object containing updated details
     * @return a DTO representation of the updated expense
     * @throws NotFoundException if the expense with the given ID does not exist
     * @throws AccessDeniedException if the user associated with the existing expense does not match the user in the provided expense
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

        log.info("Updated expense with ID: {}", existingExpense.getId());

        // Delete all expenses cache by user ID if exists
        evictFromCache(expense.getUser().getId());

        // Convert the updated entity back to ExpenseResponseDto and return it
        return ExpenseMapper.toExpenseResponseDto(existingExpense);
    }

    /**
     * Deletes an expense entry associated with a specific user.
     * Validates that the expense exists and that the user is the owner of the expense
     * before performing the deletion.
     *
     * @param userId the ID of the user performing the deletion
     * @param expenseId the ID of the expense to be deleted
     * @throws NotFoundException if the expense with the given ID does not exist
     * @throws AccessDeniedException if the user attempting deletion is not the owner of the expense
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

        // Delete all expenses cache by user ID if exists
        expenseRepository.delete(existingExpense);

        log.info("Deleted expense with ID: {}", expenseId);

        // Delete expenses cache if exists
        evictFromCache(userId);
    }

    /**
     * Retrieves a paginated list of expense records for a specified user, filtered by date range and other optional filters.
     * Save the result into a cache and converts it into a {@link PagedResponseDto} of {@link ExpenseResponseDto} objects.
     *
     * @param userId the ID of the user whose expenses are to be retrieved
     * @param filter an optional filter to restrict the date range (e.g., past week, month, etc.)
     * @param from the start date for the date range filter (overridden if filter is provided)
     * @param to the end date for the date range filter (overridden if filter is provided)
     * @param pageable the pagination and sorting information
     * @return a paginated list of ExpenseResponseDto objects representing the user's expenses
     */
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.EXPENSES, keyGenerator = "expensesKeyGenerator")
    public PagedResponseDto<ExpenseResponseDto> findAll(
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

        log.info("Fetched {} expenses for user with ID: {}", expenses.getTotalElements(), userId);

        // Convert the Page of Expense entities to a Page of ExpenseResponseDto
        Page<ExpenseResponseDto> page = expenses.map(ExpenseMapper::toExpenseResponseDto);

        // Convert the Page of ExpenseResponseDto to a PagedResponseDto of ExpenseResponseDto
        return ExpenseMapper.toPagedResponseDto(page);
    }

    /**
     * Evicts cache entries related to the specified user's expenses from the Redis cache.
     * This method constructs a cache key pattern based on the user ID and removes
     * all matching entries from the cache. If no matching keys are found, it logs an informational message.
     *
     * @param userId the ID of the user whose expense cache entries should be removed
     */
    private void evictFromCache(Long userId) {
        String pattern = CacheConfig.EXPENSES + "::userId=" + userId + ":*";
        Set<String> keysToDelete = redisTemplate.keys(pattern);
        if (keysToDelete.isEmpty()) {
            log.info("No expenses cache keys found for pattern: {}", pattern);
        } else {
            log.info("Deleting {} expenses cache for keys: {}", keysToDelete.size(), keysToDelete);
            redisTemplate.delete(keysToDelete);
        }
    }
}
