package com.daoninhthai.booking.repository;

import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByProviderId(Long providerId);

    List<Booking> findByProviderIdAndBookingDate(Long providerId, LocalDate bookingDate);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.provider.id = :providerId AND b.bookingDate = :date " +
            "AND b.status NOT IN ('CANCELLED') " +
            "AND ((b.startTime <= :startTime AND b.endTime > :startTime) " +
            "OR (b.startTime < :endTime AND b.endTime >= :endTime) " +
            "OR (b.startTime >= :startTime AND b.endTime <= :endTime))")
    List<Booking> findConflictingBookings(
            @Param("providerId") Long providerId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    List<Booking> findByCustomerIdAndStatus(Long customerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.provider.id = :providerId AND b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByProviderIdAndDateRange(
            @Param("providerId") Long providerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    long countByProviderIdAndStatus(Long providerId, BookingStatus status);
}
