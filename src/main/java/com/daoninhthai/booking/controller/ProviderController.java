package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.ProviderRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.ProviderResponse;
import com.daoninhthai.booking.security.CustomUserDetails;
import com.daoninhthai.booking.service.ProviderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProviderResponse>> registerProvider(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProviderRequest request) {
        ProviderResponse response = providerService.registerProvider(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Provider registered successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProviderResponse>> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ProviderRequest request) {
        ProviderResponse response = providerService.updateProvider(id, request);
        return ResponseEntity.ok(ApiResponse.success("Provider updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProviderResponse>> getProvider(@PathVariable Long id) {
        ProviderResponse response = providerService.getProviderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProviderResponse>>> getAllProviders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long serviceId) {
        List<ProviderResponse> providers;
        if (search != null) {
            providers = providerService.searchProviders(search);
        } else if (serviceId != null) {
            providers = providerService.getProvidersByService(serviceId);
        } else {
            providers = providerService.getAllActiveProviders();
        }
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProviderResponse>> getMyProviderProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProviderResponse response = providerService.getProviderByUserId(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
