package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.ServiceRequest;
import com.daoninhthai.booking.dto.response.ServiceResponse;
import com.daoninhthai.booking.entity.ServiceEntity;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.BookingRepository;
import com.daoninhthai.booking.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        ServiceEntity service = ServiceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .duration(request.getDuration())
                .price(request.getPrice())
                .category(request.getCategory())
                .active(true)
                .build();

        service = serviceRepository.save(service);
        return mapToResponse(service);
    }

    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setDuration(request.getDuration());
        service.setPrice(request.getPrice());
        service.setCategory(request.getCategory());

        service = serviceRepository.save(service);
        return mapToResponse(service);
    }

    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        return mapToResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllActiveServices() {
        return serviceRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getServicesByCategory(String category) {
        return serviceRepository.findByCategoryAndActiveTrue(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> searchServicesByName(String name) {
        return serviceRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getPopularServices(int limit) {
        List<ServiceEntity> allServices = serviceRepository.findByActiveTrue();

        return allServices.stream()
                .sorted(Comparator.comparingLong(
                        (ServiceEntity s) -> bookingRepository.findAll().stream()
                                .filter(b -> b.getService().getId().equals(s.getId()))
                                .count()
                ).reversed())
                .limit(limit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateService(Long id) {
        ServiceEntity service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        service.setActive(false);
        serviceRepository.save(service);
    }

    private ServiceResponse mapToResponse(ServiceEntity service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .duration(service.getDuration())
                .price(service.getPrice())
                .category(service.getCategory())
                .active(service.getActive())
                .build();
    }
}
