package com.krisnaajiep.expensetrackerapi.service;

import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
class ExpenseServiceCachingIT {
    @MockitoBean
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final List<Expense> expenses = new ArrayList<>();
    private final User user = new User();
    private final LocalDate from = LocalDate.now().minusDays(1);
    private final LocalDate to = LocalDate.now();
    private final Pageable pageable = PageRequest.of(0, 10);
    private final String keysPattern = "expenses::userId=1:*";

    @BeforeEach
    void setUp() {
        // Clear cache
        redisTemplate.delete(redisTemplate.keys(keysPattern));

        // Set user ID
        user.setId(1L);

        // Set expenses
        for (int i = 0; i < 5; i++) {
            Expense expense = Expense.builder()
                    .id(UUID.randomUUID())
                    .user(user)
                    .description("Expense " + (i + 1))
                    .amount(new BigDecimal("100.00").multiply(new BigDecimal((i + 1))))
                    .category(Expense.Category.fromDisplayName("Others"))
                    .date(java.time.LocalDate.now())
                    .build();
            expenses.add(expense);
        }
    }

    @Test
    void testFindAll_UsesCache() {
        when(expenseRepository.findAll(user.getId(), from, to, pageable))
                .thenReturn(new PageImpl<>(expenses, pageable, expenses.size()));

        PagedResponseDto<ExpenseResponseDto> cacheMiss = expenseService.findAll(1L, null, from, to, pageable);
        PagedResponseDto<ExpenseResponseDto> cacheHit = expenseService.findAll(1L, null, from, to, pageable);

        assertEquals(cacheMiss.getMetadata().totalElements(), cacheHit.getMetadata().totalElements());
        assertEquals(cacheMiss.getMetadata().size(), cacheHit.getMetadata().size());
        assertEquals(cacheMiss.getMetadata().totalPages(), cacheHit.getMetadata().totalPages());
        assertEquals(cacheMiss.getMetadata().number(), cacheHit.getMetadata().number());
        assertEquals(cacheMiss.getContent().size(), cacheHit.getContent().size());

        for (int i = 0; i < cacheMiss.getContent().size(); i++) {
            ExpenseResponseDto dto = cacheMiss.getContent().get(i);
            Expense expense = expenses.get(i);
            assertEquals(dto.getId(), expense.getId());
            assertEquals(dto.getDescription(), expense.getDescription());
            assertEquals(dto.getAmount(), expense.getAmount());
            assertEquals(dto.getCategory(), expense.getCategory().getDisplayName());
            assertEquals(dto.getDate(), expense.getDate());
        }

        verify(expenseRepository, times(1)).findAll(user.getId(), from, to, pageable);
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testSave_EvictsCache() {
        when(expenseRepository.findAll(user.getId(), from, to, pageable))
                .thenReturn(new PageImpl<>(expenses, pageable, expenses.size()));

        expenseService.findAll(1L, null, from, to, pageable);

        assertEquals(1, redisTemplate.keys(keysPattern).size());

        when(expenseRepository.save(any(Expense.class))).thenReturn(expenses.getFirst());

        expenseService.save(expenses.getFirst());

        assertEquals(0, redisTemplate.keys(keysPattern).size());

        verify(expenseRepository, times(1)).findAll(user.getId(), from, to, pageable);
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testUpdate_EvictsCache() {
        when(expenseRepository.findAll(user.getId(), from, to, pageable))
                .thenReturn(new PageImpl<>(expenses, pageable, expenses.size()));

        expenseService.findAll(user.getId(), null, from, to, pageable);

        assertEquals(1, redisTemplate.keys(keysPattern).size());

        when(expenseRepository.findById(any(UUID.class))).thenReturn(Optional.of(expenses.getFirst()));

        expenseService.update(expenses.getFirst().getId(), expenses.getFirst());

        assertEquals(0, redisTemplate.keys(keysPattern).size());

        verify(expenseRepository, times(1)).findAll(user.getId(), from, to, pageable);
        verify(expenseRepository, times(1)).findById(any(UUID.class));
        verifyNoMoreInteractions(expenseRepository);
    }

    @Test
    void testDelete_EvictsCache() {
        when(expenseRepository.findAll(user.getId(), from, to, pageable))
                .thenReturn(new PageImpl<>(expenses, pageable, expenses.size()));

        expenseService.findAll(user.getId(), null, from, to, pageable);

        assertEquals(1, redisTemplate.keys(keysPattern).size());

        when(expenseRepository.findById(any(UUID.class))).thenReturn(Optional.of(expenses.getFirst()));

        expenseService.delete(user.getId(), expenses.getFirst().getId());

        assertEquals(0, redisTemplate.keys(keysPattern).size());

        verify(expenseRepository, times(1)).findAll(user.getId(), from, to, pageable);
        verify(expenseRepository, times(1)).findById(any(UUID.class));
        verify(expenseRepository, times(1)).delete(expenses.getFirst());
        verifyNoMoreInteractions(expenseRepository);
    }
}