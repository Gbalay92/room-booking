package com.gonzalobalay.roombooking.domain.exception;

import java.util.UUID;

public class BookingOverlapException extends RuntimeException {

    public BookingOverlapException(UUID roomId) {
        super("This booking overlaps with another booking for room: " + roomId);
    }

}
