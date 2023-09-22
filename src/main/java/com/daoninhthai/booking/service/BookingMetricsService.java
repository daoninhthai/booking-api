package com.daoninhthai.booking.service;

import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.repository.BookingRepository;
import com.daoninhthai.booking.repository.ProviderRepository;
import com.daoninhthai.booking.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class BookingMetricsService {

    private final MeterRegistry meterRegistry;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ProviderRepository providerRepository;

    private Counter bookingsCreatedCounter;
    private Counter bookingsConfirmedCounter;
    private Counter bookingsCancelledCounter;
    private Counter bookingsCompletedCounter;
    private Timer bookingCreationTimer;

    private final AtomicLong activeBookingsGauge = new AtomicLong(0);
    private final AtomicLong totalUsersGauge = new AtomicLong(0);
    private final AtomicLong totalProvidersGauge = new AtomicLong(0);

    @PostConstruct
    public void initMetrics() {
        bookingsCreatedCounter = Counter.builder("booking.created")
                .description("Total number of bookings created")
                .tag("type", "created")
                .register(meterRegistry);

        bookingsConfirmedCounter = Counter.builder("booking.confirmed")
                .description("Total number of bookings confirmed")
                .tag("type", "confirmed")
                .register(meterRegistry);

        bookingsCancelledCounter = Counter.builder("booking.cancelled")
                .description("Total number of bookings cancelled")
                .tag("type", "cancelled")
                .register(meterRegistry);

        bookingsCompletedCounter = Counter.builder("booking.completed")
                .description("Total number of bookings completed")
                .tag("type", "completed")
                .register(meterRegistry);

        bookingCreationTimer = Timer.builder("booking.creation.time")
                .description("Time taken to create a booking")
                .register(meterRegistry);

        Gauge.builder("booking.active.count", activeBookingsGauge, AtomicLong::get)
                .description("Current number of active bookings (pending + confirmed)")
                .register(meterRegistry);

        Gauge.builder("users.total.count", totalUsersGauge, AtomicLong::get)
                .description("Total number of registered users")
                .register(meterRegistry);

        Gauge.builder("providers.total.count", totalProvidersGauge, AtomicLong::get)
                .description("Total number of active providers")
                .register(meterRegistry);
    }

    public void incrementBookingsCreated() {
        bookingsCreatedCounter.increment();
        refreshGauges();
    }

    public void incrementBookingsConfirmed() {
        bookingsConfirmedCounter.increment();
        refreshGauges();
    }

    public void incrementBookingsCancelled() {
        bookingsCancelledCounter.increment();
        refreshGauges();
    }

    public void incrementBookingsCompleted() {
        bookingsCompletedCounter.increment();
        refreshGauges();
    }

    public Timer getBookingCreationTimer() {
        return bookingCreationTimer;
    }

    public void refreshGauges() {
        long pendingCount = bookingRepository.findByStatus(BookingStatus.PENDING).size();
        long confirmedCount = bookingRepository.findByStatus(BookingStatus.CONFIRMED).size();
        activeBookingsGauge.set(pendingCount + confirmedCount);

        totalUsersGauge.set(userRepository.count());
        totalProvidersGauge.set(providerRepository.findByActiveTrue().size());
    }
}
