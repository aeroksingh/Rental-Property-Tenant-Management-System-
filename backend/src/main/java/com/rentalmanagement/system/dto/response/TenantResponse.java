package com.rentalmanagement.system.dto.response;

import com.rentalmanagement.system.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class TenantResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Long propertyId;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;

    public static TenantResponse fromEntity(Tenant t) {
        return TenantResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .email(t.getEmail())
                .phone(t.getPhone())
                .propertyId(t.getProperty() != null ? t.getProperty().getId() : null)
                .leaseStartDate(t.getLeaseStartDate())
                .leaseEndDate(t.getLeaseEndDate())
                .build();
    }
}
