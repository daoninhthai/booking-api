package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.BookingRequest;
import com.daoninhthai.booking.dto.response.BookingResponse;
import com.daoninhthai.booking.entity.*;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponse createBooking(Long customerId, BookingRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", request.getProviderId()));

        if (!service.getActive()) {
            throw new BadRequestException("Service is not currently available");
        }

        if (!provider.getActive()) {
            throw new BadRequestException("Provider is not currently available");
        }

        LocalTime endTime = request.getStartTime().plusMinutes(service.getDuration());

        // Check for conflicting bookings
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                request.getProviderId(), request.getBookingDate(),
                request.getStartTime(), endTime);

        if (!conflicts.isEmpty()) {
            throw new BadRequestException("The requested time slot is not available. There is a booking conflict.");
        }

        // Reserve time slot if provided
        TimeSlot timeSlot = null;
        if (request.getTimeSlotId() != null) {
            timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
                    .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", "id", request.getTimeSlotId()));

            if (!timeSlot.getAvailable()) {
                throw new BadRequestException("Time slot is not available");
            }
        }

        Booking booking = Booking.builder()
                .service(service)
                .provider(provider)
                .customer(customer)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(BookingStatus.PENDING)
                .notes(request.getNotes())
                .totalPrice(service.getPrice())
                .build();

        booking = bookingRepository.save(booking);
        logger.info("Booking created: id={}, customer={}, provider={}, date={}",
                booking.getId(), customerId, request.getProviderId(), request.getBookingDate());

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = getBookingEntity(bookingId);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);
        logger.info("Booking confirmed: id={}", bookingId);

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = getBookingEntity(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed booking");
        }

        // Cancellation policy: must cancel at least 24 hours before booking date
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        if (booking.getBookingDate().isBefore(tomorrow) &&
                booking.getCustomer().getId().equals(userId)) {
            throw new BadRequestException("Bookings must be cancelled at least 24 hours in advance");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // Release the time slot if reserved
        if (booking.getTimeSlot() != null) {
            TimeSlot timeSlot = booking.getTimeSlot();
            timeSlot.setAvailable(true);
            timeSlotRepository.save(timeSlot);
        }

        booking = bookingRepository.save(booking);
        logger.info("Booking cancelled: id={}, by user={}", bookingId, userId);

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse completeBooking(Long bookingId) {
        Booking booking = getBookingEntity(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed bookings can be completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        booking = bookingRepository.save(booking);
        logger.info("Booking completed: id={}", bookingId);

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = getBookingEntity(bookingId);
        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByProvider(Long providerId) {
        return bookingRepository.findByProviderId(providerId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByProviderAndDate(Long providerId, LocalDate date) {
        return bookingRepository.findByProviderIdAndBookingDate(providerId, date).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Booking getBookingEntity(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));
    }
}
