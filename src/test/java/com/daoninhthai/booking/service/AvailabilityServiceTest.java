package com.daoninhthai.booking.service;

import com.daoninhthai.booking.dto.request.TimeSlotRequest;
import com.daoninhthai.booking.dto.response.TimeSlotResponse;
import com.daoninhthai.booking.entity.Provider;
import com.daoninhthai.booking.entity.User;
import com.daoninhthai.booking.enums.Role;
import com.daoninhthai.booking.exception.BadRequestException;
import com.daoninhthai.booking.repository.ProviderRepository;
import com.daoninhthai.booking.repository.TimeSlotRepository;
import com.daoninhthai.booking.repository.UserRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AvailabilityServiceTest {

    @Autowired
    private TimeSlotService timeSlotService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    private Provider provider;

    @BeforeEach
    void setUp() {
        User providerUser = userRepository.save(User.builder()
                .email("avail-provider@test.com")
                .password("encoded-password")
                .fullName("Availability Provider")
                .role(Role.PROVIDER)
                .build());

        provider = providerRepository.save(Provider.builder()
                .user(providerUser)
                .businessName("Test Provider")
                .description("Testing availability")
                .rating(BigDecimal.ZERO)
                .active(true)
                .build());
    }

    @Test
    void shouldCreateTimeSlot() {
        TimeSlotRequest request = new TimeSlotRequest();
        request.setProviderId(provider.getId());
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));

        TimeSlotResponse response = timeSlotService.createTimeSlot(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(DayOfWeek.MONDAY, response.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), response.getStartTime());
        assertEquals(LocalTime.of(12, 0), response.getEndTime());
        assertTrue(response.getAvailable());
    }

    @Test
    void shouldRejectInvalidTimeRange() {
        TimeSlotRequest request = new TimeSlotRequest();
        request.setProviderId(provider.getId());
        request.setDayOfWeek(DayOfWeek.MONDAY);
        request.setStartTime(LocalTime.of(14, 0));
        request.setEndTime(LocalTime.of(10, 0)); // end before start

        assertThrows(BadRequestException.class, () ->
                timeSlotService.createTimeSlot(request));
    }

    @Test
    void shouldGetAvailableSlots() {
        TimeSlotRequest request = new TimeSlotRequest();
        request.setProviderId(provider.getId());
        request.setDayOfWeek(DayOfWeek.WEDNESDAY);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(12, 0));

        timeSlotService.createTimeSlot(request);

        // Find next Wednesday
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
            date = date.plusDays(1);
        }

        List<TimeSlotResponse> slots = timeSlotService.getAvailableSlots(provider.getId(), date);
        assertFalse(slots.isEmpty());
    }

    @Test
    void shouldBlockSlot() {
        TimeSlotRequest request = new TimeSlotRequest();
        request.setProviderId(provider.getId());
        request.setDayOfWeek(DayOfWeek.FRIDAY);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(10, 0));

        TimeSlotResponse created = timeSlotService.createTimeSlot(request);
        TimeSlotResponse blocked = timeSlotService.blockSlot(created.getId());

        assertFalse(blocked.getAvailable());
    }

    @Test
    void shouldUpdateAvailability() {
        TimeSlotRequest request = new TimeSlotRequest();
        request.setProviderId(provider.getId());
        request.setDayOfWeek(DayOfWeek.THURSDAY);
        request.setStartTime(LocalTime.of(13, 0));
        request.setEndTime(LocalTime.of(14, 0));

        TimeSlotResponse created = timeSlotService.createTimeSlot(request);
        TimeSlotResponse updated = timeSlotService.updateAvailability(created.getId(), false);

        assertFalse(updated.getAvailable());

        // Re-enable
        updated = timeSlotService.updateAvailability(created.getId(), true);
        assertTrue(updated.getAvailable());
    }

    @Test
    void shouldGetSlotsByProvider() {
        TimeSlotRequest request1 = new TimeSlotRequest();
        request1.setProviderId(provider.getId());
        request1.setDayOfWeek(DayOfWeek.MONDAY);
        request1.setStartTime(LocalTime.of(9, 0));
        request1.setEndTime(LocalTime.of(12, 0));
        timeSlotService.createTimeSlot(request1);

        TimeSlotRequest request2 = new TimeSlotRequest();
        request2.setProviderId(provider.getId());
        request2.setDayOfWeek(DayOfWeek.TUESDAY);
        request2.setStartTime(LocalTime.of(14, 0));
        request2.setEndTime(LocalTime.of(17, 0));
        timeSlotService.createTimeSlot(request2);

        List<TimeSlotResponse> slots = timeSlotService.getSlotsByProvider(provider.getId());
        assertEquals(2, slots.size());
    }
}
