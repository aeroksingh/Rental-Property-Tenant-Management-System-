package com.rentalmanagement.system.dto.request;

import com.rentalmanagement.system.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceStatusUpdateRequest {

    @NotNull
    private MaintenanceStatus status;
}
