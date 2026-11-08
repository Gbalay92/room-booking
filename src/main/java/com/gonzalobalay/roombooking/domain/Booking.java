package com.gonzalobalay.roombooking.domain;

import java.time.Instant;
import java.util.UUID;

public record Booking(UUID id, UUID roomId, Instant startsAt, Instant endsAt, String userName) {

    public Booking {
        if (id == null) {
            throw new IllegalArgumentException("Booking id cannot be null");
        }
        if (roomId == null) {
            throw new IllegalArgumentException("Booking roomId cannot be null");
        }
        if (startsAt == null) {
            throw new IllegalArgumentException("Booking startsAt cannot be null");
        }
        if (endsAt == null) {
            throw new IllegalArgumentException("Booking endsAt cannot be null");
        }
        if (!endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("Booking startsAt must be before endsAt");
        }
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("Booking userName cannot be blank");
        }
    }

}
