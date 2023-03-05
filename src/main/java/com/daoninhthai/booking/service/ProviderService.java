package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.ProviderRequest;
import com.daoninhthai.booking.dto.response.ProviderResponse;
import com.daoninhthai.booking.dto.response.ServiceResponse;
import com.daoninhthai.booking.entity.Provider;
import com.daoninhthai.booking.entity.ServiceEntity;
import com.daoninhthai.booking.entity.User;
import com.daoninhthai.booking.enums.Role;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.ProviderRepository;
import com.daoninhthai.booking.repository.ServiceRepository;
import com.daoninhthai.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    @Transactional
    public ProviderResponse registerProvider(Long userId, ProviderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (providerRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("User is already registered as a provider");
        }

        user.setRole(Role.PROVIDER);
        userRepository.save(user);

        List<ServiceEntity> services = new ArrayList<>();
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            services = serviceRepository.findAllById(request.getServiceIds());
        }

        Provider provider = Provider.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .address(request.getAddress())
                .rating(BigDecimal.ZERO)
                .active(true)
                .services(services)
                .build();

        provider = providerRepository.save(provider);
        return mapToResponse(provider);
    }

    @Transactional
    public ProviderResponse updateProvider(Long providerId, ProviderRequest request) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", providerId));

        provider.setBusinessName(request.getBusinessName());
        provider.setDescription(request.getDescription());
        provider.setPhone(request.getPhone());
        provider.setAddress(request.getAddress());

        if (request.getServiceIds() != null) {
            List<ServiceEntity> services = serviceRepository.findAllById(request.getServiceIds());
            provider.setServices(services);
        }

        provider = providerRepository.save(provider);
        return mapToResponse(provider);
    }

    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "id", id));
        return mapToResponse(provider);
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> getAllActiveProviders() {
        return providerRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> searchProviders(String keyword) {
        return providerRepository.findByBusinessNameContainingIgnoreCase(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> getProvidersByService(Long serviceId) {
        return providerRepository.findByServiceId(serviceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProviderResponse getProviderByUserId(Long userId) {
        Provider provider = providerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider", "userId", userId));
        return mapToResponse(provider);
    }

    private ProviderResponse mapToResponse(Provider provider) {
        List<ServiceResponse> serviceResponses = provider.getServices().stream()
                .map(s -> ServiceResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .duration(s.getDuration())
                        .price(s.getPrice())
                        .category(s.getCategory())
                        .active(s.getActive())
                        .build())
                .collect(Collectors.toList());

        return ProviderResponse.builder()
                .id(provider.getId())
                .userId(provider.getUser().getId())
                .businessName(provider.getBusinessName())
                .description(provider.getDescription())
                .phone(provider.getPhone())
                .address(provider.getAddress())
                .rating(provider.getRating())
                .active(provider.getActive())
                .services(serviceResponses)
                .build();
    }
}
