package com.gonzalobalay.roombooking.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateBookingRequest(@NotNull UUID roomId,@NotNull Instant startsAt, @NotNull Instant endsAt,
                                   @NotBlank @Size(max = 50) String userName) {
}
