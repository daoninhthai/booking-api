package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.response.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyBookingCreated(BookingResponse booking) {
        Map<String, Object> payload = buildPayload("BOOKING_CREATED", booking);

        // Notify the provider about new booking
        messagingTemplate.convertAndSendToUser(
                String.valueOf(booking.getProviderId()),
                "/queue/bookings",
                payload);

        // Notify the customer about booking creation
        messagingTemplate.convertAndSendToUser(
                String.valueOf(booking.getCustomerId()),
                "/queue/bookings",
                payload);

        // Broadcast to general topic for admin monitoring
        messagingTemplate.convertAndSend("/topic/bookings", payload);

        logger.info("WebSocket notification sent for booking created: id={}", booking.getId());
    }

    public void notifyBookingConfirmed(BookingResponse booking) {
        Map<String, Object> payload = buildPayload("BOOKING_CONFIRMED", booking);

        // Notify the customer about booking confirmation
        messagingTemplate.convertAndSendToUser(
                String.valueOf(booking.getCustomerId()),
                "/queue/bookings",
                payload);

        // Broadcast to general topic
        messagingTemplate.convertAndSend("/topic/bookings", payload);

        logger.info("WebSocket notification sent for booking confirmed: id={}", booking.getId());
    }

    public void notifyBookingCancelled(BookingResponse booking) {
        Map<String, Object> payload = buildPayload("BOOKING_CANCELLED", booking);

        // Notify the provider
        messagingTemplate.convertAndSendToUser(
                String.valueOf(booking.getProviderId()),
                "/queue/bookings",
                payload);

        // Notify the customer
        messagingTemplate.convertAndSendToUser(
                String.valueOf(booking.getCustomerId()),
                "/queue/bookings",
                payload);

        // Broadcast to general topic
        messagingTemplate.convertAndSend("/topic/bookings", payload);

        logger.info("WebSocket notification sent for booking cancelled: id={}", booking.getId());
    }

    public void notifyBookingCompleted(BookingResponse booking) {
        Map<String, Object> payload = buildPayload("BOOKING_COMPLETED", booking);

        // Notify the customer
        messagingTemplate.convertAndSendToUser(
                String.valueOf(booking.getCustomerId()),
                "/queue/bookings",
                payload);

        messagingTemplate.convertAndSend("/topic/bookings", payload);

        logger.info("WebSocket notification sent for booking completed: id={}", booking.getId());
    }

    private Map<String, Object> buildPayload(String eventType, BookingResponse booking) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", eventType);
        payload.put("bookingId", booking.getId());
        payload.put("serviceName", booking.getServiceName());
        payload.put("providerName", booking.getProviderName());
        payload.put("customerName", booking.getCustomerName());
        payload.put("bookingDate", booking.getBookingDate().toString());
        payload.put("startTime", booking.getStartTime().toString());
        payload.put("endTime", booking.getEndTime().toString());
        payload.put("status", booking.getStatus().name());
        payload.put("totalPrice", booking.getTotalPrice());
        return payload;
    }
}
