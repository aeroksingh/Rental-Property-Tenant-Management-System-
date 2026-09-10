package com.rentalmanagement.system.service;

import com.rentalmanagement.system.dto.request.MarkPaidRequest;
import com.rentalmanagement.system.dto.request.RentPaymentRequest;
import com.rentalmanagement.system.dto.response.RentPaymentResponse;
import com.rentalmanagement.system.entity.Property;
import com.rentalmanagement.system.entity.RentPayment;
import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.enums.PaymentStatus;
import com.rentalmanagement.system.enums.Role;
import com.rentalmanagement.system.exception.AccessDeniedCustomException;
import com.rentalmanagement.system.exception.BadRequestException;
import com.rentalmanagement.system.exception.ResourceNotFoundException;
import com.rentalmanagement.system.repository.RentPaymentRepository;
import com.rentalmanagement.system.repository.TenantRepository;
import com.rentalmanagement.system.security.AppUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentPaymentService {

    private final RentPaymentRepository rentPaymentRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public RentPaymentResponse createRentPayment(Long ownerId, RentPaymentRequest request) {
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + request.getTenantId()));

        Property property = tenant.getProperty();
        if (property == null) {
            throw new BadRequestException("Tenant is not currently assigned to a property");
        }

        if (!property.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedCustomException("You do not own the property this tenant is assigned to");
        }

        RentPayment payment = RentPayment.builder()
                .tenant(tenant)
                .property(property)
                .amountDue(request.getAmountDue())
                .amountPaid(BigDecimal.ZERO)
                .dueDate(request.getDueDate())
                .status(PaymentStatus.PENDING)
                .build();

        return RentPaymentResponse.fromEntity(rentPaymentRepository.save(payment));
    }

    public List<RentPaymentResponse> getPaymentsForTenant(AppUserPrincipal requester, Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id: " + tenantId));

        boolean isOwnerOfProperty = requester.getRole() == Role.OWNER
                && tenant.getProperty() != null
                && tenant.getProperty().getOwner().getId().equals(requester.getId());
        boolean isSelf = requester.getRole() == Role.TENANT && requester.getId().equals(tenantId);

        if (!isOwnerOfProperty && !isSelf) {
            throw new AccessDeniedCustomException("You do not have permission to view these rent payments");
        }

        return rentPaymentRepository.findByTenantId(tenantId).stream()
                .map(RentPaymentResponse::fromEntity)
                .toList();
    }

    /**
     * Owner dashboard: all overdue payments across all of the owner's properties.
     */
    public List<RentPaymentResponse> getOverduePaymentsForOwner(Long ownerId) {
        return rentPaymentRepository.findByStatus(PaymentStatus.OVERDUE).stream()
                .filter(p -> p.getProperty().getOwner().getId().equals(ownerId))
                .map(RentPaymentResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RentPaymentResponse markPaid(Long ownerId, Long paymentId, MarkPaidRequest request) {
        RentPayment payment = rentPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Rent payment not found with id: " + paymentId));

        if (!payment.getProperty().getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedCustomException("You do not own the property associated with this payment");
        }

        BigDecimal amountPaid = request.getAmountPaid() != null ? request.getAmountPaid() : payment.getAmountDue();
        LocalDate paidDate = request.getPaidDate() != null ? request.getPaidDate() : LocalDate.now();

        payment.setAmountPaid(amountPaid);
        payment.setPaidDate(paidDate);
        payment.setStatus(amountPaid.compareTo(payment.getAmountDue()) >= 0 ? PaymentStatus.PAID : PaymentStatus.PENDING);

        return RentPaymentResponse.fromEntity(rentPaymentRepository.save(payment));
    }
}
