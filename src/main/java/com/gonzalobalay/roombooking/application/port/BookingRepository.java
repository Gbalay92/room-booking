package com.gonzalobalay.roombooking.application.port;

import com.gonzalobalay.roombooking.domain.Booking;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    void deleteById(UUID id);
    Optional<Booking> findById(UUID id);
    Optional<BookingSummary> findSummaryById(UUID id);
    PageResult<BookingSummary> findAll(int page, int size);
    boolean existsOverlapping(UUID roomId, Instant startsAt, Instant endsAt);
}
