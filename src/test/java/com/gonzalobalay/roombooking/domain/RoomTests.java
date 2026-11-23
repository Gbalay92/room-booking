package com.gonzalobalay.roombooking.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoomTests {

    private static final UUID ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void rejectsNonPositiveCapacity() {
        assertThatThrownBy(() -> new Room(ID, "Room A", 0, CREATED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new Room(ID, " ", 4, CREATED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingIdOrCreationDate() {
        assertThatThrownBy(() -> new Room(null, "Room A", 4, CREATED_AT, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Room(ID, "Room A", 4, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withDetailsKeepsIdentityAndCreationDate() {
        Room updated = new Room(ID, "Room A", 4, CREATED_AT, 0L).withDetails("Room B", 8, 1L);

        assertThat(updated.id()).isEqualTo(ID);
        assertThat(updated.createdAt()).isEqualTo(CREATED_AT);
        assertThat(updated.name()).isEqualTo("Room B");
        assertThat(updated.capacity()).isEqualTo(8);
    }
}