package com.daoninhthai.booking.repository;

import com.daoninhthai.booking.entity.WaitlistEntry;
import com.daoninhthai.booking.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<WaitlistEntry, Long> {

    List<WaitlistEntry> findByProviderIdAndPreferredDateAndStatus(
            Long providerId, LocalDate preferredDate, WaitlistStatus status);

    List<WaitlistEntry> findByCustomerId(Long customerId);

    List<WaitlistEntry> findByStatus(WaitlistStatus status);

    List<WaitlistEntry> findByPreferredDateBeforeAndStatus(LocalDate date, WaitlistStatus status);
}
