package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RetryingBookingService {

    private final BookingService bookingService;

    public RetryingBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }
    @Retryable(includes = ConcurrencyFailureException.class,
            maxRetries = 4, delay = 50, multiplier = 2, jitter = 25)
    public BookingSummary createBooking(UUID roomId, Instant startsAt, Instant endsAt, String userName) {
        return bookingService.createBookingOnce(roomId, startsAt, endsAt, userName);
    }
}
