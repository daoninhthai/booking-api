package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.WaitlistRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.WaitlistResponse;
import com.daoninhthai.booking.security.CustomUserDetails;
import com.daoninhthai.booking.service.WaitlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    public ResponseEntity<ApiResponse<WaitlistResponse>> addToWaitlist(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody WaitlistRequest request) {
        WaitlistResponse response = waitlistService.addToWaitlist(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Added to waitlist", response));
    }

    @PostMapping("/{id}/auto-book")
    public ResponseEntity<ApiResponse<Void>> autoBook(@PathVariable Long id) {
        waitlistService.autoBook(id);
        return ResponseEntity.ok(ApiResponse.success("Auto-booking attempted", null));
    }

    @GetMapping("/my-waitlist")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getMyWaitlist(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<WaitlistResponse> entries = waitlistService.getByCustomer(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(entries));
    }
}
