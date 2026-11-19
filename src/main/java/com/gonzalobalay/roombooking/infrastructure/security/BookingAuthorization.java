package com.gonzalobalay.roombooking.infrastructure.security;

import com.gonzalobalay.roombooking.application.port.BookingRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("bookingAuthorization")
public class BookingAuthorization {

    private final BookingRepository bookingRepository;

    public BookingAuthorization(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public boolean isOwner(UUID bookingId, String username) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.userName().equals(username))
                .orElse(true);   // not found → let the service throw 404 instead of a misleading 403
    }
}
