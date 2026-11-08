package com.gonzalobalay.roombooking.infrastructure.in.web.dto;

import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.domain.Booking;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(UUID id, UUID roomId, String roomName, Instant startsAt, Instant endsAt,
                              String userName) {

    public static BookingResponse from(BookingSummary bookingSummary) {
        return new BookingResponse(bookingSummary.id(), bookingSummary.roomId(), bookingSummary.roomName(), bookingSummary.startsAt(), bookingSummary.endsAt(), bookingSummary.userName());
    }

}
