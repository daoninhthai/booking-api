package com.daoninhthai.booking.dto.response;

import com.daoninhthai.booking.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long serviceId;
    private String serviceName;
    private Long providerId;
    private String providerName;
    private Long customerId;
    private String customerName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private String notes;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
