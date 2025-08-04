package com.krisnaajiep.expensetrackerapi.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"throttling.refill-duration=100"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitFilterIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRateLimit() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andDo(result -> {
                        Map<String, Object> response = objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                new TypeReference<>() {
                                }
                        );

                        assertNotNull(response);
                        assertNotNull(response.get("status"));
                        assertEquals("UP", response.get("status"));
                    });
        }

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isTooManyRequests())
                .andDo(result -> {
                    Map<String, Object> response = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<>() {
                            }
                    );

                    assertNotNull(response);
                    assertNotNull(response.get("message"));
                    assertEquals("Rate limit exceeded", response.get("message"));
                });
    }
}