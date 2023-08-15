package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.security.CustomUserDetails;
import com.daoninhthai.booking.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping(value = "/export.ics", produces = "text/calendar")
    public ResponseEntity<String> exportCustomerCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String ical = calendarService.exportCustomerBookings(userDetails.getId());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings.ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ical);
    }

    @GetMapping(value = "/provider/{providerId}/export.ics", produces = "text/calendar")
    public ResponseEntity<String> exportProviderCalendar(@PathVariable Long providerId) {
        String ical = calendarService.exportProviderSchedule(providerId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=schedule.ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ical);
    }
}
