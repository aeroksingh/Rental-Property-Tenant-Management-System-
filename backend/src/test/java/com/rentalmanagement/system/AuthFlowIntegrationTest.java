package com.rentalmanagement.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerOwner_succeedsAndReturnsToken() throws Exception {
        Map<String, String> body = Map.of(
                "name", "Alice Owner",
                "email", "alice.auth@owner.com",
                "password", "password123",
                "phone", "9999999999",
                "role", "OWNER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void registerWithDuplicateEmail_fails() throws Exception {
        registerUser("Alice Owner", "dup@owner.com", "password123", "OWNER");

        Map<String, String> body = Map.of(
                "name", "Alice Clone",
                "email", "dup@owner.com",
                "password", "password123",
                "phone", "9999999999",
                "role", "OWNER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withCorrectCredentials_succeeds() throws Exception {
        registerUser("Bob Tenant", "bob.auth@tenant.com", "password123", "TENANT");

        Map<String, String> loginBody = Map.of("email", "bob.auth@tenant.com", "password", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("TENANT"));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        registerUser("Carol Owner", "carol.auth@owner.com", "password123", "OWNER");

        Map<String, String> loginBody = Map.of("email", "carol.auth@owner.com", "password", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isUnauthorized());
    }
}
