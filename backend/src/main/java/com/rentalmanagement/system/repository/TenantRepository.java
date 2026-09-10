package com.rentalmanagement.system.repository;

import com.rentalmanagement.system.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Tenant> findByPropertyId(Long propertyId);
    List<Tenant> findByLeaseEndDateBetween(LocalDate start, LocalDate end);
}
