package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.response.BookingResponse;
import com.daoninhthai.booking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .providerId(booking.getProvider().getId())
                .providerName(booking.getProvider().getBusinessName())
                .customerId(booking.getCustomer().getId())
                .customerName(booking.getCustomer().getFullName())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .notes(booking.getNotes())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
