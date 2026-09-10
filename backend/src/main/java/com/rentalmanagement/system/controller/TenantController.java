package com.rentalmanagement.system.controller;

import com.rentalmanagement.system.dto.response.PropertyResponse;
import com.rentalmanagement.system.dto.response.TenantResponse;
import com.rentalmanagement.system.security.SecurityUtils;
import com.rentalmanagement.system.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantById(SecurityUtils.getCurrentUser(), id));
    }

    @GetMapping("/{id}/property")
    public ResponseEntity<PropertyResponse> getTenantProperty(@PathVariable Long id) {
        return ResponseEntity.ok(tenantService.getTenantProperty(SecurityUtils.getCurrentUser(), id));
    }
}
