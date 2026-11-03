package com.gonzalobalay.roombooking.domain.exception;

public class RoomCapacityExceededException extends RuntimeException {

    public RoomCapacityExceededException(int capacity, int maxCapacity) {
        super("Room capacity exceeds " + capacity + " the maximum capacity of " + maxCapacity);
    }

}


