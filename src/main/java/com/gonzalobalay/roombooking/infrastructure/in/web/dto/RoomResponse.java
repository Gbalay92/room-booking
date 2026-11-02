package com.gonzalobalay.roombooking.infrastructure.in.web.dto;

import com.gonzalobalay.roombooking.domain.Room;

import java.time.Instant;
import java.util.UUID;

public record RoomResponse(UUID id, String name, Integer capacity, Instant createdAt) {

    public RoomResponse {

    }

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.id(), room.name(), room.capacity(), room.createdAt());
    }

}
