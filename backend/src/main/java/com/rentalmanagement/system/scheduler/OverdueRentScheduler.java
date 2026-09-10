package com.rentalmanagement.system.scheduler;

import com.rentalmanagement.system.entity.RentPayment;
import com.rentalmanagement.system.enums.PaymentStatus;
import com.rentalmanagement.system.repository.RentPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueRentScheduler {

    private final RentPaymentRepository rentPaymentRepository;

    /**
     * Runs daily at 1:00 AM server time. Finds every RentPayment still PENDING
     * whose dueDate has passed and flips it to OVERDUE.
     *
     * Note: this only ever moves PENDING -> OVERDUE. A payment already marked
     * PAID is untouched regardless of amountPaid/dueDate, so a late-but-settled
     * payment never gets incorrectly re-flagged.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void flagOverduePayments() {
        LocalDate today = LocalDate.now();
        List<RentPayment> overdueCandidates =
                rentPaymentRepository.findByDueDateBeforeAndStatus(today, PaymentStatus.PENDING);

        if (overdueCandidates.isEmpty()) {
            log.info("Overdue rent check: no pending payments past due date as of {}", today);
            return;
        }

        overdueCandidates.forEach(payment -> payment.setStatus(PaymentStatus.OVERDUE));
        rentPaymentRepository.saveAll(overdueCandidates);

        log.info("Overdue rent check: flagged {} payment(s) as OVERDUE as of {}",
                overdueCandidates.size(), today);
    }
}
