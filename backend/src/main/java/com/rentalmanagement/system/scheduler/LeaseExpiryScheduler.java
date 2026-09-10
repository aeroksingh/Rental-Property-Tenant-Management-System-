package com.rentalmanagement.system.scheduler;

import com.rentalmanagement.system.entity.Tenant;
import com.rentalmanagement.system.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaseExpiryScheduler {

    private static final int WARNING_WINDOW_DAYS = 30;

    private final TenantRepository tenantRepository;

    /**
     * Runs daily at 2:00 AM server time (after the overdue-rent job). Finds
     * tenants whose leaseEndDate falls within the next 30 days and logs a
     * renewal reminder for the owner dashboard.
     *
     * TODO: wire this up to an actual notification channel (email/SMS/in-app)
     * once one exists - for now this is the extension point. Consider adding
     * a "lastReminderSentAt" field to Tenant if you want to avoid re-notifying
     * every single day during the 30-day window.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void flagUpcomingLeaseExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate windowEnd = today.plusDays(WARNING_WINDOW_DAYS);

        List<Tenant> expiringLeases = tenantRepository.findByLeaseEndDateBetween(today, windowEnd);

        if (expiringLeases.isEmpty()) {
            log.info("Lease expiry check: no leases expiring in the next {} days", WARNING_WINDOW_DAYS);
            return;
        }

        expiringLeases.forEach(tenant -> log.info(
                "Lease expiry reminder: tenant '{}' (id={}) at property id={} expires on {}",
                tenant.getName(), tenant.getId(),
                tenant.getProperty() != null ? tenant.getProperty().getId() : "N/A",
                tenant.getLeaseEndDate()));

        log.info("Lease expiry check: {} lease(s) expiring within {} days", expiringLeases.size(), WARNING_WINDOW_DAYS);
    }
}
