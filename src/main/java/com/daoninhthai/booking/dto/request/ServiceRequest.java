package com.daoninhthai.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Service name is required")
    private String name;

    private String description;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    private Integer duration;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Category is required")
    private String category;
}
