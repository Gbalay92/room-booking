package com.gonzalobalay.roombooking.application.port;

import com.gonzalobalay.roombooking.domain.Room;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository {
    Room save(Room room);
    Optional<Room> findById(UUID id);
    void deleteById(UUID id);
    List<Room> findAll();
    Optional<Room> findByIdForUpdate(UUID id);
    List<AvailableRoom> findAvailableRooms(int minCapacity, Instant startsAt, Instant endsAt);
}
