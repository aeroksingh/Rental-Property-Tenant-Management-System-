package com.rentalmanagement.system.dto.response;

import com.rentalmanagement.system.entity.Property;
import com.rentalmanagement.system.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class PropertyResponse {
    private Long id;
    private String address;
    private String city;
    private PropertyType type;
    private BigDecimal rentAmount;
    private boolean isOccupied;
    private Long ownerId;

    public static PropertyResponse fromEntity(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .address(p.getAddress())
                .city(p.getCity())
                .type(p.getType())
                .rentAmount(p.getRentAmount())
                .isOccupied(p.isOccupied())
                .ownerId(p.getOwner() != null ? p.getOwner().getId() : null)
                .build();
    }
}
