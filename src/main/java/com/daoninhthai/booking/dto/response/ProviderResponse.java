package com.daoninhthai.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderResponse {

    private Long id;
    private Long userId;
    private String businessName;
    private String description;
    private String phone;
    private String address;
    private BigDecimal rating;
    private Boolean active;
    private List<ServiceResponse> services;
}
