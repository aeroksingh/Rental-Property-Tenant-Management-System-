package com.rentalmanagement.system.service;

import com.rentalmanagement.system.dto.response.PropertyResponse;
import com.rentalmanagement.system.dto.response.TenantResponse;
import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.enums.Role;
import com.rentalmanagement.system.exception.AccessDeniedCustomException;
import com.rentalmanagement.system.exception.ResourceNotFoundException;
import com.rentalmanagement.system.repository.TenantRepository;
import com.rentalmanagement.system.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantResponse getTenantById(AppUserPrincipal requester, Long tenantId) {
        Tenant tenant = findTenantOrThrow(tenantId);
        assertCanAccessTenant(requester, tenant);
        return TenantResponse.fromEntity(tenant);
    }

    public PropertyResponse getTenantProperty(AppUserPrincipal requester, Long tenantId) {
        Tenant tenant = findTenantOrThrow(tenantId);
        assertCanAccessTenant(requester, tenant);

        if (tenant.getProperty() == null) {
            throw new ResourceNotFoundException("Tenant is not currently assigned to a property");
        }
        return PropertyResponse.fromEntity(tenant.getProperty());
    }

    private Tenant findTenantOrThrow(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));
    }

    /**
     * Owners may look up any tenant (needed for management/assignment flows).
     * A tenant may only look up their own record.
     */
    private void assertCanAccessTenant(AppUserPrincipal requester, Tenant tenant) {
        boolean isOwner = requester.getRole() == Role.OWNER;
        boolean isSelf = requester.getRole() == Role.TENANT && requester.getId().equals(tenant.getId());

        if (!isOwner && !isSelf) {
            throw new AccessDeniedCustomException("You do not have permission to view this tenant");
        }
    }
}
