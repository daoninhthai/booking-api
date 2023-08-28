package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.PromotionRequest;
import com.daoninhthai.booking.dto.response.PromotionResponse;
import com.daoninhthai.booking.entity.Promotion;
import com.daoninhthai.booking.enums.DiscountType;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private static final Logger logger = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;

    @Transactional
    public PromotionResponse create(PromotionRequest request) {
        if (promotionRepository.existsByCode(request.getCode().toUpperCase())) {
            throw new BadRequestException("Promotion code already exists");
        }

        if (request.getValidFrom().isAfter(request.getValidTo())) {
            throw new BadRequestException("Valid from date must be before valid to date");
        }

        Promotion promotion = Promotion.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxUses(request.getMaxUses())
                .usedCount(0)
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .active(true)
                .build();

        promotion = promotionRepository.save(promotion);
        logger.info("Promotion created: code={}", promotion.getCode());

        return mapToResponse(promotion);
    }

    @Transactional(readOnly = true)
    public PromotionResponse validate(String code) {
        Promotion promotion = promotionRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "code", code));

        if (!promotion.getActive()) {
            throw new BadRequestException("Promotion is no longer active");
        }

        LocalDate today = LocalDate.now();
        if (today.isBefore(promotion.getValidFrom()) || today.isAfter(promotion.getValidTo())) {
            throw new BadRequestException("Promotion is not valid at this time");
        }

        if (promotion.getMaxUses() != null && promotion.getUsedCount() >= promotion.getMaxUses()) {
            throw new BadRequestException("Promotion has reached maximum usage");
        }

        return mapToResponse(promotion);
    }

    @Transactional
    public BigDecimal apply(String code, BigDecimal originalPrice) {
        Promotion promotion = promotionRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "code", code));

        // Validate again before applying
        validate(code);

        BigDecimal discountedPrice;
        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            BigDecimal discount = originalPrice.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountedPrice = originalPrice.subtract(discount);
        } else {
            discountedPrice = originalPrice.subtract(promotion.getDiscountValue());
        }

        // Ensure price doesn't go below zero
        if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
            discountedPrice = BigDecimal.ZERO;
        }

        // Increment usage count
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);

        logger.info("Promotion applied: code={}, original={}, discounted={}",
                code, originalPrice, discountedPrice);

        return discountedPrice;
    }

    @Transactional
    public PromotionResponse deactivate(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", "id", id));

        promotion.setActive(false);
        promotion = promotionRepository.save(promotion);
        logger.info("Promotion deactivated: code={}", promotion.getCode());

        return mapToResponse(promotion);
    }

    @Transactional(readOnly = true)
    public List<PromotionResponse> getActivePromotions() {
        return promotionRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PromotionResponse mapToResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .code(promotion.getCode())
                .description(promotion.getDescription())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .maxUses(promotion.getMaxUses())
                .usedCount(promotion.getUsedCount())
                .validFrom(promotion.getValidFrom())
                .validTo(promotion.getValidTo())
                .active(promotion.getActive())
                .build();
    }
}
