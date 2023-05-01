package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.ReviewRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.ReviewResponse;
import com.daoninhthai.booking.security.CustomUserDetails;
import com.daoninhthai.booking.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", response));
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProviderReviews(
            @PathVariable Long providerId) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProvider(providerId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/provider/{providerId}/average")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long providerId) {
        Double averageRating = reviewService.getAverageRating(providerId);
        return ResponseEntity.ok(ApiResponse.success(averageRating));
    }

    @GetMapping("/can-review/{bookingId}")
    public ResponseEntity<ApiResponse<Boolean>> canReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookingId) {
        boolean canReview = reviewService.canReview(userDetails.getId(), bookingId);
        return ResponseEntity.ok(ApiResponse.success(canReview));
    }
}
