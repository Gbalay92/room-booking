package com.gonzalobalay.roombooking.domain.exception;

import java.util.UUID;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(UUID id) {
        super("Room not found with id: " + id);
    }
}
