package com.rentalmanagement.system.dto.response;

import com.rentalmanagement.system.entity.RentPayment;
import com.rentalmanagement.system.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class RentPaymentResponse {
    private Long id;
    private Long tenantId;
    private Long propertyId;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private PaymentStatus status;

    public static RentPaymentResponse fromEntity(RentPayment r) {
        return RentPaymentResponse.builder()
                .id(r.getId())
                .tenantId(r.getTenant().getId())
                .propertyId(r.getProperty().getId())
                .amountDue(r.getAmountDue())
                .amountPaid(r.getAmountPaid())
                .dueDate(r.getDueDate())
                .paidDate(r.getPaidDate())
                .status(r.getStatus())
                .build();
    }
}
