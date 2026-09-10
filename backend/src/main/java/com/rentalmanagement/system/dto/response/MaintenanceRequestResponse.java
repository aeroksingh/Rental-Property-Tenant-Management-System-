package com.rentalmanagement.system.dto.response;

import com.rentalmanagement.system.entity.MaintenanceRequest;
import com.rentalmanagement.system.enums.MaintenanceStatus;
import com.rentalmanagement.system.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MaintenanceRequestResponse {
    private Long id;
    private Long tenantId;
    private Long propertyId;
    private String title;
    private String description;
    private MaintenanceStatus status;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public static MaintenanceRequestResponse fromEntity(MaintenanceRequest m) {
        return MaintenanceRequestResponse.builder()
                .id(m.getId())
                .tenantId(m.getTenant().getId())
                .propertyId(m.getProperty().getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .status(m.getStatus())
                .priority(m.getPriority())
                .createdAt(m.getCreatedAt())
                .resolvedAt(m.getResolvedAt())
                .build();
    }
}
