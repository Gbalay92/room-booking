package com.gonzalobalay.roombooking.domain;

import java.util.UUID;

public record User(UUID id, String email, String passwordHash, Role role) {
    public User {
        if (id == null || email == null || passwordHash == null || role == null) {
            throw new IllegalArgumentException("No field can be null");
        }
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
    }
}
