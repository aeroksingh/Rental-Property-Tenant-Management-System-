package com.rentalmanagement.system.controller;

import com.rentalmanagement.system.dto.request.PropertyRequest;
import com.rentalmanagement.system.dto.response.PropertyResponse;
import com.rentalmanagement.system.security.SecurityUtils;
import com.rentalmanagement.system.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(ownerId, request));
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getMyProperties() {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(propertyService.getPropertiesForOwner(ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(@PathVariable Long id, @Valid @RequestBody PropertyRequest request) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(propertyService.updateProperty(ownerId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        propertyService.deleteProperty(ownerId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign-tenant/{tenantId}")
    public ResponseEntity<PropertyResponse> assignTenant(@PathVariable Long id, @PathVariable Long tenantId) {
        Long ownerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(propertyService.assignTenant(ownerId, id, tenantId));
    }
}
