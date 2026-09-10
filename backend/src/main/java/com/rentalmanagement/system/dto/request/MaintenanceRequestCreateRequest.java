package com.rentalmanagement.system.dto.request;

import com.rentalmanagement.system.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceRequestCreateRequest {

    @NotNull
    private Long propertyId;

    @NotBlank
    private String title;

    private String description;

    private Priority priority;
}
