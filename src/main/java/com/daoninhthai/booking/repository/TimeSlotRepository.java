package com.daoninhthai.booking.repository;

import com.daoninhthai.booking.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByProviderId(Long providerId);

    List<TimeSlot> findByProviderIdAndDayOfWeek(Long providerId, DayOfWeek dayOfWeek);

    @Query("SELECT ts FROM TimeSlot ts WHERE ts.provider.id = :providerId AND ts.dayOfWeek = :dayOfWeek AND ts.available = true")
    List<TimeSlot> findAvailableSlots(@Param("providerId") Long providerId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

    List<TimeSlot> findByProviderIdAndAvailableTrue(Long providerId);
}
