package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.BookingRequest;
import com.daoninhthai.booking.dto.response.BookingResponse;
import com.daoninhthai.booking.entity.*;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.enums.Role;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User customer;
    private User providerUser;
    private Provider provider;
    private ServiceEntity service;

    @BeforeEach
    void setUp() {
        customer = userRepository.save(User.builder()
                .email("customer@test.com")
                .password("encoded-password")
                .fullName("Test Customer")
                .phone("0123456789")
                .role(Role.CUSTOMER)
                .build());

        providerUser = userRepository.save(User.builder()
                .email("provider@test.com")
                .password("encoded-password")
                .fullName("Test Provider")
                .phone("0987654321")
                .role(Role.PROVIDER)
                .build());

        service = serviceRepository.save(ServiceEntity.builder()
                .name("Haircut")
                .description("Standard haircut service")
                .duration(30)
                .price(BigDecimal.valueOf(25.00))
                .category("Beauty")
                .active(true)
                .build());

        provider = providerRepository.save(Provider.builder()
                .user(providerUser)
                .businessName("Test Salon")
                .description("A test salon")
                .phone("0987654321")
                .address("123 Test Street")
                .rating(BigDecimal.ZERO)
                .active(true)
                .build());

        timeSlotRepository.save(TimeSlot.builder()
                .provider(provider)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .available(true)
                .build());
    }

    @Test
    void shouldCreateBookingSuccessfully() {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));
        request.setNotes("Test booking");

        BookingResponse response = bookingService.createBooking(customer.getId(), request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(service.getId(), response.getServiceId());
        assertEquals(provider.getId(), response.getProviderId());
        assertEquals(customer.getId(), response.getCustomerId());
        assertEquals(BookingStatus.PENDING, response.getStatus());
        assertEquals(LocalTime.of(10, 0), response.getStartTime());
        assertEquals(LocalTime.of(10, 30), response.getEndTime());
        assertEquals(0, BigDecimal.valueOf(25.00).compareTo(response.getTotalPrice()));
    }

    @Test
    void shouldConfirmBooking() {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));

        BookingResponse created = bookingService.createBooking(customer.getId(), request);
        BookingResponse confirmed = bookingService.confirmBooking(created.getId());

        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
    }

    @Test
    void shouldCancelBooking() {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));

        BookingResponse created = bookingService.createBooking(customer.getId(), request);
        BookingResponse cancelled = bookingService.cancelBooking(created.getId(), customer.getId());

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void shouldCompleteBooking() {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));

        BookingResponse created = bookingService.createBooking(customer.getId(), request);
        bookingService.confirmBooking(created.getId());
        BookingResponse completed = bookingService.completeBooking(created.getId());

        assertEquals(BookingStatus.COMPLETED, completed.getStatus());
    }

    @Test
    void shouldDetectBookingConflict() {
        BookingRequest request1 = new BookingRequest();
        request1.setServiceId(service.getId());
        request1.setProviderId(provider.getId());
        request1.setBookingDate(LocalDate.now().plusDays(7));
        request1.setStartTime(LocalTime.of(10, 0));

        bookingService.createBooking(customer.getId(), request1);

        BookingRequest request2 = new BookingRequest();
        request2.setServiceId(service.getId());
        request2.setProviderId(provider.getId());
        request2.setBookingDate(LocalDate.now().plusDays(7));
        request2.setStartTime(LocalTime.of(10, 15));

        assertThrows(BadRequestException.class, () ->
                bookingService.createBooking(customer.getId(), request2));
    }

    @Test
    void shouldGetBookingsByCustomer() {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));

        bookingService.createBooking(customer.getId(), request);

        var bookings = bookingService.getBookingsByCustomer(customer.getId());
        assertFalse(bookings.isEmpty());
        assertEquals(1, bookings.size());
    }
}
