package com.daoninhthai.booking.repository;

import com.daoninhthai.booking.entity.RecurringBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringBookingRepository extends JpaRepository<RecurringBooking, Long> {

    List<RecurringBooking> findByCustomerId(Long customerId);

    List<RecurringBooking> findByActiveTrue();

    List<RecurringBooking> findByProviderIdAndActiveTrue(Long providerId);
}
