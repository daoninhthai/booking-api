package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.BookingRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.BookingResponse;
import com.daoninhthai.booking.security.CustomUserDetails;
import com.daoninhthai.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed", response));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BookingResponse response = bookingService.cancelBooking(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", response));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(@PathVariable Long id) {
        BookingResponse response = bookingService.completeBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking completed", response));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BookingResponse> bookings = bookingService.getBookingsByCustomer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getProviderBookings(
            @PathVariable Long providerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<BookingResponse> bookings;
        if (date != null) {
            bookings = bookingService.getBookingsByProviderAndDate(providerId, date);
        } else {
            bookings = bookingService.getBookingsByProvider(providerId);
        }
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
}
