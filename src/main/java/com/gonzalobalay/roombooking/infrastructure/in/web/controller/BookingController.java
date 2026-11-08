package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.application.service.BookingService;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.BookingResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

//    @GetMapping
//    public List<BookingResponse> getBookingSummaries() {
//        return bookingService.findAllBookingSummaries().stream().map(BookingResponse::from).toList();
//    }

    @GetMapping()
    public PageResult<BookingResponse> getBookingSummaries(@RequestParam(defaultValue = "0") @Min(0) int page, @Min(1) @Max(100) @RequestParam(defaultValue = "20") int size) {
        PageResult<BookingSummary> bookingSummaries = bookingService.findAllBookingSummaries(page, size);
        return new PageResult<>(bookingSummaries.content().stream().map(BookingResponse::from).toList(),
                bookingSummaries.page(), bookingSummaries.size(), bookingSummaries.totalElements());
    }
}
