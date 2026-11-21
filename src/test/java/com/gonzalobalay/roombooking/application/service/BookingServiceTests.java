package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.port.BookingRepository;
import com.gonzalobalay.roombooking.application.port.BookingSummary;
import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Booking;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.BookingOverlapException;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTests {

    private static final UUID ROOM_ID = UUID.randomUUID();
    private static final Instant TEN = Instant.parse("2027-03-01T10:00:00Z");
    private static final Instant ELEVEN = Instant.parse("2027-03-01T11:00:00Z");
    private static final Room ROOM = new Room(ROOM_ID, "Room A", 4, Instant.parse("2026-01-01T00:00:00Z"), 0L);

    @Mock
    BookingRepository bookingRepository;
    @Mock
    RoomRepository roomRepository;
    @InjectMocks
    BookingService bookingService;

    @Test
    void createsBookingWhenSlotIsFree() {
        when(roomRepository.findByIdForUpdate(ROOM_ID)).thenReturn(Optional.of(ROOM));
        when(bookingRepository.existsOverlapping(ROOM_ID, TEN, ELEVEN)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingSummary summary = bookingService.createBookingOnce(ROOM_ID, TEN, ELEVEN, "alice@test");

        assertThat(summary.roomName()).isEqualTo("Room A");
        assertThat(summary.userName()).isEqualTo("alice@test");
    }

    @Test
    void rejectsOverlappingBookingWithoutSaving() {
        when(roomRepository.findByIdForUpdate(ROOM_ID)).thenReturn(Optional.of(ROOM));
        when(bookingRepository.existsOverlapping(ROOM_ID, TEN, ELEVEN)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBookingOnce(ROOM_ID, TEN, ELEVEN, "alice@test"))
                .isInstanceOf(BookingOverlapException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void failsWhenRoomDoesNotExist() {
        when(roomRepository.findByIdForUpdate(ROOM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBookingOnce(ROOM_ID, TEN, ELEVEN, "alice@test"))
                .isInstanceOf(RoomNotFoundException.class);
        verifyNoInteractions(bookingRepository);
    }
}