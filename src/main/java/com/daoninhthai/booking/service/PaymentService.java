package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.PaymentRequest;
import com.daoninhthai.booking.dto.response.PaymentResponse;
import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.entity.Payment;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.enums.PaymentStatus;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.exception.ResourceNotFoundException;
import com.daoninhthai.booking.repository.BookingRepository;
import com.daoninhthai.booking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Cannot process payment for a cancelled booking");
        }

        // Check if payment already exists for this booking
        if (paymentRepository.findByBookingId(request.getBookingId()).isPresent()) {
            throw new BadRequestException("Payment already exists for this booking");
        }

        String transactionRef = generateTransactionRef();

        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .amount(booking.getTotalPrice())
                .method(request.getMethod())
                .status(PaymentStatus.COMPLETED)
                .transactionRef(transactionRef)
                .paidAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        // Auto-confirm booking after successful payment
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
        }

        logger.info("Payment processed: id={}, bookingId={}, amount={}, ref={}",
                payment.getId(), booking.getId(), payment.getAmount(), transactionRef);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only completed payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        logger.info("Payment refunded: id={}, bookingId={}, amount={}",
                payment.getId(), payment.getBookingId(), payment.getAmount());

        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentStatus(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));
        return mapToResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByRef(String transactionRef) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionRef", transactionRef));
        return mapToResponse(payment);
    }

    private String generateTransactionRef() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionRef(payment.getTransactionRef())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
