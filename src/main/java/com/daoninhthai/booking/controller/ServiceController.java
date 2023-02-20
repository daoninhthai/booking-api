package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.ServiceRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.ServiceResponse;
import com.daoninhthai.booking.service.ServiceCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceCatalogService serviceCatalogService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceCatalogService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServiceResponse>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceCatalogService.updateService(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceResponse>> getService(@PathVariable Long id) {
        ServiceResponse response = serviceCatalogService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getAllServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        List<ServiceResponse> services;
        if (category != null) {
            services = serviceCatalogService.getServicesByCategory(category);
        } else if (name != null) {
            services = serviceCatalogService.searchServicesByName(name);
        } else {
            services = serviceCatalogService.getAllActiveServices();
        }
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ServiceResponse>>> getPopularServices(
            @RequestParam(defaultValue = "10") int limit) {
        List<ServiceResponse> services = serviceCatalogService.getPopularServices(limit);
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateService(@PathVariable Long id) {
        serviceCatalogService.deactivateService(id);
        return ResponseEntity.ok(ApiResponse.success("Service deactivated successfully", null));
    }
}
