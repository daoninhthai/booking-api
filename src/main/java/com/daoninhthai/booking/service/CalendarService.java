package com.daoninhthai.booking.service;

import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final DateTimeFormatter ICAL_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final String CRLF = "\r\n";

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public String generateICalForBookings(List<Booking> bookings) {
        StringBuilder ical = new StringBuilder();
        ical.append("BEGIN:VCALENDAR").append(CRLF);
        ical.append("VERSION:2.0").append(CRLF);
        ical.append("PRODID:-//Booking API//EN").append(CRLF);
        ical.append("CALSCALE:GREGORIAN").append(CRLF);
        ical.append("METHOD:PUBLISH").append(CRLF);

        for (Booking booking : bookings) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            LocalDateTime start = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
            LocalDateTime end = LocalDateTime.of(booking.getBookingDate(), booking.getEndTime());

            ical.append("BEGIN:VEVENT").append(CRLF);
            ical.append("UID:").append(UUID.randomUUID()).append("@booking-api").append(CRLF);
            ical.append("DTSTART:").append(start.format(ICAL_FORMAT)).append(CRLF);
            ical.append("DTEND:").append(end.format(ICAL_FORMAT)).append(CRLF);
            ical.append("SUMMARY:").append(escapeIcalText(booking.getService().getName())).append(CRLF);
            ical.append("DESCRIPTION:").append(escapeIcalText(buildDescription(booking))).append(CRLF);
            ical.append("STATUS:").append(mapStatus(booking.getStatus())).append(CRLF);
            ical.append("END:VEVENT").append(CRLF);
        }

        ical.append("END:VCALENDAR").append(CRLF);
        return ical.toString();
    }

    @Transactional(readOnly = true)
    public String exportProviderSchedule(Long providerId) {
        List<Booking> bookings = bookingRepository.findByProviderId(providerId);
        return generateICalForBookings(bookings);
    }

    @Transactional(readOnly = true)
    public String exportCustomerBookings(Long customerId) {
        List<Booking> bookings = bookingRepository.findByCustomerId(customerId);
        return generateICalForBookings(bookings);
    }

    private String buildDescription(Booking booking) {
        StringBuilder desc = new StringBuilder();
        desc.append("Service: ").append(booking.getService().getName());
        desc.append("\\nProvider: ").append(booking.getProvider().getBusinessName());
        desc.append("\\nCustomer: ").append(booking.getCustomer().getFullName());
        desc.append("\\nStatus: ").append(booking.getStatus().name());
        desc.append("\\nPrice: $").append(booking.getTotalPrice());
        if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
            desc.append("\\nNotes: ").append(booking.getNotes());
        }
        return desc.toString();
    }

    private String mapStatus(BookingStatus status) {
        return switch (status) {
            case CONFIRMED -> "CONFIRMED";
            case PENDING -> "TENTATIVE";
            case COMPLETED -> "CONFIRMED";
            default -> "CANCELLED";
        };
    }

    private String escapeIcalText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(",", "\\,")
                .replace(";", "\\;")
                .replace("\n", "\\n");
    }
}
