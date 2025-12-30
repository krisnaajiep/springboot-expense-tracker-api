package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseCategoryResponseDto;
import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseCategoryRepository;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.StringUtility;
import com.krisnaajiep.expensetrackerapi.util.TestConstants;
import com.krisnaajiep.expensetrackerapi.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ExpenseCategoryControllerIT {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseCategoryRepository categoryRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String accessToken;
    private List<ExpenseCategory> categories = new ArrayList<>();


    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TestConstants.DATABASE_TABLE_NAMES);

        for (String categoryName : TestConstants.EXPENSE_CATEGORY_NAMES) {
            categories.add(ExpenseCategory.builder().name(categoryName).build());
        }

        categories = categoryRepository.saveAll(categories);

        User user = User.builder()
                .name(TestDataGenerator.generateFullName())
                .email(TestDataGenerator.generateEmail())
                .password(StringUtility.generateRandomString(8))
                .build();

        user = userRepository.save(user);

        accessToken = jwtUtility.generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testFindAll_Success() throws Exception {
        mockMvc.perform(get("/categories")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            List<ExpenseCategoryResponseDto> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            response.forEach(res -> System.out.println(res.toString()));

            assertNotNull(response);
            assertFalse(response.isEmpty());
            assertEquals(categories.size(), response.size());

            for (int i = 0; i < categories.size(); i++) {
                assertEquals(categories.get(i).getId(), response.get(i).getId());
                assertEquals(categories.get(i).getName(), response.get(i).getName());
            }
        });
    }
}