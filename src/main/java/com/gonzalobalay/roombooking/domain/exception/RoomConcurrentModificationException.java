package com.gonzalobalay.roombooking.domain.exception;

public class RoomConcurrentModificationException extends RuntimeException {
    public RoomConcurrentModificationException(Long version) {
        super("Room has been modified since it was read. Expected version: " + version);
    }
}
