package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final List<Expense> expenses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user.setId(1L);
        user.setEmail("john@doe");
        user.setPassword(SecureRandomUtility.generateRandomString(8));
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

    @Test
    void testUpdateFailure_ExpenseNotFound() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.update(expense.getId(), expense));

        verify(expenseRepository, times(1)).findById(expense.getId());
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testUpdateFailure_AccessDenied() {
        User anotherUser = User.builder()
                .id(2L)
                .email("jane@doe")
                .password(SecureRandomUtility.generateRandomString(8))
                .name("Jane Doe")
                .build();

        Expense anotherExpense = Expense.builder()
                .id(2L)
                .description("Another Expense")
                .amount(new BigDecimal("200.00"))
                .category(Expense.Category.fromDisplayName("Others"))
                .date(LocalDate.now())
                .user(anotherUser)
                .build();

        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class, () -> expenseService.update(expense.getId(), anotherExpense));

        verify(expenseRepository, times(1)).findById(expense.getId());
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testUpdateSuccess() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        ExpenseResponseDto response = expenseService.update(expense.getId(), expense);

        assertNotNull(response);
        assertEquals(expense.getId(), response.getId());
        assertEquals(expense.getDescription(), response.getDescription());
        assertEquals(expense.getAmount(), response.getAmount());
        assertEquals(expense.getCategory().getDisplayName(), response.getCategory());
        assertEquals(expense.getDate(), response.getDate());

        verify(expenseRepository, times(1)).findById(expense.getId());
    }

    @Test
    void testDeleteFailure_ExpenseNotFound() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.delete(user.getId(), expense.getId()));

        verify(expenseRepository, times(1)).findById(expense.getId());
    }

    @Test
    void testDeleteFailure_AccessDenied() {
        User anotherUser = User.builder()
                .id(2L)
                .email("jane@doe")
                .password(SecureRandomUtility.generateRandomString(8))
                .name("Jane Doe")
                .build();

        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class, () -> expenseService.delete(anotherUser.getId(), expense.getId()));

        verify(expenseRepository, times(1)).findById(expense.getId());
    }

    @Test
    void testDeleteSuccess() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        expenseService.delete(user.getId(), expense.getId());

        verify(expenseRepository, times(1)).findById(expense.getId());

        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void testFindAllSuccess() {
        setExpenses();

        int pageNumber = 1; // 0-based index
        int pageSize = 10; // Number of items per page
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(expenseRepository.findAll(user.getId(), null, null, pageable)).thenReturn(
                new PageImpl<>(expenses, pageable, expenses.size())
        );

        Page<ExpenseResponseDto> response = expenseService.findAll(
                user.getId(),
                null,
                null,
                null,
                pageable
        );

        long expectedTotalPages = (long) Math.ceil((double) expenses.size() / pageSize);

        assertNotNull(response);
        assertEquals(expenses.size(), response.getTotalElements());
        assertEquals(pageSize, response.getSize());
        assertEquals(expectedTotalPages, response.getTotalPages());
        assertEquals(pageNumber, response.getNumber());
        assertEquals(expenses.size(), response.getContent().size());

        for (int i = 0; i < expenses.size(); i++) {
            ExpenseResponseDto dto = response.getContent().get(i);
            Expense expense = expenses.get(i);
            assertEquals(expense.getId(), dto.getId());
            assertEquals(expense.getDescription(), dto.getDescription());
            assertEquals(expense.getAmount(), dto.getAmount());
            assertEquals(expense.getCategory().getDisplayName(), dto.getCategory());
            assertEquals(expense.getDate(), dto.getDate());
        }

        verify(expenseRepository, times(1)).findAll(user.getId(), null, null, pageable);
    }

    private void setExpenses() {
        expenses.clear();
        for (int i = 0; i < 100; i++) {
            Expense expense = Expense.builder()
                    .id((long) (i + 1))
                    .description("Expense " + (i + 1))
                    .amount(new BigDecimal("100.00"))
                    .category(Expense.Category.fromDisplayName("Others"))
                    .date(LocalDate.now())
                    .user(user)
                    .build();
            expenses.add(expense);
        }
    }
}