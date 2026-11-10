package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.domain.exception.BookingOverlapException;
import com.gonzalobalay.roombooking.infrastructure.config.ResilienceConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig({RetryingBookingService.class, ResilienceConfig.class})
class RetryingBookingServiceTests {

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final Instant START = Instant.parse("2030-01-01T10:00:00Z");
    private static final Instant END = START.plus(1, ChronoUnit.HOURS);

    @MockitoBean
    BookingService bookingService;

    @Autowired
    RetryingBookingService retryingBookingService;

    @Test
    void retriesOnTransientLockFailure() {
        BookingSummary summary = new BookingSummary(UUID.randomUUID(), ROOM_ID, "Room", START, END, "user");
        when(bookingService.createBookingOnce(any(), any(), any(), any()))
                .thenThrow(new CannotAcquireLockException("deadlock"))
                .thenThrow(new CannotAcquireLockException("deadlock"))
                .thenReturn(summary);

        BookingSummary result = retryingBookingService.createBooking(ROOM_ID, START, END, "user");

        assertThat(result).isEqualTo(summary);
        verify(bookingService, times(3)).createBookingOnce(any(), any(), any(), any());
    }

    @Test
    void givesUpAfterMaxRetries() {
        when(bookingService.createBookingOnce(any(), any(), any(), any()))
                .thenThrow(new CannotAcquireLockException("deadlock"));

        assertThatThrownBy(() -> retryingBookingService.createBooking(ROOM_ID, START, END, "user"))
                .isInstanceOf(CannotAcquireLockException.class);

        verify(bookingService, times(5)).createBookingOnce(any(), any(), any(), any());
    }

    @Test
    void doesNotRetryBusinessErrors() {
        when(bookingService.createBookingOnce(any(), any(), any(), any()))
                .thenThrow(new BookingOverlapException(ROOM_ID));

        assertThatThrownBy(() -> retryingBookingService.createBooking(ROOM_ID, START, END, "user"))
                .isInstanceOf(BookingOverlapException.class);

        verify(bookingService, times(1)).createBookingOnce(any(), any(), any(), any());
    }
}
