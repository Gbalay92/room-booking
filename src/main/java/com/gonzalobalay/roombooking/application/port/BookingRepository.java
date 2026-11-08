package com.gonzalobalay.roombooking.application.port;

import com.gonzalobalay.roombooking.domain.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    void deleteById(UUID id);
    Optional<Booking> findById(UUID id);
    List<BookingSummary> findAllSummaries();
    PageResult<BookingSummary> findAll(int page, int size);
}
