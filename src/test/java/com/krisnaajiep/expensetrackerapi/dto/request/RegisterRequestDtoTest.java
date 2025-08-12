package com.krisnaajiep.expensetrackerapi.dto.request;

import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import com.krisnaajiep.expensetrackerapi.util.ValidationMessages;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestDtoTest extends RequestDtoTest<RegisterRequestDto> {
    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    @BeforeEach
    void setUp() {
        registerRequestDto.setName("John Doe");
        registerRequestDto.setEmail("john@doe.com");
        registerRequestDto.setPassword(SecureRandomUtility.generateRandomString(8) + "1_2");
    }

    @Test
    void testNullInputs_ValidationErrors() {
        registerRequestDto.setName(null);
        registerRequestDto.setEmail(null);
        registerRequestDto.setPassword(null);
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(registerRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "name", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "email", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "password", ValidationMessages.NOT_BLANK_MESSAGE);
    }

    @Test
    void testBlankInputs_ValidationErrors() {
        registerRequestDto.setName(" ");
        registerRequestDto.setEmail(" ");
        registerRequestDto.setPassword(" ");
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(registerRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "name", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "email", ValidationMessages.NOT_BLANK_MESSAGE);
        assertHasViolation(violations, "password", ValidationMessages.NOT_BLANK_MESSAGE);
    }

    @Test
    void testMaxSizeInputs_ValidationErrors() {
        registerRequestDto.setName(SecureRandomUtility.generateRandomString(256));
        registerRequestDto.setEmail(SecureRandomUtility.generateRandomString(256) + "@email.com");
        registerRequestDto.setPassword(SecureRandomUtility.generateRandomString(256) + "1_2");
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(registerRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "name", ValidationMessages.sizeMessage(1));
        assertHasViolation(violations, "email", ValidationMessages.sizeMessage(1));
        assertHasViolation(violations, "password", ValidationMessages.sizeMessage(8));
    }

    @Test
    void testInvalidEmail_ValidationErrors() {
        registerRequestDto.setEmail("john_doe.com");
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(registerRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "email", ValidationMessages.EMAIL_MESSAGE);
    }

    @Test
    void testInvalidPatternInputs_ValidationErrors() {
        registerRequestDto.setName("<h1>John Doe</h1>");
        registerRequestDto.setPassword("abc12345");
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(registerRequestDto);

        assertFalse(violations.isEmpty());
        assertHasViolation(violations, "name", ValidationMessages.getValidationMessage("name.Pattern"));
        assertHasViolation(violations, "password", ValidationMessages.getValidationMessage("password.Pattern"));
    }

    @Test
    void testValidInputs_NoErrors() {
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(registerRequestDto);
        assertTrue(violations.isEmpty());
    }
}