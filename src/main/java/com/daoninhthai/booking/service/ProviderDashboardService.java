package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.response.*;
import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.repository.BookingRepository;
import com.daoninhthai.booking.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderDashboardService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getBookingStats(Long providerId) {
        long total = bookingRepository.findByProviderId(providerId).size();
        long pending = bookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.PENDING);
        long confirmed = bookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.CONFIRMED);
        long completed = bookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.COMPLETED);
        long cancelled = bookingRepository.countByProviderIdAndStatus(providerId, BookingStatus.CANCELLED);

        BigDecimal totalRevenue = bookingRepository.findByProviderId(providerId).stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Double avgRating = reviewRepository.getAverageRatingByProviderId(providerId);
        long totalReviews = reviewRepository.countByProviderId(providerId);

        return DashboardStatsResponse.builder()
                .totalBookings(total)
                .pendingBookings(pending)
                .confirmedBookings(confirmed)
                .completedBookings(completed)
                .cancelledBookings(cancelled)
                .totalRevenue(totalRevenue)
                .averageRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RevenueResponse> getRevenueByPeriod(Long providerId, LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingRepository.findByProviderIdAndDateRange(providerId, startDate, endDate);

        Map<LocalDate, List<Booking>> grouped = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.groupingBy(Booking::getBookingDate));

        List<RevenueResponse> revenueList = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            List<Booking> dayBookings = grouped.getOrDefault(current, Collections.emptyList());
            BigDecimal dayRevenue = dayBookings.stream()
                    .map(Booking::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            revenueList.add(RevenueResponse.builder()
                    .date(current)
                    .revenue(dayRevenue)
                    .bookingCount(dayBookings.size())
                    .build());

            current = current.plusDays(1);
        }

        return revenueList;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getTopServices(Long providerId, int limit) {
        List<Booking> completedBookings = bookingRepository.findByProviderId(providerId).stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .toList();

        Map<Long, Long> serviceBookingCount = completedBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getService().getId(), Collectors.counting()));

        return serviceBookingCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Booking sample = completedBookings.stream()
                            .filter(b -> b.getService().getId().equals(entry.getKey()))
                            .findFirst().orElseThrow();
                    return ServiceResponse.builder()
                            .id(sample.getService().getId())
                            .name(sample.getService().getName())
                            .description(sample.getService().getDescription())
                            .duration(sample.getService().getDuration())
                            .price(sample.getService().getPrice())
                            .category(sample.getService().getCategory())
                            .active(sample.getService().getActive())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public double getCustomerRetentionRate(Long providerId) {
        List<Booking> allBookings = bookingRepository.findByProviderId(providerId);

        Map<Long, Long> customerBookingCount = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getCustomer().getId(), Collectors.counting()));

        long totalCustomers = customerBookingCount.size();
        if (totalCustomers == 0) return 0.0;

        long returningCustomers = customerBookingCount.values().stream()
                .filter(count -> count > 1)
                .count();

        return (double) returningCustomers / totalCustomers * 100.0;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(Long providerId, int limit) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByProviderId(providerId).stream()
                .filter(b -> !b.getBookingDate().isBefore(today))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.PENDING)
                .sorted(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getStartTime))
                .limit(limit)
                .map(b -> BookingResponse.builder()
                        .id(b.getId())
                        .serviceId(b.getService().getId())
                        .serviceName(b.getService().getName())
                        .providerId(b.getProvider().getId())
                        .providerName(b.getProvider().getBusinessName())
                        .customerId(b.getCustomer().getId())
                        .customerName(b.getCustomer().getFullName())
                        .bookingDate(b.getBookingDate())
                        .startTime(b.getStartTime())
                        .endTime(b.getEndTime())
                        .status(b.getStatus())
                        .totalPrice(b.getTotalPrice())
                        .createdAt(b.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
