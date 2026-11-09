package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.port.BookingRepository;
import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Booking;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.BookingNotFoundException;
import com.gonzalobalay.roombooking.domain.exception.BookingOverlapException;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public Booking findById(UUID id) {
        return bookingRepository.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public BookingSummary findSummaryById(UUID id) {
        return bookingRepository.findSummaryById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PageResult<BookingSummary> findAllBookingSummaries(int page, int size) {
        return bookingRepository.findAll(page, size);
    }

    @Transactional
    public BookingSummary createBooking(UUID roomId, Instant startsAt, Instant endsAt, String userName) {
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new RoomNotFoundException(roomId));
        if (bookingRepository.existsOverlapping(roomId, startsAt, endsAt)) {
            throw new BookingOverlapException(roomId);
        }
        Booking saved = bookingRepository.save(
                new Booking(UUID.randomUUID(), roomId, startsAt, endsAt, userName));   // 3

        return new BookingSummary(saved.id(), saved.roomId(), room.name(),     // 4
                saved.startsAt(), saved.endsAt(), saved.userName());
    }
}
