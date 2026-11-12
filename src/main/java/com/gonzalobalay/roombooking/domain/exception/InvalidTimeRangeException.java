package com.gonzalobalay.roombooking.domain.exception;

import java.time.Instant;

public class InvalidTimeRangeException extends RuntimeException {
    public InvalidTimeRangeException(Instant startsAt, Instant endsAt) {
        super(String.format("startsAt %s must be before endsAt %s", startsAt, endsAt));
    }
}
