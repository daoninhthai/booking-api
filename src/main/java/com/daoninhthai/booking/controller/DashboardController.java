package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.response.*;
import com.daoninhthai.booking.service.ProviderDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final ProviderDashboardService dashboardService;

    @GetMapping("/stats/{providerId}")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(@PathVariable Long providerId) {
        DashboardStatsResponse stats = dashboardService.getBookingStats(providerId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/revenue/{providerId}")
    public ResponseEntity<ApiResponse<List<RevenueResponse>>> getRevenue(
            @PathVariable Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RevenueResponse> revenue = dashboardService.getRevenueByPeriod(providerId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }

    @GetMapping("/top-services/{providerId}")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getTopServices(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ServiceResponse> topServices = dashboardService.getTopServices(providerId, limit);
        return ResponseEntity.ok(ApiResponse.success(topServices));
    }

    @GetMapping("/retention/{providerId}")
    public ResponseEntity<ApiResponse<Double>> getRetentionRate(@PathVariable Long providerId) {
        double rate = dashboardService.getCustomerRetentionRate(providerId);
        return ResponseEntity.ok(ApiResponse.success(rate));
    }

    @GetMapping("/upcoming/{providerId}")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getUpcomingBookings(
            @PathVariable Long providerId,
            @RequestParam(defaultValue = "10") int limit) {
        List<BookingResponse> bookings = dashboardService.getUpcomingBookings(providerId, limit);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
}
