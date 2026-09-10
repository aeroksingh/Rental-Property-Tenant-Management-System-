package com.rentalmanagement.system.repository;

import com.rentalmanagement.system.entity.RentPayment;
import com.rentalmanagement.system.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RentPaymentRepository extends JpaRepository<RentPayment, Long> {
    List<RentPayment> findByTenantId(Long tenantId);
    List<RentPayment> findByPropertyId(Long propertyId);
    List<RentPayment> findByStatus(PaymentStatus status);
    List<RentPayment> findByDueDateBeforeAndStatus(LocalDate date, PaymentStatus status);
    List<RentPayment> findByTenantIdAndPropertyId(Long tenantId, Long propertyId);
}
