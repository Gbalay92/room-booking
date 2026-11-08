package com.gonzalobalay.roombooking.application.port;

import com.gonzalobalay.roombooking.domain.Booking;

import java.time.Instant;
import java.util.UUID;

public record BookingSummary(UUID id, UUID roomId, String roomName, Instant startsAt, Instant endsAt, String userName) {}
