package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import com.krisnaajiep.expensetrackerapi.repository.RefreshTokenRepository;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.SecureRandomUtility;
import com.krisnaajiep.expensetrackerapi.util.ValidationMessages;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ExpenseControllerIT {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
    private final Map<String, Object> invalidExpenseRequest = new HashMap<>();
    private final List<Expense> expenses = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        expenseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder()
                .name("Test User")
                .email("test@user.com")
                .password(SecureRandomUtility.generateRandomString(8))
                .build();

        user = userRepository.save(user);

        accessToken = jwtUtility.generateToken(user.getId().toString(), user.getEmail());
    }

    @Test
    void testSave_Unauthorized() throws Exception {
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

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    void testSave_ValidationErrors() throws Exception {
        setInvalidExpenseRequest(); // Set up an invalid expense request

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(invalidExpenseRequest))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("errors"));
            assertEquals(4, ((Map<?, ?>) response.get("errors")).size());
            assertEquals(ValidationMessages.NOT_BLANK_MESSAGE, ((Map<?, ?>) response.get("errors")).get("description"));
            assertEquals(ValidationMessages.DECIMAL_MIN_MESSAGE, ((Map<?, ?>) response.get("errors")).get("amount"));
            assertEquals(
                    ValidationMessages.getValidationMessage("category.Pattern"),
                    ((Map<?, ?>) response.get("errors")).get("category")
            );
            assertEquals(ValidationMessages.PAST_OR_PRESENT_MESSAGE, ((Map<?, ?>) response.get("errors")).get("date"));
        });
    }

    @Test
    void testSave_InvalidAmountFormat() throws Exception {
        setInvalidExpenseRequest(); // Set up an invalid expense request
        invalidExpenseRequest.put("amount", "Invalid amount format");

        mockMvc.perform(post("/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(invalidExpenseRequest))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals(
                    "Invalid format for field: amount, Expected type: BigDecimal",
                    response.get("message")
            );
        });
    }

    @Test
    void testSave_Success() throws Exception {
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

            assertNotNull(response);
            assertNotNull(response.getId());
            assertEquals(expenseRequestDto.getDescription(), response.getDescription());
            assertEquals(expenseRequestDto.getAmount(), response.getAmount());
            assertEquals(expenseRequestDto.getCategory(), response.getCategory());
            assertEquals(expenseRequestDto.getDate(), response.getDate());
        });
    }

    @Test
    void testUpdate_Unauthorized() throws Exception {
        String accessToken = jwtUtility.generateToken(user.getId().toString(), " ");

        mockMvc.perform(post("/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(expenseRequestDto))
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    void testUpdate_ValidationErrors() throws Exception {
        setInvalidExpenseRequest(); // Set up an invalid expense request for updating

        mockMvc.perform(put("/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(invalidExpenseRequest))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertNotNull(response.get("errors"));
            assertEquals(4, ((Map<?, ?>) response.get("errors")).size());
            assertEquals(ValidationMessages.NOT_BLANK_MESSAGE, ((Map<?, ?>) response.get("errors")).get("description"));
            assertEquals(ValidationMessages.DECIMAL_MIN_MESSAGE, ((Map<?, ?>) response.get("errors")).get("amount"));
            assertEquals(
                    ValidationMessages.getValidationMessage("category.Pattern"),
                    ((Map<?, ?>) response.get("errors")).get("category")
            );
            assertEquals(ValidationMessages.PAST_OR_PRESENT_MESSAGE, ((Map<?, ?>) response.get("errors")).get("date"));
        });
    }

    @Test
    void testUpdate_InvalidDateFormat() throws Exception {
        setInvalidExpenseRequest(); // Set up an invalid expense request for updating
        invalidExpenseRequest.put("date", "Invalid date format");

        mockMvc.perform(put("/expenses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.ALL)
                .content(objectMapper.writeValueAsString(invalidExpenseRequest))
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isBadRequest()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals(
                    "Invalid date format: Invalid date format, Expected format: yyyy-MM-dd",
                    response.get("message")
            );
        });
    }

    @Test
    void testUpdate_NotFound() throws Exception {
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

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Expense not found with ID: " + nonExistentExpenseId, response.get("message"));
        });
    }

    @Test
    void testUpdate_Forbidden() throws Exception {
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

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Forbidden", response.get("message"));
        });
    }

    @Test
    void testUpdate_Success() throws Exception {
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

            assertNotNull(response);
            assertEquals(savedExpense.getId(), response.getId());
            assertEquals(expenseRequestDto.getDescription(), response.getDescription());
            assertEquals(expenseRequestDto.getAmount(), response.getAmount());
            assertEquals(expenseRequestDto.getCategory(), response.getCategory());
            assertEquals(expenseRequestDto.getDate(), response.getDate());
        });
    }

    @Test
    void testDelete_Unauthorized() throws Exception {
        mockMvc.perform(delete("/expenses/1")
                .accept(MediaType.ALL)
                .header("Authorization", "Bearer abc")
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    void testDelete_NotFound() throws Exception {
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

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Expense not found with ID: " + nonExistentExpenseId, response.get("message"));
        });
    }

    @Test
    void testDelete_Forbidden() throws Exception {
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

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Forbidden", response.get("message"));
        });
    }

    @Test
    void testDelete_Success() throws Exception {
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
    void testFindAll_Unauthorized() throws Exception {
        mockMvc.perform(get("/expenses")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "abc")
        ).andExpect(
                status().isUnauthorized()
        ).andDo(result -> {
            Map<String, Object> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);
            assertTrue(response.containsKey("message"));
            assertEquals("Unauthorized", response.get("message"));
        });
    }

    @Test
    void testFindAll_Success() throws Exception {
        setExpenses();

        mockMvc.perform(get("/expenses?page=1&size=15&filter=past_week")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken) // Assuming accessToken is set
        ).andExpect(
                status().isOk()
        ).andDo(result -> {
            String contentAsString = result.getResponse().getContentAsString();

            String actualCacheControl = result.getResponse().getHeader("Cache-Control");
            String actualEtag = result.getResponse().getHeader("ETag");
            String expectedCacheControl = "no-cache, must-revalidate, private";
            String expectedEtag = "\"" + DigestUtils.md5Hex(contentAsString)  + "\"";

            assertEquals(expectedCacheControl, actualCacheControl);
            assertEquals(expectedEtag, actualEtag);

            PagedResponseDto<ExpenseResponseDto> response = objectMapper.readValue(
                    contentAsString,
                    new TypeReference<>() {
                    }
            );

            assertNotNull(response);

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
        invalidExpenseRequest.put("description", null);
        invalidExpenseRequest.put("category", "Invalid");
        invalidExpenseRequest.put("amount", new BigDecimal(-100));
        invalidExpenseRequest.put("date", LocalDate.now().plusDays(1));
    }

    private void setUpdateExpenseRequest() {
        expenseRequestDto.setDescription("Updated expense description");
        expenseRequestDto.setCategory("Others");
        expenseRequestDto.setAmount(new BigDecimal("300.00"));
        expenseRequestDto.setDate(LocalDate.now().minusDays(1));
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