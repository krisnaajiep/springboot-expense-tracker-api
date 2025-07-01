package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private final User user = new User();
    private final Expense expense = new Expense();

    @BeforeEach
    void setUp() {
        user.setId(1L);
        user.setEmail("john@doe");
        user.setPassword("password123");
        user.setName("John Doe");

        expense.setId(1L);
        expense.setDescription("Test Expense");
        expense.setAmount(new BigDecimal("100.00"));
        expense.setCategory(Expense.Category.fromDisplayName("Others"));
        expense.setDate(LocalDate.now());
        expense.setUser(user);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testSaveSuccess() {
        when(expenseRepository.save(expense)).thenReturn(expense);

        ExpenseResponseDto response = expenseService.save(expense);

        assertNotNull(response);
        assertEquals(expense.getId(), response.getId());
        assertEquals(expense.getDescription(), response.getDescription());
        assertEquals(expense.getAmount(), response.getAmount());
        assertEquals(expense.getCategory().getDisplayName(), response.getCategory());
        assertEquals(expense.getDate(), response.getDate());

        verify(expenseRepository, times(1)).save(expense);
    }
}