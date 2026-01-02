package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseCategoryResponseDto;
import com.krisnaajiep.expensetrackerapi.handler.exception.NotFoundException;
import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseCategoryRepository;
import com.krisnaajiep.expensetrackerapi.util.TestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCategoryServiceTest {
    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    @InjectMocks
    private ExpenseCategoryService expenseCategoryService;

    private final List<ExpenseCategory> expenseCategories = new ArrayList<>();

    @BeforeEach
    void setUp() {
        for (String categoryName : TestConstants.EXPENSE_CATEGORY_NAMES) {
            expenseCategories.add(ExpenseCategory.builder().name(categoryName).build());
        }
    }

    @Test
    void testGetById_CategoryNotFound() {
        when(expenseCategoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> expenseCategoryService.getById(anyLong()));

        verify(expenseCategoryRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(expenseCategoryRepository);
    }

    @Test
    void testGetById_Success() {
        Long id = expenseCategories.getFirst().getId();

        when(expenseCategoryRepository.findById(id)).thenReturn(Optional.of(expenseCategories.getFirst()));

        ExpenseCategory response = expenseCategoryService.getById(id);

        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(expenseCategories.getFirst().getName(), response.getName());

        verify(expenseCategoryRepository, times(1)).findById(id);
    }

    @Test
    void testFindAll_Success() {
        when(expenseCategoryRepository.findAll()).thenReturn(expenseCategories);

        List<ExpenseCategoryResponseDto> response = expenseCategoryService.findAll();

        assertEquals(expenseCategories.size(), response.size());

        for (int i = 0; i < expenseCategories.size(); i++) {
            assertEquals(expenseCategories.get(i).getId(), response.get(i).getId());
            assertEquals(expenseCategories.get(i).getName(), response.get(i).getName());
        }

        verify(expenseCategoryRepository, times(1)).findAll();
    }
}