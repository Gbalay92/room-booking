package com.gonzalobalay.roombooking.domain;

import java.time.Instant;
import java.util.UUID;

public record Room(UUID id, String name, int capacity, Instant createdAt) {

    public Room {
        if (id == null) {
            throw new IllegalArgumentException("Room id cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Room creation date cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than 0");
        }
    }

    public Room withDetails(String name, int capacity) {
        return new Room(this.id, name, capacity, this.createdAt);
    }
}
