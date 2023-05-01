package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.ReviewRequest;
import com.daoninhthai.booking.dto.response.ReviewResponse;
import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.entity.Provider;
import com.daoninhthai.booking.entity.Review;
import com.daoninhthai.booking.entity.User;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.BookingRepository;
import com.daoninhthai.booking.repository.ProviderRepository;
import com.daoninhthai.booking.repository.ReviewRepository;
import com.daoninhthai.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(Long customerId, ReviewRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Can only review completed bookings");
        }

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You can only review your own bookings");
        }

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new BadRequestException("You have already reviewed this booking");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", customerId));

        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .provider(booking.getProvider())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);

        // Update provider's average rating
        updateProviderRating(booking.getProvider().getId());

        return mapToResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProvider(Long providerId) {
        return reviewRepository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long providerId) {
        Double avg = reviewRepository.getAverageRatingByProviderId(providerId);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public boolean canReview(Long customerId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) return false;

        return booking.getStatus() == BookingStatus.COMPLETED
                && booking.getCustomer().getId().equals(customerId)
                && !reviewRepository.existsByBookingId(bookingId);
    }

    private void updateProviderRating(Long providerId) {
        Double averageRating = reviewRepository.getAverageRatingByProviderId(providerId);
        if (averageRating != null) {
            Provider provider = providerRepository.findById(providerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", providerId));
            provider.setRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
            providerRepository.save(provider);
        }
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .bookingId(review.getBooking().getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getFullName())
                .providerId(review.getProvider().getId())
                .providerName(review.getProvider().getBusinessName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
