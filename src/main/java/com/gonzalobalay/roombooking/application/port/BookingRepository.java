package com.gonzalobalay.roombooking.application.port;

import com.gonzalobalay.roombooking.domain.Booking;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    void deleteById(UUID id);
    Optional<Booking> findById(UUID id);
    PageResult<BookingSummary> findAll(int page, int size);
}
