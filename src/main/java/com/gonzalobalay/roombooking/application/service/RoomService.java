package com.gonzalobalay.roombooking.application.service;

import com.gonzalobalay.roombooking.application.config.BookingProperties;
import com.gonzalobalay.roombooking.application.port.AvailableRoom;
import com.gonzalobalay.roombooking.application.port.RoomRepository;
import com.gonzalobalay.roombooking.domain.Room;
import com.gonzalobalay.roombooking.domain.exception.InvalidTimeRangeException;
import com.gonzalobalay.roombooking.domain.exception.RoomCapacityExceededException;
import com.gonzalobalay.roombooking.domain.exception.RoomNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final Clock clock;
    private final BookingProperties bookingProperties;

    public RoomService(RoomRepository roomRepository, Clock clock, BookingProperties bookingProperties) {
        this.roomRepository = roomRepository;
        this.clock = clock;
        this.bookingProperties = bookingProperties;
    }

    public Room create(String name, int capacity) {
        checkCapacity(capacity);
        return roomRepository.save(new Room(UUID.randomUUID(), name, capacity, Instant.now(clock), null));
    }

    public Room findById(UUID id) {
        return roomRepository.findById(id).orElseThrow(() -> new RoomNotFoundException(id));
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public void delete(UUID id) {
        roomRepository.deleteById(id);
    }

    public Room update(UUID id, String name, int capacity, Long version) {
        Room existingRoom = findById(id);
        checkCapacity(capacity);
        Room updatedRoom = existingRoom.withDetails(name, capacity, version);
        return roomRepository.save(updatedRoom);
    }

    private void checkCapacity(int capacity) {
        if (capacity > bookingProperties.maxRoomCapacity()) {
            throw new RoomCapacityExceededException(capacity, bookingProperties.maxRoomCapacity());
        }
    }

    @Transactional(readOnly = true)
    public List<AvailableRoom> findAvailableRooms(int minCapacity, Instant startsAt, Instant endsAt) {
        if (!endsAt.isAfter(startsAt)) {
            throw new InvalidTimeRangeException(startsAt, endsAt);
        }
        return roomRepository.findAvailableRooms(minCapacity, startsAt, endsAt);
    }

}
