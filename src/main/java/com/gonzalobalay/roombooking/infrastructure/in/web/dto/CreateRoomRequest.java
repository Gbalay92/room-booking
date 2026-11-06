package com.gonzalobalay.roombooking.infrastructure.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(@Size(max = 50) @NotBlank String name, @NotNull @Positive Integer capacity) {
}
