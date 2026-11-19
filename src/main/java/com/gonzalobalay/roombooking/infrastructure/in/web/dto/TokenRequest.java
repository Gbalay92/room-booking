package com.gonzalobalay.roombooking.infrastructure.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank @Email String email, @NotBlank String password) {
}
