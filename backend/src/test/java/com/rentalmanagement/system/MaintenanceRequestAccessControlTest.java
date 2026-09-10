package com.rentalmanagement.system;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MaintenanceRequestAccessControlTest extends AbstractIntegrationTest {

    private long createProperty(String ownerToken, String address) throws Exception {
        Map<String, Object> body = Map.of(
                "address", address, "city", "Nashik", "type", "APARTMENT", "rentAmount", 15000);

        MvcResult result = mockMvc.perform(post("/api/properties")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private void assignTenant(String ownerToken, long propertyId, long tenantId) throws Exception {
        mockMvc.perform(post("/api/properties/" + propertyId + "/assign-tenant/" + tenantId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());
    }

    @Test
    void tenantCannotRaiseRequestForUnassignedProperty() throws Exception {
        RegisteredUser owner = registerUser("Owner MR1", "owner.mr1@owner.com", "password123", "OWNER");
        RegisteredUser tenant = registerUser("Tenant MR1", "tenant.mr1@tenant.com", "password123", "TENANT");
        long propertyId = createProperty(owner.token(), "1 Unassigned Lane");
        // Note: tenant is deliberately NOT assigned to propertyId here.

        Map<String, Object> requestBody = Map.of(
                "propertyId", propertyId, "title", "Broken window", "priority", "HIGH");

        mockMvc.perform(post("/api/maintenance-requests")
                        .header("Authorization", bearer(tenant.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void assignedTenantCanRaiseRequest_andOwnerCanViewAndUpdateIt() throws Exception {
        RegisteredUser owner = registerUser("Owner MR2", "owner.mr2@owner.com", "password123", "OWNER");
        RegisteredUser tenant = registerUser("Tenant MR2", "tenant.mr2@tenant.com", "password123", "TENANT");
        long propertyId = createProperty(owner.token(), "2 Assigned Lane");
        assignTenant(owner.token(), propertyId, tenant.id());

        Map<String, Object> requestBody = Map.of(
                "propertyId", propertyId, "title", "Leaking tap", "priority", "MEDIUM");

        MvcResult createResult = mockMvc.perform(post("/api/maintenance-requests")
                        .header("Authorization", bearer(tenant.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RAISED"))
                .andReturn();

        Number requestId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/maintenance-requests/property/" + propertyId)
                        .header("Authorization", bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Leaking tap"));

        mockMvc.perform(put("/api/maintenance-requests/" + requestId + "/status")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(put("/api/maintenance-requests/" + requestId + "/status")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "RESOLVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolvedAt").isNotEmpty());

        // Once RESOLVED, further status changes should be rejected.
        mockMvc.perform(put("/api/maintenance-requests/" + requestId + "/status")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "IN_PROGRESS"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ownerCannotViewAnotherOwnersMaintenanceQueue() throws Exception {
        RegisteredUser ownerA = registerUser("Owner MR3A", "ownerA.mr3@owner.com", "password123", "OWNER");
        RegisteredUser ownerB = registerUser("Owner MR3B", "ownerB.mr3@owner.com", "password123", "OWNER");
        long propertyId = createProperty(ownerA.token(), "3 Rival Lane");

        mockMvc.perform(get("/api/maintenance-requests/property/" + propertyId)
                        .header("Authorization", bearer(ownerB.token())))
                .andExpect(status().isForbidden());
    }
}
