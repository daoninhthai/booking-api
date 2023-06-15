package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.BookingRequest;
import com.daoninhthai.booking.dto.request.RecurringBookingRequest;
import com.daoninhthai.booking.dto.response.RecurringBookingResponse;
import com.daoninhthai.booking.entity.*;
import com.daoninhthai.booking.enums.RecurringFrequency;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurringBookingService {

    private static final Logger logger = LoggerFactory.getLogger(RecurringBookingService.class);

    private final RecurringBookingRepository recurringBookingRepository;
    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;

    @Transactional
    public RecurringBookingResponse create(Long customerId, RecurringBookingRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", request.getProviderId()));

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }

        RecurringBooking recurring = RecurringBooking.builder()
                .customer(customer)
                .service(service)
                .provider(provider)
                .preferredTime(request.getPreferredTime())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .build();

        recurring = recurringBookingRepository.save(recurring);
        logger.info("Recurring booking created: id={}, customer={}, frequency={}",
                recurring.getId(), customerId, request.getFrequency());

        return mapToResponse(recurring);
    }

    public List<LocalDate> generateOccurrences(Long recurringBookingId) {
        RecurringBooking recurring = recurringBookingRepository.findById(recurringBookingId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", recurringBookingId));

        List<LocalDate> occurrences = new ArrayList<>();
        LocalDate current = recurring.getStartDate();

        while (!current.isAfter(recurring.getEndDate())) {
            occurrences.add(current);
            current = getNextOccurrence(current, recurring.getFrequency());
        }

        return occurrences;
    }

    @Transactional
    public RecurringBookingResponse cancel(Long recurringBookingId) {
        RecurringBooking recurring = recurringBookingRepository.findById(recurringBookingId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", recurringBookingId));

        recurring.setActive(false);
        recurring = recurringBookingRepository.save(recurring);
        logger.info("Recurring booking cancelled: id={}", recurringBookingId);

        return mapToResponse(recurring);
    }

    @Transactional
    public RecurringBookingResponse pause(Long recurringBookingId) {
        RecurringBooking recurring = recurringBookingRepository.findById(recurringBookingId)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringBooking", "id", recurringBookingId));

        recurring.setActive(false);
        recurring = recurringBookingRepository.save(recurring);
        logger.info("Recurring booking paused: id={}", recurringBookingId);

        return mapToResponse(recurring);
    }

    @Transactional(readOnly = true)
    public List<RecurringBookingResponse> getByCustomer(Long customerId) {
        return recurringBookingRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled job to auto-generate bookings from active recurring bookings.
     * Runs every day at 6:00 AM to create bookings for the upcoming week.
     */
    @Scheduled(cron = "0 0 6 * * ?")
    @Transactional
    public void autoGenerateBookings() {
        logger.info("Running recurring booking generation job");

        List<RecurringBooking> activeRecurring = recurringBookingRepository.findByActiveTrue();
        LocalDate today = LocalDate.now();
        LocalDate oneWeekLater = today.plusWeeks(1);
        int generated = 0;

        for (RecurringBooking recurring : activeRecurring) {
            if (recurring.getEndDate().isBefore(today)) {
                recurring.setActive(false);
                recurringBookingRepository.save(recurring);
                continue;
            }

            List<LocalDate> occurrences = generateOccurrencesInRange(recurring, today, oneWeekLater);
            for (LocalDate date : occurrences) {
                try {
                    BookingRequest bookingRequest = new BookingRequest();
                    bookingRequest.setServiceId(recurring.getService().getId());
                    bookingRequest.setProviderId(recurring.getProvider().getId());
                    bookingRequest.setBookingDate(date);
                    bookingRequest.setStartTime(recurring.getPreferredTime());
                    bookingRequest.setNotes("Auto-generated from recurring booking #" + recurring.getId());

                    bookingService.createBooking(recurring.getCustomer().getId(), bookingRequest);
                    generated++;
                } catch (Exception e) {
                    logger.warn("Failed to auto-generate booking for recurring={} on date={}: {}",
                            recurring.getId(), date, e.getMessage());
                }
            }
        }

        logger.info("Recurring booking generation completed. Generated {} bookings.", generated);
    }

    private List<LocalDate> generateOccurrencesInRange(RecurringBooking recurring,
                                                        LocalDate rangeStart, LocalDate rangeEnd) {
        List<LocalDate> occurrences = new ArrayList<>();
        LocalDate current = recurring.getStartDate();

        while (!current.isAfter(recurring.getEndDate()) && !current.isAfter(rangeEnd)) {
            if (!current.isBefore(rangeStart)) {
                occurrences.add(current);
            }
            current = getNextOccurrence(current, recurring.getFrequency());
        }

        return occurrences;
    }

    private LocalDate getNextOccurrence(LocalDate current, RecurringFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> current.plusWeeks(1);
            case BIWEEKLY -> current.plusWeeks(2);
            case MONTHLY -> current.plusMonths(1);
        };
    }

    private RecurringBookingResponse mapToResponse(RecurringBooking recurring) {
        return RecurringBookingResponse.builder()
                .id(recurring.getId())
                .customerId(recurring.getCustomer().getId())
                .serviceId(recurring.getService().getId())
                .serviceName(recurring.getService().getName())
                .providerId(recurring.getProvider().getId())
                .providerName(recurring.getProvider().getBusinessName())
                .preferredTime(recurring.getPreferredTime())
                .frequency(recurring.getFrequency())
                .startDate(recurring.getStartDate())
                .endDate(recurring.getEndDate())
                .active(recurring.getActive())
                .build();
    }
}
