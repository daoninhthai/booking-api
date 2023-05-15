package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.response.ProviderResponse;
import com.daoninhthai.booking.dto.response.ServiceResponse;
import com.daoninhthai.booking.entity.Provider;
import com.daoninhthai.booking.entity.ServiceEntity;
import com.daoninhthai.booking.repository.ProviderRepository;
import com.daoninhthai.booking.repository.ReviewRepository;
import com.daoninhthai.booking.repository.ServiceRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ServiceRepository serviceRepository;
    private final ProviderRepository providerRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ServiceResponse> searchServices(String keyword, String category,
                                                 BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<ServiceEntity> spec = buildServiceSpecification(keyword, category, minPrice, maxPrice);
        return serviceRepository.findAll(spec).stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> searchProviders(String location, Long serviceId,
                                                   BigDecimal minRating) {
        Specification<Provider> spec = buildProviderSpecification(location, serviceId, minRating);
        return providerRepository.findAll(spec).stream()
                .map(this::mapProviderToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> advancedSearchServices(String keyword, String category,
                                                         BigDecimal minPrice, BigDecimal maxPrice,
                                                         Integer minDuration, Integer maxDuration) {
        Specification<ServiceEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (keyword != null && !keyword.isBlank()) {
                Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
                Predicate descLike = cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
                predicates.add(cb.or(nameLike, descLike));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minDuration != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("duration"), minDuration));
            }

            if (maxDuration != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("duration"), maxDuration));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return serviceRepository.findAll(spec).stream()
                .map(this::mapServiceToResponse)
                .collect(Collectors.toList());
    }

    private Specification<ServiceEntity> buildServiceSpecification(String keyword, String category,
                                                                    BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (keyword != null && !keyword.isBlank()) {
                Predicate nameLike = cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%");
                Predicate descLike = cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
                predicates.add(cb.or(nameLike, descLike));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<Provider> buildProviderSpecification(String location, Long serviceId,
                                                                BigDecimal minRating) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("active")));

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("address")), "%" + location.toLowerCase() + "%"));
            }

            if (serviceId != null) {
                predicates.add(cb.isMember(serviceId, root.get("services").get("id")));
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), minRating));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private ServiceResponse mapServiceToResponse(ServiceEntity service) {
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

    private ProviderResponse mapProviderToResponse(Provider provider) {
        List<com.daoninhthai.booking.dto.response.ServiceResponse> serviceResponses =
                provider.getServices().stream()
                        .map(this::mapServiceToResponse)
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
