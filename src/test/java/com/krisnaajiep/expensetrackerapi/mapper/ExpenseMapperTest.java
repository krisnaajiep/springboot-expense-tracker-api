package com.krisnaajiep.expensetrackerapi.mapper;

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseMapperTest {
    private final ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();
    private final User user = User.builder().id(1L).build();

    @BeforeEach
    void setUp() {
        expenseRequestDto.setDescription("  Weekly grocery shopping  ");
        expenseRequestDto.setAmount(new BigDecimal("150.00"));
        expenseRequestDto.setCategory("Groceries");
        expenseRequestDto.setDate(LocalDate.now());
    }

    @Test
    void testTrimDescription() {
        Expense expense = ExpenseMapper.toExpense(user, expenseRequestDto);
        assertEquals("Weekly grocery shopping", expense.getDescription());
    }

    @Test
    void testHtmlEscapeDescription() {
        expenseRequestDto.setDescription("<script>alert('XSS')</script>");
        String expected = HtmlUtils.htmlEscape(expenseRequestDto.getDescription());

        Expense expense = ExpenseMapper.toExpense(user, expenseRequestDto);

        assertNotEquals(expenseRequestDto.getDescription(), expense.getDescription());
        assertEquals(expected, expense.getDescription());
    }
}