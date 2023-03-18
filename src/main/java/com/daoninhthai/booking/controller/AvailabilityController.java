package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.TimeSlotRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.TimeSlotResponse;
import com.daoninhthai.booking.service.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final TimeSlotService timeSlotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getAvailableSlots(
            @RequestParam Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TimeSlotResponse> slots = timeSlotService.getAvailableSlots(providerId, date);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }

    @PostMapping("/slots")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> createSlot(@Valid @RequestBody TimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.createTimeSlot(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Time slot created successfully", response));
    }

    @PutMapping("/slots/{id}/block")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> blockSlot(@PathVariable Long id) {
        TimeSlotResponse response = timeSlotService.blockSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Time slot blocked", response));
    }

    @PutMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TimeSlotResponse>> updateSlotAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        TimeSlotResponse response = timeSlotService.updateAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.success("Time slot updated", response));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<TimeSlotResponse>>> getProviderSlots(
            @PathVariable Long providerId) {
        List<TimeSlotResponse> slots = timeSlotService.getSlotsByProvider(providerId);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}
