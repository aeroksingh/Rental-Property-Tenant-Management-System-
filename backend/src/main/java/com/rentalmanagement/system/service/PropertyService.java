package com.rentalmanagement.system.service;

import com.rentalmanagement.system.dto.request.PropertyRequest;
import com.rentalmanagement.system.dto.response.PropertyResponse;
import com.rentalmanagement.system.entity.Owner;
import com.rentalmanagement.system.entity.Property;
import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.exception.AccessDeniedCustomException;
import com.rentalmanagement.system.exception.BadRequestException;
import com.rentalmanagement.system.exception.ResourceNotFoundException;
import com.rentalmanagement.system.repository.OwnerRepository;
import com.rentalmanagement.system.repository.PropertyRepository;
import com.rentalmanagement.system.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public PropertyResponse createProperty(Long ownerId, PropertyRequest request) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

        Property property = Property.builder()
                .address(request.getAddress())
                .city(request.getCity())
                .type(request.getType())
                .rentAmount(request.getRentAmount())
                .isOccupied(false)
                .owner(owner)
                .build();

        return PropertyResponse.fromEntity(propertyRepository.save(property));
    }

    public List<PropertyResponse> getPropertiesForOwner(Long ownerId) {
        return propertyRepository.findByOwnerId(ownerId).stream()
                .map(PropertyResponse::fromEntity)
                .toList();
    }

    @Transactional
    public PropertyResponse updateProperty(Long ownerId, Long propertyId, PropertyRequest request) {
        Property property = getOwnedPropertyOrThrow(ownerId, propertyId);

        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setType(request.getType());
        property.setRentAmount(request.getRentAmount());

        return PropertyResponse.fromEntity(propertyRepository.save(property));
    }

    @Transactional
    public void deleteProperty(Long ownerId, Long propertyId) {
        Property property = getOwnedPropertyOrThrow(ownerId, propertyId);
        propertyRepository.delete(property);
    }

    @Transactional
    public PropertyResponse assignTenant(Long ownerId, Long propertyId, Long tenantId) {
        Property property = getOwnedPropertyOrThrow(ownerId, propertyId);

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        if (property.isOccupied()) {
            throw new BadRequestException("Property is already occupied. Unassign the current tenant first.");
        }

        if (tenant.getProperty() != null) {
            throw new BadRequestException("Tenant is already assigned to a property.");
        }

        tenant.setProperty(property);
        property.setOccupied(true);

        tenantRepository.save(tenant);
        return PropertyResponse.fromEntity(propertyRepository.save(property));
    }

    private Property getOwnedPropertyOrThrow(Long ownerId, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedCustomException("You do not own this property");
        }
        return property;
    }
}
