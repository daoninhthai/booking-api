package com.daoninhthai.booking.dto.response;

import com.daoninhthai.booking.enums.WaitlistStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long serviceId;
    private String serviceName;
    private Long providerId;
    private String providerName;
    private LocalDate preferredDate;
    private LocalTime preferredTime;
    private WaitlistStatus status;
    private LocalDateTime createdAt;
}
