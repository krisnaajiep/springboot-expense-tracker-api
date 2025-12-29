package com.krisnaajiep.expensetrackerapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krisnaajiep.expensetrackerapi.dto.request.ExpenseRequestDto;
import com.krisnaajiep.expensetrackerapi.dto.response.ExpenseResponseDto;
import com.krisnaajiep.expensetrackerapi.dto.response.PagedResponseDto;
import com.krisnaajiep.expensetrackerapi.model.Expense;
import com.krisnaajiep.expensetrackerapi.model.ExpenseCategory;
import com.krisnaajiep.expensetrackerapi.model.User;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseCategoryRepository;
import com.krisnaajiep.expensetrackerapi.repository.ExpenseRepository;
import com.krisnaajiep.expensetrackerapi.repository.RefreshTokenRepository;
import com.krisnaajiep.expensetrackerapi.repository.UserRepository;
import com.krisnaajiep.expensetrackerapi.security.JwtUtility;
import com.krisnaajiep.expensetrackerapi.util.StringUtility;
import com.krisnaajiep.expensetrackerapi.util.TestDataGenerator;
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
import java.util.*;

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
    private ExpenseCategoryRepository categoryRepository;

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
    private List<ExpenseCategory> categories;

    private final ExpenseRequestDto expenseRequestDto = new ExpenseRequestDto();
    private final Map<String, Object> invalidExpenseRequest = new HashMap<>();
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        // Clean up the database before each test
        expenseRepository.deleteAll();
        categoryRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        categories = categoryRepository.saveAll(
                List.of(
                        ExpenseCategory.builder().name("Groceries").build(),
                        ExpenseCategory.builder().name("Leisure").build(),
                        ExpenseCategory.builder().name("Electronics").build(),
                        ExpenseCategory.builder().name("Utilities").build(),
                        ExpenseCategory.builder().name("Clothing").build(),
                        ExpenseCategory.builder().name("Health").build(),
                        ExpenseCategory.builder().name("Others").build()
                )
        );

        user = createUser(); // Create a test user

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
                    ValidationMessages.POSITIVE_MESSAGE,
                    ((Map<?, ?>) response.get("errors")).get("categoryId")
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
    void testSave_CategoryNotFound() throws Exception {
        setSaveExpenseRequest(TestDataGenerator.generateRandomNumber(1000, 5000)); // Set up the request DTO with a non-existent category ID

        mockMvc.perform(post("/expenses")
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
            assertEquals(
                    "Expense category not found with ID: " + expenseRequestDto.getCategoryId(),
                    response.get("message")
            );
        });
    }

    @Test
    void testSave_Success() throws Exception {
        ExpenseCategory category = getRandomCategory(); // Get a random category
        setSaveExpenseRequest(category.getId()); // Set up the request DTO for saving an expense

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
            assertEquals(category.getName(), response.getCategory());
            assertEquals(expenseRequestDto.getDate(), response.getDate());
        });
    }

    @Test
    void testUpdate_Unauthorized() throws Exception {
        String accessToken = jwtUtility.generateToken(user.getId().toString(), " ");

        mockMvc.perform(post("/expenses/" + UUID.randomUUID())
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

        mockMvc.perform(put("/expenses/" + UUID.randomUUID())
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
                    ValidationMessages.POSITIVE_MESSAGE,
                    ((Map<?, ?>) response.get("errors")).get("categoryId")
            );
            assertEquals(ValidationMessages.PAST_OR_PRESENT_MESSAGE, ((Map<?, ?>) response.get("errors")).get("date"));
        });
    }

    @Test
    void testUpdate_InvalidDateFormat() throws Exception {
        setInvalidExpenseRequest(); // Set up an invalid expense request for updating
        invalidExpenseRequest.put("date", "Invalid date format");

        mockMvc.perform(put("/expenses/" + UUID.randomUUID())
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
    void testUpdate_ExpenseNotFound() throws Exception {
        setUpdateExpenseRequest(getRandomCategory().getId()); // Set up the request DTO for updating an expense
        UUID nonExistentExpenseId = UUID.randomUUID(); // Assuming expense with this ID does not exist

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
    void testUpdate_CategoryNotFound() throws Exception {
        setUpdateExpenseRequest(TestDataGenerator.generateRandomNumber(1000, 5000)); // Set up the request DTO with a non-existent category ID

        Expense expense = Expense.builder()
                .description(TestDataGenerator.generateDescription(10))
                .category(getRandomCategory())
                .amount(TestDataGenerator.generateAmount(10, 1000))
                .date(TestDataGenerator.generateDate(-30, 0))
                .user(user)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        mockMvc.perform(put("/expenses/" + savedExpense.getId())
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
            assertEquals(
                    "Expense category not found with ID: " + expenseRequestDto.getCategoryId(),
                    response.get("message")
            );
        });
    }

    @Test
    void testUpdate_Forbidden() throws Exception {
        setUpdateExpenseRequest(getRandomCategory().getId()); // Set up the request DTO for updating an expense
        User anotherUser = createUser(); // Create another user
        anotherExpense = createExpense(anotherUser); // Create an expense for the other user

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
        ExpenseCategory newCategory = getRandomCategory(); // Get a random category for updating
        setUpdateExpenseRequest(newCategory.getId()); // Set up the request DTO for updating an expense

        Expense expense = Expense.builder()
                .description(TestDataGenerator.generateDescription(10))
                .category(getRandomCategory())
                .amount(TestDataGenerator.generateAmount(10, 1000))
                .date(TestDataGenerator.generateDate(-30, 0))
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
            assertEquals(newCategory.getName(), response.getCategory());
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
    void testDelete_InvalidExpenseId() throws Exception {
        mockMvc.perform(delete("/expenses/1")
                .accept(MediaType.ALL)
                .header("Authorization", "Bearer " + accessToken)
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
            assertEquals("Invalid value for `expenseId`: 1, Expected type: UUID", response.get("message"));
        });
    }

    @Test
    void testDelete_NotFound() throws Exception {
        // Assuming expense with this ID does not exist
        UUID nonExistentExpenseId = UUID.randomUUID();

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
        User anotherUser = createUser(); // Create another user
        anotherExpense = createExpense(anotherUser); // Create an expense for the other user

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
                .category(getRandomCategory())
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
    void testFindAll_InvalidFilter() throws Exception {
        mockMvc.perform(get("/expenses?filter=invalid_filter")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
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
            assertTrue(response.get("message").toString().contains("Invalid value for `filter`: invalid_filter"));
        });
    }

    @Test
    void testFindAll_Success() throws Exception {
        createExpenses(user); // Create multiple expenses for the user

        mockMvc.perform(get("/expenses?page=0&size=15&filter=past_week")
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

            assertNotNull(response.getContent());
            assertNotNull(response.getMetadata());

            assertFalse(response.getContent().isEmpty());
            assertEquals(15, response.getContent().size());

            assertNotNull(response.getMetadata());
            assertEquals(15, response.getMetadata().totalElements());

            Optional<ExpenseResponseDto> notPastWeekExpense = response.getContent().stream()
                    .filter(exp ->
                            exp.getDate().isBefore(LocalDate.now().minusDays(7))
                    ).findFirst();

            assertNull(notPastWeekExpense.orElse(null));
        });
    }

    private void setInvalidExpenseRequest() {
        invalidExpenseRequest.put("description", null);
        invalidExpenseRequest.put("categoryId", TestDataGenerator.generateRandomNumber(-1000, -1));
        invalidExpenseRequest.put("amount", TestDataGenerator.generateAmount(-1000, 0));
        invalidExpenseRequest.put("date", TestDataGenerator.generateDate(1, 30));
    }

    private void setSaveExpenseRequest(Long categoryId) {
        expenseRequestDto.setDescription(TestDataGenerator.generateDescription(10));
        expenseRequestDto.setCategoryId(categoryId);
        expenseRequestDto.setAmount(TestDataGenerator.generateAmount(10, 1000));
        expenseRequestDto.setDate(TestDataGenerator.generateDate(-30, 0));
    }

    private void setUpdateExpenseRequest(Long categoryId) {
        expenseRequestDto.setDescription(TestDataGenerator.generateDescription(10));
        expenseRequestDto.setCategoryId(categoryId);
        expenseRequestDto.setAmount(TestDataGenerator.generateAmount(10, 1000));
        expenseRequestDto.setDate(TestDataGenerator.generateDate(-30, 0));
    }

    private User createUser() {
        User user = User.builder()
                .name(TestDataGenerator.generateFullName())
                .email(TestDataGenerator.generateEmail())
                .password(StringUtility.generateRandomString(8))
                .build();

        return userRepository.save(user);
    }

    private Expense createExpense(User user) {
        anotherExpense = Expense.builder()
                .description(TestDataGenerator.generateDescription(10))
                .category(getRandomCategory())
                .amount(TestDataGenerator.generateAmount(10, 1000))
                .date(TestDataGenerator.generateDate(-30, 0))
                .user(user)
                .build();

        return expenseRepository.save(anotherExpense);
    }

    private void createExpenses(User user) {
        List<Expense> expenses = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            LocalDate date;
            if (i < 15) {
                date = LocalDate.now().minusDays(6);
            } else if (i < 60) {
                date = LocalDate.now().minusDays(20);
            } else {
                date = LocalDate.now().minusDays(80);
            }

            Expense expense = Expense.builder()
                    .description(TestDataGenerator.generateDescription(10))
                    .amount(TestDataGenerator.generateAmount(10, 1000))
                    .category(getRandomCategory())
                    .date(date)
                    .user(user)
                    .build();

            expenses.add(expense);
        }

        expenseRepository.saveAll(expenses);
    }

    private ExpenseCategory getRandomCategory() {
        Long categoryId = categories.get(random.nextInt(categories.size())).getId(); // Get a random category ID
        return categoryRepository.findById(categoryId).orElse(null); // Fetch the category from the repository
    }
}