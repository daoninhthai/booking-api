package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.PromotionRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.PromotionResponse;
import com.daoninhthai.booking.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(
            @Valid @RequestBody PromotionRequest request) {
        PromotionResponse response = promotionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promotion created", response));
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<ApiResponse<PromotionResponse>> validate(@PathVariable String code) {
        PromotionResponse response = promotionService.validate(code);
        return ResponseEntity.ok(ApiResponse.success("Promotion is valid", response));
    }

    @PostMapping("/apply/{code}")
    public ResponseEntity<ApiResponse<BigDecimal>> apply(
            @PathVariable String code,
            @RequestParam BigDecimal originalPrice) {
        BigDecimal discountedPrice = promotionService.apply(code, originalPrice);
        return ResponseEntity.ok(ApiResponse.success("Promotion applied", discountedPrice));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromotionResponse>> deactivate(@PathVariable Long id) {
        PromotionResponse response = promotionService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Promotion deactivated", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions() {
        List<PromotionResponse> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(ApiResponse.success(promotions));
    }
}
