package com.daoninhthai.booking.dto.request;

import com.daoninhthai.booking.enums.RecurringFrequency;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Preferred time is required")
    private LocalTime preferredTime;

    @NotNull(message = "Frequency is required")
    private RecurringFrequency frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
