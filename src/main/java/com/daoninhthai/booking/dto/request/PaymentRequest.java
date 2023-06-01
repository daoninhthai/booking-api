package com.daoninhthai.booking.dto.request;

import com.daoninhthai.booking.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}
