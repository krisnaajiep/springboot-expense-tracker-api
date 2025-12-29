package com.krisnaajiep.expensetrackerapi.mapper;

import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.HtmlUtils;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseMapperTest {
    private final ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();
    private final User user = User.builder().id(1L).build();

    @BeforeEach
    void setUp() {
        expenseRequestDto.setDescription("  Weekly grocery shopping  ");
        expenseRequestDto.setAmount(TestDataGenerator.generateAmount(10, 500));
        expenseRequestDto.setCategoryId(TestDataGenerator.generateRandomNumber(1, 100));
        expenseRequestDto.setDate(TestDataGenerator.generateDate(-30, 0));
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