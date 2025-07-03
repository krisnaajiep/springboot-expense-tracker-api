package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private final List<Expense> expenses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        expenseRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .name("Test User")
                .email("test@user.com")
                .password(SecureRandomUtility.generateRandomString(8))
                .build();

        user = userRepository.save(user);

        accessToken = jwtUtility.generateToken(user.getId().toString(), user.getEmail());
    }

    @AfterEach
    void tearDown() {
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
        setInvalidExpenseRequest(); // Set up an invalid expense request

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
        expenseRequestDto.setDescription("Weekly grocery shopping");
        expenseRequestDto.setCategory("Groceries");
        expenseRequestDto.setAmount(new BigDecimal("150.00"));
        expenseRequestDto.setDate(LocalDate.now());

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
            assertEquals(expenseRequestDto.getDescription(), response.getDescription());
            assertEquals(expenseRequestDto.getAmount(), response.getAmount());
            assertEquals(expenseRequestDto.getCategory(), response.getCategory());
            assertEquals(expenseRequestDto.getDate(), response.getDate());
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
        setInvalidExpenseRequest(); // Set up an invalid expense request for updating

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
        setUpdateExpenseRequest(); // Set up the request DTO for updating an expense
        long nonExistentExpenseId = 999L; // Assuming expense with ID 999 does not exist

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
        setUpdateExpenseRequest(); // Set up the request DTO for updating an expense
        setAnotherExpense(); // Set up another expense for a different user

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
        setUpdateExpenseRequest(); // Set up the request DTO for updating an expense

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

    @Test
    public void testDeleteExpenseUnauthorized() throws Exception {
        mockMvc.perform(delete("/expenses/1")
                .accept(MediaType.ALL)
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
    public void testDeleteExpenseNotFound() throws Exception {
        // Assuming expense with ID 999 does not exist
        long nonExistentExpenseId = 999L;

        mockMvc.perform(delete("/expenses/" + nonExistentExpenseId)
                .accept(MediaType.ALL)
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
    public void testDeleteExpenseForbidden() throws Exception {
        setAnotherExpense(); // Set up another expense for a different user

        mockMvc.perform(delete("/expenses/" + anotherExpense.getId())
                .accept(MediaType.ALL)
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
    public void testDeleteExpenseSuccess() throws Exception {
        Expense expense = Expense.builder()
                .description("Expense to be deleted")
                .category(Expense.Category.fromDisplayName("Clothing"))
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.now())
                .user(user)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        mockMvc.perform(delete("/expenses/" + savedExpense.getId())
                .accept(MediaType.ALL)
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isNoContent()
        );

        assertFalse(expenseRepository.existsById(savedExpense.getId()));
    }

    @Test
    public void testFindAllExpensesUnauthorized() throws Exception {
        mockMvc.perform(get("/expenses")
                .accept(MediaType.APPLICATION_JSON)
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
    public void testFindAllExpensesSuccess() throws Exception {
        setExpenses();

        mockMvc.perform(get("/expenses?page=1&size=15&filter=past_week")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            PagedResponseDto<ExpenseResponseDto> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);

            System.out.printf("Content: %s%n", response.getContent());
            System.out.printf("Metadata: %s%n", response.getMetadata());

            assertFalse(response.getContent().isEmpty());
            assertEquals(5, response.getContent().size());

            assertNotNull(response.getMetadata());
            assertEquals(20, response.getMetadata().totalElements());

            Optional<ExpenseResponseDto> notPastWeekExpense = response.getContent().stream()
                    .filter(exp ->
                            exp.getDate().isBefore(LocalDate.now().minusDays(7))
                    ).findFirst();

            assertNull(notPastWeekExpense.orElse(null));
        });
    }

    private void setInvalidExpenseRequest() {
        expenseRequestDto.setDescription("");
        expenseRequestDto.setCategory("");
        expenseRequestDto.setAmount(new BigDecimal("100.125")); // Invalid amount with more than 2 decimal places
        expenseRequestDto.setDate(null);
    }

    private void setUpdateExpenseRequest() {
        expenseRequestDto.setDescription("Updated expense description");
        expenseRequestDto.setCategory("Others");
        expenseRequestDto.setAmount(new BigDecimal("300.00"));
        expenseRequestDto.setDate(LocalDate.now().plusDays(1));
    }

    private void setAnotherExpense() {
        User anotherUser = User.builder()
                .name("Another User")
                .email("another@user.com")
                .password(SecureRandomUtility.generateRandomString(8))
                .build();

        anotherUser = userRepository.save(anotherUser);

        anotherExpense = Expense.builder()
                .description("Another expense")
                .category(Expense.Category.fromDisplayName("Utilities"))
                .amount(new BigDecimal("200.00"))
                .date(LocalDate.now())
                .user(anotherUser)
                .build();

        anotherExpense = expenseRepository.save(anotherExpense);
    }

    private void setExpenses() {
        for (int i = 0; i < 100; i++) {
            LocalDate date;
            if (i < 20) {
                date = LocalDate.now().minusDays(6);
            } else if (i < 60) {
                date = LocalDate.now().minusDays(20);
            } else {
                date = LocalDate.now().minusDays(80);
            }

            Expense expense = Expense.builder()
                    .description("Expense " + (i + 1))
                    .amount(new BigDecimal(125 + i))
                    .category(Expense.Category.fromDisplayName("Others"))
                    .date(date)
                    .user(user)
                    .build();
            expenses.add(expense);
        }

        expenseRepository.saveAll(expenses);
    }
}