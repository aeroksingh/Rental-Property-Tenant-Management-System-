package com.rentalmanagement.system.config;

import com.rentalmanagement.system.entity.*;
import com.rentalmanagement.system.enums.*;
import com.rentalmanagement.system.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds demo data on startup so the API is immediately explorable without
 * manually POSTing through Postman/Swagger first. Runs for local/dev H2 runs
 * only - excluded from the "mysql" (production-style) and "test" profiles.
 *
 * Idempotent: skips seeding entirely if any owner already exists, so
 * restarting against a persistent DB won't duplicate data.
 */
@Component
@Profile("!mysql & !test")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final OwnerRepository ownerRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (ownerRepository.count() > 0) {
            log.info("Demo data already present - skipping seed.");
            return;
        }

        String demoPassword = passwordEncoder.encode("password123");

        Owner owner = ownerRepository.save(Owner.builder()
                .name("Alice Owner")
                .email("alice@owner.com")
                .password(demoPassword)
                .phone("9999999999")
                .role(Role.OWNER)
                .build());

        Property occupiedProperty = propertyRepository.save(Property.builder()
                .address("12 MG Road")
                .city("Nashik")
                .type(PropertyType.APARTMENT)
                .rentAmount(new BigDecimal("15000"))
                .isOccupied(true)
                .owner(owner)
                .build());

        Property vacantProperty = propertyRepository.save(Property.builder()
                .address("45 Church Street")
                .city("Pune")
                .type(PropertyType.HOUSE)
                .rentAmount(new BigDecimal("28000"))
                .isOccupied(false)
                .owner(owner)
                .build());

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name("Bob Tenant")
                .email("bob@tenant.com")
                .password(demoPassword)
                .phone("8888888888")
                .property(occupiedProperty)
                .leaseStartDate(LocalDate.now().minusMonths(6))
                .leaseEndDate(LocalDate.now().plusDays(20)) // inside the 30-day expiry window on purpose
                .role(Role.TENANT)
                .build());

        rentPaymentRepository.save(RentPayment.builder()
                .tenant(tenant)
                .property(occupiedProperty)
                .amountDue(new BigDecimal("15000"))
                .amountPaid(new BigDecimal("15000"))
                .dueDate(LocalDate.now().minusMonths(1))
                .paidDate(LocalDate.now().minusMonths(1).plusDays(2))
                .status(PaymentStatus.PAID)
                .build());

        rentPaymentRepository.save(RentPayment.builder()
                .tenant(tenant)
                .property(occupiedProperty)
                .amountDue(new BigDecimal("15000"))
                .amountPaid(BigDecimal.ZERO)
                .dueDate(LocalDate.now().minusDays(5)) // already past due - overdue job will flag this
                .status(PaymentStatus.PENDING)
                .build());

        rentPaymentRepository.save(RentPayment.builder()
                .tenant(tenant)
                .property(occupiedProperty)
                .amountDue(new BigDecimal("15000"))
                .amountPaid(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(25))
                .status(PaymentStatus.PENDING)
                .build());

        maintenanceRequestRepository.save(MaintenanceRequest.builder()
                .tenant(tenant)
                .property(occupiedProperty)
                .title("Leaking kitchen tap")
                .description("Tap has been dripping steadily since yesterday morning.")
                .status(MaintenanceStatus.RAISED)
                .priority(Priority.MEDIUM)
                .build());

        log.info("Seeded demo data: owner={}, properties=[{}, {}], tenant={}",
                owner.getEmail(), occupiedProperty.getAddress(), vacantProperty.getAddress(), tenant.getEmail());
        log.info("Login with alice@owner.com / password123 (OWNER) or bob@tenant.com / password123 (TENANT)");
    }
}
