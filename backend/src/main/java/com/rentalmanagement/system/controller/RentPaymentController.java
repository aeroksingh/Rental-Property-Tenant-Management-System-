package com.rentalmanagement.system.controller;

import com.rentalmanagement.system.dto.request.MarkPaidRequest;
import com.rentalmanagement.system.dto.request.RentPaymentRequest;
import com.rentalmanagement.system.dto.response.RentPaymentResponse;
import com.rentalmanagement.system.security.SecurityUtils;
import com.rentalmanagement.system.service.RentPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rent-payments")
@RequiredArgsConstructor
public class RentPaymentController {

    private final RentPaymentService rentPaymentService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RentPaymentResponse> create(@Valid @RequestBody RentPaymentRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(rentPaymentService.createRentPayment(ownerId, request));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<RentPaymentResponse>> getForTenant(@PathVariable Long tenantId) {
        return ResponseEntity.ok(rentPaymentService.getPaymentsForTenant(SecurityUtils.getCurrentUser(), tenantId));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<RentPaymentResponse>> getOverdue() {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(rentPaymentService.getOverduePaymentsForOwner(ownerId));
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RentPaymentResponse> markPaid(@PathVariable Long id,
                                                         @Valid @RequestBody(required = false) MarkPaidRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        MarkPaidRequest body = request != null ? request : new MarkPaidRequest();
        return ResponseEntity.ok(rentPaymentService.markPaid(ownerId, id, body));
    }
}
