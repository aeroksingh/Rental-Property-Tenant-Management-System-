package com.rentalmanagement.system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class RentPaymentRequest {

    @NotNull
    private Long tenantId;

    @NotNull
    @Positive
    private BigDecimal amountDue;

    @NotNull
    private LocalDate dueDate;
}
