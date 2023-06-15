package com.daoninhthai.booking.dto.response;

import com.daoninhthai.booking.enums.RecurringFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringBookingResponse {

    private Long id;
    private Long customerId;
    private Long serviceId;
    private String serviceName;
    private Long providerId;
    private String providerName;
    private LocalTime preferredTime;
    private RecurringFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
