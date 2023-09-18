package com.daoninhthai.booking.controller;

import com.daoninhthai.booking.dto.request.BookingRequest;
import com.daoninhthai.booking.entity.*;
import com.daoninhthai.booking.enums.Role;
import com.daoninhthai.booking.repository.*;
import com.daoninhthai.booking.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private User customer;
    private Provider provider;
    private ServiceEntity service;
    private UsernamePasswordAuthenticationToken customerAuth;

    @BeforeEach
    void setUp() {
        customer = userRepository.save(User.builder()
                .email("ctrl-customer@test.com")
                .password("encoded-password")
                .fullName("Controller Customer")
                .role(Role.CUSTOMER)
                .build());

        User providerUser = userRepository.save(User.builder()
                .email("ctrl-provider@test.com")
                .password("encoded-password")
                .fullName("Controller Provider")
                .role(Role.PROVIDER)
                .build());

        service = serviceRepository.save(ServiceEntity.builder()
                .name("Massage")
                .description("Full body massage")
                .duration(60)
                .price(BigDecimal.valueOf(50.00))
                .category("Wellness")
                .active(true)
                .build());

        provider = providerRepository.save(Provider.builder()
                .user(providerUser)
                .businessName("Test Wellness Center")
                .description("Wellness center for testing")
                .rating(BigDecimal.ZERO)
                .active(true)
                .build());

        timeSlotRepository.save(TimeSlot.builder()
                .provider(provider)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .available(true)
                .build());

        CustomUserDetails userDetails = CustomUserDetails.fromUser(customer);
        customerAuth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void shouldCreateBookingViaApi() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));
        request.setNotes("API test booking");

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(customerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serviceName").value("Massage"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        // First create a booking
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(14, 0));

        String response = mockMvc.perform(post("/api/bookings")
                        .with(authentication(customerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(response).get("data").get("id").asLong();

        mockMvc.perform(get("/api/bookings/" + bookingId)
                        .with(authentication(customerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(bookingId));
    }

    @Test
    void shouldGetMyBookings() throws Exception {
        // Create a booking first
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(11, 0));

        mockMvc.perform(post("/api/bookings")
                        .with(authentication(customerAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bookings/my-bookings")
                        .with(authentication(customerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setServiceId(service.getId());
        request.setProviderId(provider.getId());
        request.setBookingDate(LocalDate.now().plusDays(7));
        request.setStartTime(LocalTime.of(10, 0));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
