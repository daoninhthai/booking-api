package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.TimeSlotRequest;
import com.daoninhthai.booking.dto.response.TimeSlotResponse;
import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.entity.Provider;
import com.daoninhthai.booking.entity.TimeSlot;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.BookingRepository;
import com.daoninhthai.booking.repository.ProviderRepository;
import com.daoninhthai.booking.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final ProviderRepository providerRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public TimeSlotResponse createTimeSlot(TimeSlotRequest request) {
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", request.getProviderId()));

        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        TimeSlot timeSlot = TimeSlot.builder()
                .provider(provider)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .available(true)
                .build();

        timeSlot = timeSlotRepository.save(timeSlot);
        return mapToResponse(timeSlot);
    }

    @Cacheable(value = "availability", key = "#providerId + '-' + #date")
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAvailableSlots(Long providerId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        List<TimeSlot> slots = timeSlotRepository.findAvailableSlots(providerId, dayOfWeek);

        // Filter out slots that already have bookings for the given date
        List<Booking> existingBookings = bookingRepository.findByProviderIdAndBookingDate(providerId, date);

        return slots.stream()
                .filter(slot -> existingBookings.stream().noneMatch(booking ->
                        isOverlapping(slot.getStartTime(), slot.getEndTime(),
                                booking.getStartTime(), booking.getEndTime())))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "availability", allEntries = true)
    @Transactional
    public TimeSlotResponse blockSlot(Long slotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));

        timeSlot.setAvailable(false);
        timeSlot = timeSlotRepository.save(timeSlot);
        return mapToResponse(timeSlot);
    }

    @CacheEvict(value = "availability", allEntries = true)
    @Transactional
    public TimeSlotResponse updateAvailability(Long slotId, boolean available) {
        TimeSlot timeSlot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", slotId));

        timeSlot.setAvailable(available);
        timeSlot = timeSlotRepository.save(timeSlot);
        return mapToResponse(timeSlot);
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getSlotsByProvider(Long providerId) {
        return timeSlotRepository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private boolean isOverlapping(java.time.LocalTime start1, java.time.LocalTime end1,
                                  java.time.LocalTime start2, java.time.LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private TimeSlotResponse mapToResponse(TimeSlot timeSlot) {
        return TimeSlotResponse.builder()
                .id(timeSlot.getId())
                .providerId(timeSlot.getProvider().getId())
                .dayOfWeek(timeSlot.getDayOfWeek())
                .startTime(timeSlot.getStartTime())
                .endTime(timeSlot.getEndTime())
                .available(timeSlot.getAvailable())
                .build();
    }
}
