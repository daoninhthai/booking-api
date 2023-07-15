package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.BookingRequest;
import com.daoninhthai.booking.dto.request.WaitlistRequest;
import com.daoninhthai.booking.dto.response.WaitlistResponse;
import com.daoninhthai.booking.entity.*;
import com.daoninhthai.booking.enums.WaitlistStatus;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistService.class);

    private final WaitlistRepository waitlistRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final BookingService bookingService;
    private final NotificationService notificationService;

    @Transactional
    public WaitlistResponse addToWaitlist(Long customerId, WaitlistRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", request.getProviderId()));

        WaitlistEntry entry = WaitlistEntry.builder()
                .customer(customer)
                .service(service)
                .provider(provider)
                .preferredDate(request.getPreferredDate())
                .preferredTime(request.getPreferredTime())
                .status(WaitlistStatus.WAITING)
                .build();

        entry = waitlistRepository.save(entry);
        logger.info("Added to waitlist: id={}, customer={}, provider={}, date={}",
                entry.getId(), customerId, request.getProviderId(), request.getPreferredDate());

        return mapToResponse(entry);
    }

    @Transactional
    public void notifyWhenAvailable(Long providerId, LocalDate date) {
        List<WaitlistEntry> waitingEntries = waitlistRepository
                .findByProviderIdAndPreferredDateAndStatus(providerId, date, WaitlistStatus.WAITING);

        for (WaitlistEntry entry : waitingEntries) {
            entry.setStatus(WaitlistStatus.NOTIFIED);
            waitlistRepository.save(entry);
            logger.info("Notified waitlist entry: id={}, customer={}", entry.getId(), entry.getCustomer().getId());
        }
    }

    @Transactional
    public void autoBook(Long waitlistEntryId) {
        WaitlistEntry entry = waitlistRepository.findById(waitlistEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("WaitlistEntry", "id", waitlistEntryId));

        if (entry.getStatus() != WaitlistStatus.WAITING && entry.getStatus() != WaitlistStatus.NOTIFIED) {
            return;
        }

        try {
            BookingRequest bookingRequest = new BookingRequest();
            bookingRequest.setServiceId(entry.getService().getId());
            bookingRequest.setProviderId(entry.getProvider().getId());
            bookingRequest.setBookingDate(entry.getPreferredDate());
            bookingRequest.setStartTime(entry.getPreferredTime());
            bookingRequest.setNotes("Auto-booked from waitlist #" + entry.getId());

            bookingService.createBooking(entry.getCustomer().getId(), bookingRequest);

            entry.setStatus(WaitlistStatus.BOOKED);
            waitlistRepository.save(entry);
            logger.info("Auto-booked from waitlist: entryId={}, customerId={}", entry.getId(), entry.getCustomer().getId());
        } catch (Exception e) {
            logger.warn("Failed to auto-book from waitlist: entryId={}, error={}", entry.getId(), e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<WaitlistResponse> getByCustomer(Long customerId) {
        return waitlistRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Scheduled job to expire old waitlist entries.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireOldEntries() {
        LocalDate today = LocalDate.now();
        List<WaitlistEntry> expiredEntries = waitlistRepository
                .findByPreferredDateBeforeAndStatus(today, WaitlistStatus.WAITING);

        for (WaitlistEntry entry : expiredEntries) {
            entry.setStatus(WaitlistStatus.EXPIRED);
            waitlistRepository.save(entry);
        }

        if (!expiredEntries.isEmpty()) {
            logger.info("Expired {} waitlist entries", expiredEntries.size());
        }
    }

    private WaitlistResponse mapToResponse(WaitlistEntry entry) {
        return WaitlistResponse.builder()
                .id(entry.getId())
                .customerId(entry.getCustomer().getId())
                .customerName(entry.getCustomer().getFullName())
                .serviceId(entry.getService().getId())
                .serviceName(entry.getService().getName())
                .providerId(entry.getProvider().getId())
                .providerName(entry.getProvider().getBusinessName())
                .preferredDate(entry.getPreferredDate())
                .preferredTime(entry.getPreferredTime())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
