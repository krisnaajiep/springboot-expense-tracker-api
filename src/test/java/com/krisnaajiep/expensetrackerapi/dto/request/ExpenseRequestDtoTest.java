package com.krisnaajiep.expensetrackerapi.dto.request;

import com.krisnaajiep.expensetrackerapi.util.StringUtility;
import com.krisnaajiep.expensetrackerapi.util.TestDataGenerator;
import com.krisnaajiep.expensetrackerapi.util.ValidationMessages;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseRequestDtoTest extends RequestDtoTest<ExpenseRequestDto> {

    private final ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();

    @BeforeEach
    void setUp() {
        expenseRequestDto.setDescription(TestDataGenerator.generateDescription(10));
        expenseRequestDto.setAmount(TestDataGenerator.generateAmount(10, 1000));
        expenseRequestDto.setCategoryId(TestDataGenerator.generateRandomNumber(1, 100));
        expenseRequestDto.setDate(TestDataGenerator.generateDate(-30, 0));
    }

    @Test
    void testNullInputs_ValidationErrors() {
        expenseRequestDto.setDescription(null);
        expenseRequestDto.setAmount(null);
        expenseRequestDto.setCategoryId(null);
        expenseRequestDto.setDate(null);

        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        System.out.println(violations);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "description", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "amount", ValidationMessages.NOT_NULL_MESSAGE);
        assertHasViolation(violations, "categoryId", ValidationMessages.NOT_NULL_MESSAGE);
        assertHasViolation(violations, "date", ValidationMessages.NOT_NULL_MESSAGE);
    }

    @Test
    void testBlankDescription_ValidationErrors() {
        expenseRequestDto.setDescription(" ");
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "description", ValidationMessages.NOT_BLANK_MESSAGE);
    }

    @Test
    void testMaxSizeDescription_ValidationErrors() {
        expenseRequestDto.setDescription(StringUtility.generateRandomString(256));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "description", ValidationMessages.sizeMessage(1));
    }

    @Test
    void testDecimalMinAmount_ValidationErrors() {
        expenseRequestDto.setAmount(TestDataGenerator.generateAmount(-1000, 0));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "amount", ValidationMessages.DECIMAL_MIN_MESSAGE);
    }

    @Test
    void testInvalidAmountDigits_ValidationErrors() {
        expenseRequestDto.setAmount(new BigDecimal("150.000"));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "amount", ValidationMessages.DIGITS_MESSAGE);
    }

    @Test
    void testNegativeCategoryID_ValidationErrors() {
        expenseRequestDto.setCategoryId(TestDataGenerator.generateRandomNumber(-100, 0));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        System.out.println(violations);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "categoryId", ValidationMessages.POSITIVE_MESSAGE);
    }

    @Test
    void testFutureDate_ValidationErrors() {
        expenseRequestDto.setDate(TestDataGenerator.generateDate(1, 30));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "date", ValidationMessages.PAST_OR_PRESENT_MESSAGE);
    }

    @Test
    void testValidInputs_NoErrors() {
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);
        assertTrue(violations.isEmpty());
    }
}