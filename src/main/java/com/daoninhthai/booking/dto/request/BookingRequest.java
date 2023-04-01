package com.daoninhthai.booking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    private Long timeSlotId;

    private String notes;
}
