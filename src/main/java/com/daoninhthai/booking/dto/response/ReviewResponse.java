package com.daoninhthai.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long providerId;
    private String providerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
