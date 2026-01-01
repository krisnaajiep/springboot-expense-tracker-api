package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseCategoryRepository;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import com.krisnaajiep.expensetrackerapi.util.StringUtility;
import com.krisnaajiep.expensetrackerapi.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCategoryRepository categoryRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private ExpenseService expenseService;

    private final User user = new User();
    private final User anotherUser = new User();
    private final ExpenseCategory category = new ExpenseCategory();
    private final Expense expense = new Expense();
    private final List<Expense> expenses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        user.setId(1L);
        user.setEmail(TestDataGenerator.generateEmail());
        user.setPassword(StringUtility.generateRandomString(8));
        user.setName(TestDataGenerator.generateFullName());

        anotherUser.setId(2L);
        anotherUser.setEmail(TestDataGenerator.generateEmail());
        anotherUser.setPassword(StringUtility.generateRandomString(8));
        anotherUser.setName(TestDataGenerator.generateFullName());

        category.setId(TestDataGenerator.generateRandomNumber(1, 1000));
        category.setName("Others");

        expense.setId(UUID.randomUUID());
        expense.setDescription(TestDataGenerator.generateDescription(10));
        expense.setAmount(TestDataGenerator.generateAmount(10, 1000));
        expense.setCategory(category);
        expense.setDate(TestDataGenerator.generateDate(-30, 0));
        expense.setUser(user);
    }

    @Test
    void testSave_CategoryNotFound() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.save(expense));

        verify(categoryRepository, times(1)).findById(category.getId());
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testSave_Success() {
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(redisTemplate.keys(anyString())).thenReturn(Set.of());

        ExpenseResponseDto response = expenseService.save(expense);

        assertNotNull(response);
        assertEquals(expense.getId(), response.getId());
        assertEquals(expense.getDescription(), response.getDescription());
        assertEquals(expense.getAmount(), response.getAmount());
        assertEquals(expense.getCategory().getName(), response.getCategory());
        assertEquals(expense.getDate(), response.getDate());

        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void testUpdate_ExpenseNotFound() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.update(expense.getId(), expense));

        verify(expenseRepository, times(1)).findById(expense.getId());
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testUpdate_CategoryNotFound() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.update(expense.getId(), expense));

        verify(expenseRepository, times(1)).findById(expense.getId());
        verify(categoryRepository, times(1)).findById(category.getId());
        verifyNoMoreInteractions(expenseRepository);
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void testUpdate_AccessDenied() {
        Expense anotherExpense = Expense.builder()
                .id(UUID.randomUUID())
                .description(TestDataGenerator.generateDescription(10))
                .amount(TestDataGenerator.generateAmount(10, 100))
                .category(category)
                .date(TestDataGenerator.generateDate(-30, 0))
                .user(anotherUser)
                .build();

        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));

        assertThrows(AccessDeniedException.class, () -> expenseService.update(expense.getId(), anotherExpense));

        verify(expenseRepository, times(1)).findById(expense.getId());
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testUpdateSuccess() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(redisTemplate.keys(anyString())).thenReturn(Set.of());

        ExpenseResponseDto response = expenseService.update(expense.getId(), expense);

        assertNotNull(response);
        assertEquals(expense.getId(), response.getId());
        assertEquals(expense.getDescription(), response.getDescription());
        assertEquals(expense.getAmount(), response.getAmount());
        assertEquals(expense.getCategory().getName(), response.getCategory());
        assertEquals(expense.getDate(), response.getDate());

        verify(expenseRepository, times(1)).findById(expense.getId());
    }

    @Test
    void testDelete_NotFound() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseService.delete(user.getId(), expense.getId()));

        verify(expenseRepository, times(1)).findById(expense.getId());
    }

    @Test
    void testDelete_AccessDenied() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class, () -> expenseService.delete(anotherUser.getId(), expense.getId()));

        verify(expenseRepository, times(1)).findById(expense.getId());
    }

    @Test
    void testDeleteSuccess() {
        when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
        when(redisTemplate.keys(anyString())).thenReturn(Set.of());

        expenseService.delete(user.getId(), expense.getId());

        verify(expenseRepository, times(1)).findById(expense.getId());

        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void testFindAll_Success() {
        setExpenses();

        int pageNumber = 1; // 0-based index
        int pageSize = 10; // Number of items per page
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(expenseRepository.findAll(user.getId(), null, null, pageable)).thenReturn(
                new PageImpl<>(expenses, pageable, expenses.size())
        );

        PagedResponseDto<ExpenseResponseDto> response = expenseService.findAll(
                user.getId(),
                null,
                null,
                null,
                pageable
        );

        long expectedTotalPages = (long) Math.ceil((double) expenses.size() / pageSize);

        assertNotNull(response);
        assertEquals(expenses.size(), response.getMetadata().totalElements());
        assertEquals(pageSize, response.getMetadata().size());
        assertEquals(expectedTotalPages, response.getMetadata().totalPages());
        assertEquals(pageNumber, response.getMetadata().number());
        assertEquals(expenses.size(), response.getContent().size());

        for (int i = 0; i < expenses.size(); i++) {
            ExpenseResponseDto dto = response.getContent().get(i);
            Expense expense = expenses.get(i);
            assertEquals(expense.getId(), dto.getId());
            assertEquals(expense.getDescription(), dto.getDescription());
            assertEquals(expense.getAmount(), dto.getAmount());
            assertEquals(expense.getCategory().getName(), dto.getCategory());
            assertEquals(expense.getDate(), dto.getDate());
        }

        verify(expenseRepository, times(1)).findAll(user.getId(), null, null, pageable);
    }

    private void setExpenses() {
        expenses.clear();
        for (int i = 0; i < 100; i++) {
            Expense expense = Expense.builder()
                    .id(UUID.randomUUID())
                    .description(TestDataGenerator.generateDescription(10))
                    .amount(TestDataGenerator.generateAmount(10, 1000))
                    .category(category)
                    .date(TestDataGenerator.generateDate(-30, 0))
                    .user(user)
                    .build();
            expenses.add(expense);
        }
    }
}