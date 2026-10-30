package com.gonzalobalay.roombooking.domain;

import java.util.UUID;

public record Room(UUID id, String name, int capacity) {

    public Room {
        if (id == null) {
            throw new IllegalArgumentException("Room id cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than 0");
        }
    }
}
