package com.rentalmanagement.system;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PropertyAccessControlTest extends AbstractIntegrationTest {

    @Test
    void tenantCannotCreateProperty() throws Exception {
        RegisteredUser tenant = registerUser("Tenant One", "t1.prop@tenant.com", "password123", "TENANT");

        Map<String, Object> body = Map.of(
                "address", "1 Fake Street", "city", "Nashik", "type", "APARTMENT", "rentAmount", 10000);

        mockMvc.perform(post("/api/properties")
                        .header("Authorization", bearer(tenant.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanCreateReadUpdateAndDeleteOwnProperty() throws Exception {
        RegisteredUser owner = registerUser("Owner One", "o1.prop@owner.com", "password123", "OWNER");

        Map<String, Object> createBody = Map.of(
                "address", "12 MG Road", "city", "Nashik", "type", "APARTMENT", "rentAmount", 15000);

        MvcResult createResult = mockMvc.perform(post("/api/properties")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.address").value("12 MG Road"))
                .andReturn();

        Number rawId = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        long propertyId = rawId.longValue();

        mockMvc.perform(get("/api/properties").header("Authorization", bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address").value("12 MG Road"));

        Map<String, Object> updateBody = Map.of(
                "address", "12 MG Road, Unit 2", "city", "Nashik", "type", "APARTMENT", "rentAmount", 16000);

        mockMvc.perform(put("/api/properties/" + propertyId)
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rentAmount").value(16000));

        mockMvc.perform(delete("/api/properties/" + propertyId)
                        .header("Authorization", bearer(owner.token())))
                .andExpect(status().isNoContent());
    }

    @Test
    void ownerCannotModifyAnotherOwnersProperty() throws Exception {
        RegisteredUser ownerA = registerUser("Owner A", "ownerA.prop@owner.com", "password123", "OWNER");
        RegisteredUser ownerB = registerUser("Owner B", "ownerB.prop@owner.com", "password123", "OWNER");

        Map<String, Object> createBody = Map.of(
                "address", "45 Church Street", "city", "Pune", "type", "HOUSE", "rentAmount", 28000);

        MvcResult createResult = mockMvc.perform(post("/api/properties")
                        .header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())
                .andReturn();

        Number rawId = com.jayway.jsonpath.JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");
        long propertyId = rawId.longValue();

        Map<String, Object> updateBody = Map.of(
                "address", "Hijacked", "city", "Pune", "type", "HOUSE", "rentAmount", 99999);

        mockMvc.perform(put("/api/properties/" + propertyId)
                        .header("Authorization", bearer(ownerB.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBody)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/properties/" + propertyId)
                        .header("Authorization", bearer(ownerB.token())))
                .andExpect(status().isForbidden());
    }
}
