package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.ProviderResponse;
import com.daoninhthai.booking.dto.response.ServiceResponse;
import com.daoninhthai.booking.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/services")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> searchServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<ServiceResponse> services = searchService.searchServices(keyword, category, minPrice, maxPrice);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<ProviderResponse>>> searchProviders(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) BigDecimal minRating) {
        List<ProviderResponse> providers = searchService.searchProviders(location, serviceId, minRating);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/services/advanced")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> advancedSearchServices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDuration,
            @RequestParam(required = false) Integer maxDuration) {
        List<ServiceResponse> services = searchService.advancedSearchServices(
                keyword, category, minPrice, maxPrice, minDuration, maxDuration);
        return ResponseEntity.ok(ApiResponse.success(services));
    }
}
