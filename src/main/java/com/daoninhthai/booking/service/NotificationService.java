package com.daoninhthai.booking.service;

import com.daoninhthai.booking.entity.Booking;
import com.daoninhthai.booking.enums.BookingStatus;
import com.daoninhthai.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final EmailTemplateService emailTemplateService;
    private final BookingRepository bookingRepository;

    @Async
    public void sendBookingConfirmation(Booking booking) {
        try {
            String htmlContent = emailTemplateService.renderBookingConfirmation(booking);
            String to = booking.getCustomer().getEmail();
            String subject = "Booking Confirmation - #" + booking.getId();

            // In production, integrate with actual email service (e.g., SendGrid, SES)
            logger.info("Sending booking confirmation email to: {} | Subject: {} | Booking ID: {}",
                    to, subject, booking.getId());
            logger.debug("Email content length: {}", htmlContent.length());

            // Simulate email send
            simulateEmailSend(to, subject, htmlContent);

            logger.info("Booking confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email for booking: {}", booking.getId(), e);
        }
    }

    @Async
    public void sendBookingCancellation(Booking booking) {
        try {
            String htmlContent = emailTemplateService.renderBookingCancellation(booking);
            String to = booking.getCustomer().getEmail();
            String subject = "Booking Cancelled - #" + booking.getId();

            logger.info("Sending booking cancellation email to: {} | Subject: {} | Booking ID: {}",
                    to, subject, booking.getId());

            simulateEmailSend(to, subject, htmlContent);

            logger.info("Booking cancellation email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send booking cancellation email for booking: {}", booking.getId(), e);
        }
    }

    @Async
    public void sendReminder(Booking booking) {
        try {
            String htmlContent = emailTemplateService.renderBookingReminder(booking);
            String to = booking.getCustomer().getEmail();
            String subject = "Booking Reminder - Tomorrow at " + booking.getStartTime();

            logger.info("Sending booking reminder email to: {} | Booking ID: {}", to, booking.getId());

            simulateEmailSend(to, subject, htmlContent);

            logger.info("Booking reminder email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send booking reminder email for booking: {}", booking.getId(), e);
        }
    }

    /**
     * Scheduled task to send reminders for bookings happening tomorrow.
     * Runs every day at 9:00 AM.
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        logger.info("Running daily reminder job for bookings on: {}", tomorrow);

        List<Booking> tomorrowBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingDate().equals(tomorrow))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList();

        for (Booking booking : tomorrowBookings) {
            sendReminder(booking);
        }

        logger.info("Daily reminder job completed. Sent {} reminders.", tomorrowBookings.size());
    }

    private void simulateEmailSend(String to, String subject, String htmlContent) {
        // This is a placeholder for actual email integration
        // Replace with JavaMailSender, SendGrid, AWS SES, etc.
        logger.debug("Email to: {}, Subject: {}, Content length: {}", to, subject, htmlContent.length());
    }
}
