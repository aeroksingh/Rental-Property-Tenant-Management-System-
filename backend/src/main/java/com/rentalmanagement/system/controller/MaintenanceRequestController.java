package com.rentalmanagement.system.controller;

import com.rentalmanagement.system.dto.request.MaintenanceRequestCreateRequest;
import com.rentalmanagement.system.dto.request.MaintenanceStatusUpdateRequest;
import com.rentalmanagement.system.dto.response.MaintenanceRequestResponse;
import com.rentalmanagement.system.security.SecurityUtils;
import com.rentalmanagement.system.service.MaintenanceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-requests")
@RequiredArgsConstructor
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<MaintenanceRequestResponse> create(@Valid @RequestBody MaintenanceRequestCreateRequest request) {
        Long tenantId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceRequestService.createRequest(tenantId, request));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<MaintenanceRequestResponse>> getForProperty(@PathVariable Long propertyId) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(maintenanceRequestService.getRequestsForProperty(ownerId, propertyId));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<List<MaintenanceRequestResponse>> getMyRequests() {
        Long tenantId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(maintenanceRequestService.getRequestsForTenant(tenantId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<MaintenanceRequestResponse> updateStatus(@PathVariable Long id,
                                                                    @Valid @RequestBody MaintenanceStatusUpdateRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(maintenanceRequestService.updateStatus(ownerId, id, request));
    }
}
