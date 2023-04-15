package com.daoninhthai.booking.service;

import com.daoninhthai.booking.entity.Booking;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public String renderBookingConfirmation(Booking booking) {
        return String.format("""
                <html>
                <body>
                    <h2>Booking Confirmation</h2>
                    <p>Dear %s,</p>
                    <p>Your booking has been confirmed!</p>
                    <table>
                        <tr><td><strong>Service:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Provider:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Date:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Time:</strong></td><td>%s - %s</td></tr>
                        <tr><td><strong>Total Price:</strong></td><td>$%s</td></tr>
                    </table>
                    <p>Booking ID: #%d</p>
                    <p>If you need to cancel, please do so at least 24 hours in advance.</p>
                    <p>Thank you for choosing our service!</p>
                </body>
                </html>
                """,
                booking.getCustomer().getFullName(),
                booking.getService().getName(),
                booking.getProvider().getBusinessName(),
                booking.getBookingDate().format(DATE_FORMAT),
                booking.getStartTime().format(TIME_FORMAT),
                booking.getEndTime().format(TIME_FORMAT),
                booking.getTotalPrice().toString(),
                booking.getId());
    }

    public String renderBookingCancellation(Booking booking) {
        return String.format("""
                <html>
                <body>
                    <h2>Booking Cancellation</h2>
                    <p>Dear %s,</p>
                    <p>Your booking #%d has been cancelled.</p>
                    <table>
                        <tr><td><strong>Service:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Date:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Time:</strong></td><td>%s - %s</td></tr>
                    </table>
                    <p>If you did not request this cancellation, please contact us immediately.</p>
                </body>
                </html>
                """,
                booking.getCustomer().getFullName(),
                booking.getId(),
                booking.getService().getName(),
                booking.getBookingDate().format(DATE_FORMAT),
                booking.getStartTime().format(TIME_FORMAT),
                booking.getEndTime().format(TIME_FORMAT));
    }

    public String renderBookingReminder(Booking booking) {
        return String.format("""
                <html>
                <body>
                    <h2>Booking Reminder</h2>
                    <p>Dear %s,</p>
                    <p>This is a reminder for your upcoming booking:</p>
                    <table>
                        <tr><td><strong>Service:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Provider:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Date:</strong></td><td>%s</td></tr>
                        <tr><td><strong>Time:</strong></td><td>%s - %s</td></tr>
                    </table>
                    <p>We look forward to seeing you!</p>
                </body>
                </html>
                """,
                booking.getCustomer().getFullName(),
                booking.getService().getName(),
                booking.getProvider().getBusinessName(),
                booking.getBookingDate().format(DATE_FORMAT),
                booking.getStartTime().format(TIME_FORMAT),
                booking.getEndTime().format(TIME_FORMAT));
    }
}
