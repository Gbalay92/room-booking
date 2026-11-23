package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.config.BookingProperties;
import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.InvalidTimeRangeException;
import com.gonzalobalay.roombooking.domain.exception.RoomCapacityExceededException;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTests {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    RoomRepository roomRepository;

    RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository,
                Clock.fixed(NOW, ZoneOffset.UTC),
                new BookingProperties(LocalTime.of(8, 0), LocalTime.of(20, 0), Duration.ofMinutes(30), 10));
    }

    @Test
    void createUsesTheInjectedClock() {
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(roomService.create("Room A", 4).createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsCapacityAboveTheConfiguredLimitWithoutSaving() {
        assertThatThrownBy(() -> roomService.create("Room A", 11))
                .isInstanceOf(RoomCapacityExceededException.class);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void rejectsUpdateIfRoomDoesNotExistWithoutSaving() {
        UUID id = UUID.randomUUID();
        when(roomRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.update(id, "Room A", 4, 2L))
                .isInstanceOf(RoomNotFoundException.class);
        verify(roomRepository, never()).save(any());
    }

    @Test
    void findAvailableRoomsRejectsInvertedRangeWithoutQuerying() {
        assertThatThrownBy(() -> roomService.findAvailableRooms(2, NOW.plusSeconds(3600), NOW))
                .isInstanceOf(InvalidTimeRangeException.class);
        verifyNoInteractions(roomRepository);
    }

    @Test
    void updateDoesNotModifyCreatedAt() {
        UUID id = UUID.randomUUID();
        when(roomRepository.findById(id)).thenReturn(Optional.of(new Room(id, "Room A", 4, NOW, 0L)));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room updated = roomService.update(id, "Room B", 5, 0L);

        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.name()).isEqualTo("Room B");
    }
}