package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.RecurringBookingRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.RecurringBookingResponse;
import com.daoninhthai.booking.security.CustomUserDetails;
import com.daoninhthai.booking.service.RecurringBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-bookings")
@RequiredArgsConstructor
public class RecurringBookingController {

    private final RecurringBookingService recurringBookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RecurringBookingRequest request) {
        RecurringBookingResponse response = recurringBookingService.create(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring booking created", response));
    }

    @GetMapping("/{id}/occurrences")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getOccurrences(@PathVariable Long id) {
        List<LocalDate> occurrences = recurringBookingService.generateOccurrences(id);
        return ResponseEntity.ok(ApiResponse.success(occurrences));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> cancel(@PathVariable Long id) {
        RecurringBookingResponse response = recurringBookingService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring booking cancelled", response));
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<RecurringBookingResponse>> pause(@PathVariable Long id) {
        RecurringBookingResponse response = recurringBookingService.pause(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring booking paused", response));
    }

    @GetMapping("/my-recurring")
    public ResponseEntity<ApiResponse<List<RecurringBookingResponse>>> getMyRecurring(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<RecurringBookingResponse> bookings = recurringBookingService.getByCustomer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
}
