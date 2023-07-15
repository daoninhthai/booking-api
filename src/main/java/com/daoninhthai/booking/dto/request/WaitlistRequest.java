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
public class WaitlistRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Preferred date is required")
    private LocalDate preferredDate;

    private LocalTime preferredTime;
}
