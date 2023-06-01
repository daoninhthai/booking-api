package com.daoninhthai.booking.dto.response;

import com.daoninhthai.booking.enums.PaymentMethod;
import com.daoninhthai.booking.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionRef;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
