package com.daoninhthai.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    private String description;

    private String phone;

    private String address;

    private List<Long> serviceIds;
}
