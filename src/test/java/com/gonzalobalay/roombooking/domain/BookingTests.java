package com.gonzalobalay.roombooking.domain;

import com.gonzalobalay.roombooking.domain.exception.InvalidTimeRangeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTests {

    private static final UUID ID = UUID.randomUUID();
    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final Instant TEN = Instant.parse("2027-03-01T10:00:00Z");
    private static final Instant ELEVEN = Instant.parse("2027-03-01T11:00:00Z");

    @Test
    void createsAValidBooking() {
        assertThatNoException().isThrownBy(() -> new Booking(ID, ROOM_ID, TEN, ELEVEN, "alice@test"));
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> new Booking(ID, ROOM_ID, ELEVEN, TEN, "alice@test"))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void rejectsZeroLengthBooking() {
        assertThatThrownBy(() -> new Booking(ID, ROOM_ID, TEN, TEN, "alice@test"))
                .isInstanceOf(InvalidTimeRangeException.class);
    }

    @Test
    void rejectsMissingRequiredFields() {
        assertThatThrownBy(() -> new Booking(null, ROOM_ID, TEN, ELEVEN, "alice@test"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Booking(ID, null, TEN, ELEVEN, "alice@test"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Booking(ID, ROOM_ID, null, ELEVEN, "alice@test"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Booking(ID, ROOM_ID, TEN, null, "alice@test"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsBlankUserName(String userName) {
        assertThatThrownBy(() -> new Booking(ID, ROOM_ID, TEN, ELEVEN, userName))
                .isInstanceOf(IllegalArgumentException.class);
    }
}