package com.krisnaajiep.expensetrackerapi.mapper;

import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    @BeforeEach
    void setUp() {
        registerRequestDto.setName("      John Doe   ");
        registerRequestDto.setEmail("john@doe.com");
        registerRequestDto.setPassword(SecureRandomUtility.generateRandomString(8) + "1_2");
    }

    @Test
    void testTrimUserName() {
        User user = UserMapper.toUser(registerRequestDto);
        assertEquals("John Doe", user.getName());
    }
}