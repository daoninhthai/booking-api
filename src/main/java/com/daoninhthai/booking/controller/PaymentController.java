package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.PaymentRequest;
import com.daoninhthai.booking.dto.response.ApiResponse;
import com.daoninhthai.booking.dto.response.PaymentResponse;
import com.daoninhthai.booking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment processed successfully", response));
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(@PathVariable Long id) {
        PaymentResponse response = paymentService.refundPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Payment refunded", response));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentStatus(@PathVariable Long bookingId) {
        PaymentResponse response = paymentService.getPaymentStatus(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ref/{transactionRef}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByRef(@PathVariable String transactionRef) {
        PaymentResponse response = paymentService.getPaymentByRef(transactionRef);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
