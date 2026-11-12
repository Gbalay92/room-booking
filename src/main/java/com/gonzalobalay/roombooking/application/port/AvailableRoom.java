package com.gonzalobalay.roombooking.application.port;

import java.util.UUID;

public record AvailableRoom(UUID id, String name, int capacity) { }
