package com.krisnaajiep.expensetrackerapi.dto.request;

import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import com.krisnaajiep.expensetrackerapi.util.ValidationMessages;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseRequestDtoTest extends RequestDtoTest<ExpenseRequestDto> {

    private final ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();

    @BeforeEach
    void setUp() {
        expenseRequestDto.setDescription("Weekly grocery shopping");
        expenseRequestDto.setAmount(new BigDecimal("150.00"));
        expenseRequestDto.setCategory("Groceries");
        expenseRequestDto.setDate(LocalDate.now());
    }

    @Test
    void testNullInputs_ValidationErrors() {
        expenseRequestDto.setDescription(null);
        expenseRequestDto.setAmount(null);
        expenseRequestDto.setCategory(null);
        expenseRequestDto.setDate(null);

        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "description", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "amount", ValidationMessages.NOT_NULL_MESSAGE);
        assertHasViolation(violations, "category", ValidationMessages.NOT_NULL_MESSAGE);
        assertHasViolation(violations, "date", ValidationMessages.NOT_NULL_MESSAGE);
    }

    @Test
    void testBlankInputs_ValidationErrors() {
        expenseRequestDto.setDescription(" ");
        expenseRequestDto.setCategory(" ");

        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "description", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "category", ValidationMessages.getValidationMessage("category.Pattern"));
    }

    @Test
    void testMaxSizeDescription_ValidationErrors() {
        expenseRequestDto.setDescription(SecureRandomUtility.generateRandomString(256));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "description", ValidationMessages.sizeMessage(1));
    }

    @Test
    void testDecimalMinAmount_ValidationErrors() {
        expenseRequestDto.setAmount(new BigDecimal("-1"));
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
    void testInvalidPatternCategory_ValidationErrors() {
        expenseRequestDto.setCategory("Invalid category");
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "category", ValidationMessages.getValidationMessage("category.Pattern"));
    }

    @Test
    void testFutureDate_ValidationErrors() {
        expenseRequestDto.setDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "date", ValidationMessages.PAST_OR_PRESENT_MESSAGE);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Groceries", "Leisure", "Electronics", "Utilities", "Clothing", "Health", "Others"})
    void testValidInputs_NoErrors(String category) {
        expenseRequestDto.setCategory(category);
        Set<ConstraintViolation<ExpenseRequestDto>> violations = validator.validate(expenseRequestDto);
        assertTrue(violations.isEmpty());
    }
}