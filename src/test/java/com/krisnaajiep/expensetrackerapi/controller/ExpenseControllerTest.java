package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtility jwtUtility;

    private User user;
    private Expense anotherExpense;
    private String accessToken;

    private final ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();

    private static final String EXPENSE_DESCRIPTION = "Weekly grocery shopping";
    private static final String EXPENSE_CATEGORY = "Groceries";
    private static final BigDecimal EXPENSE_AMOUNT = new BigDecimal("150.00");
    private static final LocalDate EXPENSE_DATE = LocalDate.now();

    private static final String USER_NAME = "Test User";
    private static final String USER_EMAIL = "test@user.com";
    private static final String USER_PASSWORD = SecureRandomUtility.generateRandomString(8);

    @BeforeEach
    void setUp() {
        expenseRequestDto.setDescription(EXPENSE_DESCRIPTION);
        expenseRequestDto.setCategory(EXPENSE_CATEGORY);
        expenseRequestDto.setAmount(EXPENSE_AMOUNT);
        expenseRequestDto.setDate(EXPENSE_DATE);

        user = User.builder()
                .name(USER_NAME)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .build();

        User anotherUser = User.builder()
                .name("Another User")
                .email("another@user.com")
                .password(SecureRandomUtility.generateRandomString(8))
                .build();

        anotherExpense = Expense.builder()
                .description("Another expense")
                .category(Expense.Category.fromDisplayName("Utilities"))
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.now())
                .user(anotherUser)
                .build();

        user = userRepository.save(user);

        userRepository.save(anotherUser);
        anotherExpense = expenseRepository.save(anotherExpense);

        accessToken = jwtUtility.generateToken(user.getId().toString(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        expenseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSaveExpenseUnauthorized() throws Exception {
        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    public void testSaveExpenseBadRequest() throws Exception {
        expenseRequestDto.setDescription("");
        expenseRequestDto.setCategory("");
        expenseRequestDto.setAmount(new BigDecimal("100.125")); // Invalid amount with more than 2 decimal places
        expenseRequestDto.setDate(null);

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertTrue(response.containsKey("errors"));
        });
    }

    @Test
    public void testSaveExpenseSuccess() throws Exception {
        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isCreated()
        ).andDo(result -> {
            ExpenseResponseDto response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertNotNull(response.getId());
            assertEquals(EXPENSE_DESCRIPTION, response.getDescription());
            assertEquals(EXPENSE_AMOUNT, response.getAmount());
            assertEquals(EXPENSE_CATEGORY, response.getCategory());
            assertEquals(EXPENSE_DATE, response.getDate());
        });
    }

    @Test
    public void testUpdateExpenseUnauthorized() throws Exception {
        mockMvc.perform(post("/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    public void testUpdateExpenseBadRequest() throws Exception {
        expenseRequestDto.setDescription("");
        expenseRequestDto.setCategory("");
        expenseRequestDto.setAmount(new BigDecimal("100.125")); // Invalid amount with more than 2 decimal places
        expenseRequestDto.setDate(null);

        mockMvc.perform(put("/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertTrue(response.containsKey("errors"));
        });
    }

    @Test
    public void testUpdateExpenseNotFound() throws Exception {
        // Assuming expense with ID 999 does not exist
        long nonExistentExpenseId = 999L;

        mockMvc.perform(put("/expenses/" + nonExistentExpenseId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isNotFound()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Expense not found with ID: " + nonExistentExpenseId, response.get("message"));
        });
    }

    @Test
    public void testUpdateExpenseForbidden() throws Exception {
        mockMvc.perform(put("/expenses/" + anotherExpense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isForbidden()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Forbidden", response.get("message"));
        });
    }

    @Test
    public void testUpdateExpenseSuccess() throws Exception {
        expenseRequestDto.setDescription("Updated description");
        expenseRequestDto.setCategory("Groceries");
        expenseRequestDto.setAmount(new BigDecimal("250.00"));
        expenseRequestDto.setDate(LocalDate.now().plusDays(1));

        Expense expense = Expense.builder()
                .description("New expense")
                .category(Expense.Category.fromDisplayName("Others"))
                .amount(new BigDecimal("300.00"))
                .date(LocalDate.now())
                .user(user)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        mockMvc.perform(put("/expenses/" + savedExpense.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            ExpenseResponseDto response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            System.out.printf("Response: %s%n", response);

            assertNotNull(response);
            assertEquals(savedExpense.getId(), response.getId());
            assertEquals(expenseRequestDto.getDescription(), response.getDescription());
            assertEquals(expenseRequestDto.getAmount(), response.getAmount());
            assertEquals(expenseRequestDto.getCategory(), response.getCategory());
            assertEquals(expenseRequestDto.getDate(), response.getDate());
        });
    }
}