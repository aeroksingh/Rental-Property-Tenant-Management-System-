package com.rentalmanagement.system.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MarkPaidRequest {

    // If omitted, defaults to amountDue (full payment) in the service layer
    @Positive
    private BigDecimal amountPaid;

    // If omitted, defaults to today
    private LocalDate paidDate;
}
