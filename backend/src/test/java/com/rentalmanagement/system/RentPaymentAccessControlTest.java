package com.rentalmanagement.system;

import com.jayway.jsonpath.JsonPath;
import com.rentalmanagement.system.scheduler.OverdueRentScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RentPaymentAccessControlTest extends AbstractIntegrationTest {

    @Autowired
    private OverdueRentScheduler overdueRentScheduler;

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
    void tenantCannotViewAnotherTenantsPayments() throws Exception {
        RegisteredUser owner = registerUser("Owner RP1", "owner.rp1@owner.com", "password123", "OWNER");
        RegisteredUser tenant1 = registerUser("Tenant RP1A", "tenant1.rp1@tenant.com", "password123", "TENANT");
        RegisteredUser tenant2 = registerUser("Tenant RP1B", "tenant2.rp1@tenant.com", "password123", "TENANT");
        long propertyId = createProperty(owner.token(), "1 Rent Lane");
        assignTenant(owner.token(), propertyId, tenant1.id());

        Map<String, Object> paymentBody = Map.of(
                "tenantId", tenant1.id(), "amountDue", 15000, "dueDate", LocalDate.now().plusDays(10).toString());

        mockMvc.perform(post("/api/rent-payments")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentBody)))
                .andExpect(status().isCreated());

        // Tenant1 can see their own payments.
        mockMvc.perform(get("/api/rent-payments/tenant/" + tenant1.id())
                        .header("Authorization", bearer(tenant1.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amountDue").value(15000));

        // Tenant2 cannot see Tenant1's payments.
        mockMvc.perform(get("/api/rent-payments/tenant/" + tenant1.id())
                        .header("Authorization", bearer(tenant2.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCannotMarkPaidOnAnotherOwnersProperty() throws Exception {
        RegisteredUser ownerA = registerUser("Owner RP2A", "ownerA.rp2@owner.com", "password123", "OWNER");
        RegisteredUser ownerB = registerUser("Owner RP2B", "ownerB.rp2@owner.com", "password123", "OWNER");
        RegisteredUser tenant = registerUser("Tenant RP2", "tenant.rp2@tenant.com", "password123", "TENANT");
        long propertyId = createProperty(ownerA.token(), "2 Rent Lane");
        assignTenant(ownerA.token(), propertyId, tenant.id());

        Map<String, Object> paymentBody = Map.of(
                "tenantId", tenant.id(), "amountDue", 15000, "dueDate", LocalDate.now().plusDays(10).toString());

        MvcResult createResult = mockMvc.perform(post("/api/rent-payments")
                        .header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentBody)))
                .andExpect(status().isCreated())
                .andReturn();

        Number paymentId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/rent-payments/" + paymentId + "/mark-paid")
                        .header("Authorization", bearer(ownerB.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/rent-payments/" + paymentId + "/mark-paid")
                        .header("Authorization", bearer(ownerA.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void overdueScheduler_flagsPastDuePendingPayments_andExcludesPaidOnes() throws Exception {
        RegisteredUser owner = registerUser("Owner RP3", "owner.rp3@owner.com", "password123", "OWNER");
        RegisteredUser tenant = registerUser("Tenant RP3", "tenant.rp3@tenant.com", "password123", "TENANT");
        long propertyId = createProperty(owner.token(), "3 Rent Lane");
        assignTenant(owner.token(), propertyId, tenant.id());

        // Past-due and still PENDING -> should get flagged OVERDUE.
        Map<String, Object> overduePayment = Map.of(
                "tenantId", tenant.id(), "amountDue", 15000, "dueDate", LocalDate.now().minusDays(5).toString());
        mockMvc.perform(post("/api/rent-payments")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overduePayment)))
                .andExpect(status().isCreated());

        // Past-due but already PAID -> must NOT be re-flagged.
        Map<String, Object> paidPastPayment = Map.of(
                "tenantId", tenant.id(), "amountDue", 15000, "dueDate", LocalDate.now().minusDays(40).toString());
        MvcResult paidResult = mockMvc.perform(post("/api/rent-payments")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paidPastPayment)))
                .andExpect(status().isCreated())
                .andReturn();
        Number paidId = JsonPath.read(paidResult.getResponse().getContentAsString(), "$.id");
        mockMvc.perform(put("/api/rent-payments/" + paidId + "/mark-paid")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // Future due date, still pending -> must NOT be flagged.
        Map<String, Object> futurePayment = Map.of(
                "tenantId", tenant.id(), "amountDue", 15000, "dueDate", LocalDate.now().plusDays(10).toString());
        mockMvc.perform(post("/api/rent-payments")
                        .header("Authorization", bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(futurePayment)))
                .andExpect(status().isCreated());

        // Run the scheduler's job body directly instead of waiting for its cron trigger.
        overdueRentScheduler.flagOverduePayments();

        mockMvc.perform(get("/api/rent-payments/overdue")
                        .header("Authorization", bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amountDue").value(15000));
    }
}
