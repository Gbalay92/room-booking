package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.port.BookingRepository;
import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<BookingSummary> findAllBookingSummaries() {
        return bookingRepository.findAllSummaries();
    }

    @Transactional(readOnly = true)
    public PageResult<BookingSummary> findAllBookingSummaries(int page, int size) {
        return bookingRepository.findAll(page, size);
    }
}
