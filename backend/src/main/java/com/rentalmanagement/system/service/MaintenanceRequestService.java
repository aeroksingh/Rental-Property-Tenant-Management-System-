package com.rentalmanagement.system.service;

import com.rentalmanagement.system.dto.request.MaintenanceRequestCreateRequest;
import com.rentalmanagement.system.dto.request.MaintenanceStatusUpdateRequest;
import com.rentalmanagement.system.dto.response.MaintenanceRequestResponse;
import com.rentalmanagement.system.entity.MaintenanceRequest;
import com.rentalmanagement.system.entity.Property;
import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.enums.MaintenanceStatus;
import com.rentalmanagement.system.enums.Priority;
import com.rentalmanagement.system.exception.AccessDeniedCustomException;
import com.rentalmanagement.system.exception.BadRequestException;
import com.rentalmanagement.system.exception.ResourceNotFoundException;
import com.rentalmanagement.system.repository.MaintenanceRequestRepository;
import com.rentalmanagement.system.repository.PropertyRepository;
import com.rentalmanagement.system.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    @Transactional
    public MaintenanceRequestResponse createRequest(Long tenantId, MaintenanceRequestCreateRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        // Critical validation: a tenant may only raise requests for the property
        // they are actually assigned to. Enforced here, not just on the frontend.
        if (tenant.getProperty() == null || !tenant.getProperty().getId().equals(request.getPropertyId())) {
            throw new AccessDeniedCustomException("You can only raise maintenance requests for your assigned property");
        }

        MaintenanceRequest maintenanceRequest = MaintenanceRequest.builder()
                .tenant(tenant)
                .property(tenant.getProperty())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(MaintenanceStatus.RAISED)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build();

        return MaintenanceRequestResponse.fromEntity(maintenanceRequestRepository.save(maintenanceRequest));
    }

    public List<MaintenanceRequestResponse> getRequestsForProperty(Long ownerId, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedCustomException("You do not own this property");
        }

        return maintenanceRequestRepository.findByPropertyId(propertyId).stream()
                .map(MaintenanceRequestResponse::fromEntity)
                .toList();
    }

    public List<MaintenanceRequestResponse> getRequestsForTenant(Long tenantId) {
        return maintenanceRequestRepository.findByTenantId(tenantId).stream()
                .map(MaintenanceRequestResponse::fromEntity)
                .toList();
    }

    @Transactional
    public MaintenanceRequestResponse updateStatus(Long ownerId, Long requestId, MaintenanceStatusUpdateRequest request) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance request not found with id: " + requestId));

        if (!maintenanceRequest.getProperty().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedCustomException("You do not own the property this request belongs to");
        }

        MaintenanceStatus current = maintenanceRequest.getStatus();
        MaintenanceStatus next = request.getStatus();

        if (current == MaintenanceStatus.RESOLVED || current == MaintenanceStatus.REJECTED) {
            throw new BadRequestException("Cannot change status of a request that is already " + current);
        }

        maintenanceRequest.setStatus(next);
        if (next == MaintenanceStatus.RESOLVED || next == MaintenanceStatus.REJECTED) {
            maintenanceRequest.setResolvedAt(LocalDateTime.now());
        }

        return MaintenanceRequestResponse.fromEntity(maintenanceRequestRepository.save(maintenanceRequest));
    }
}
