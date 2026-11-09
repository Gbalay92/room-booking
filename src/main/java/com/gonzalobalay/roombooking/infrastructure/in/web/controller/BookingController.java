package com.gonzalobalay.roombooking.infrastructure.in.web.controller;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.PageResult;
import com.gonzalobalay.roombooking.application.service.BookingService;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.BookingResponse;
import com.gonzalobalay.roombooking.infrastructure.in.web.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


    @GetMapping()
    public PageResult<BookingResponse> getBookingSummaries(@RequestParam(defaultValue = "0") @Min(0) int page, @Min(1) @Max(100) @RequestParam(defaultValue = "20") int size) {
        PageResult<BookingSummary> bookingSummaries = bookingService.findAllBookingSummaries(page, size);
        return new PageResult<>(bookingSummaries.content().stream().map(BookingResponse::from).toList(),
                bookingSummaries.page(), bookingSummaries.size(), bookingSummaries.totalElements());
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingSummary(@PathVariable UUID id) {
        BookingSummary bookingSummary = bookingService.findSummaryById(id);
        return BookingResponse.from(bookingSummary);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest bookingRequest) {
        BookingSummary bookingSummary = bookingService.createBooking(bookingRequest.roomId(), bookingRequest.startsAt(), bookingRequest.endsAt(), bookingRequest.userName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bookingSummary.id())
                .toUri();

        return ResponseEntity.created(location).body(BookingResponse.from(bookingSummary));
    }
}
