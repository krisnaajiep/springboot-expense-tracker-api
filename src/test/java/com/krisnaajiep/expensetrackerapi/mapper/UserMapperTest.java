package com.krisnaajiep.expensetrackerapi.mapper;

import com.krisnaajiep.expensetrackerapi.dto.request.RegisterRequestDto;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.util.StringUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {
    private final RegisterRequestDto registerRequestDto = new RegisterRequestDto();

    @BeforeEach
    void setUp() {
        registerRequestDto.setName("      John Doe   ");
        registerRequestDto.setEmail("john@doe.com");
        registerRequestDto.setPassword(StringUtility.generatePasswordForTest());
    }

    @Test
    void testTrimUserName() {
        User user = UserMapper.toUser(registerRequestDto);
        assertEquals("John Doe", user.getName());
    }
}