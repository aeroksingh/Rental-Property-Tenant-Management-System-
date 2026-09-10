package com.rentalmanagement.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Shared setup for integration tests: full Spring context, MockMvc,
 * "test" profile (isolated H2 instance, dev data seeder disabled), and
 * each test method wrapped in a transaction that rolls back afterwards so
 * tests never leak data into one another.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ExtendWith(SpringExtension.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected record RegisteredUser(Long id, String token) {
    }

    protected RegisteredUser registerUser(String name, String email, String password, String role) throws Exception {
        Map<String, String> body = Map.of(
                "name", name,
                "email", email,
                "password", password,
                "phone", "9000000000",
                "role", role
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Long id = Long.valueOf(JsonPath.read(json, "$.userId").toString());
        String token = JsonPath.read(json, "$.token");
        return new RegisteredUser(id, token);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
